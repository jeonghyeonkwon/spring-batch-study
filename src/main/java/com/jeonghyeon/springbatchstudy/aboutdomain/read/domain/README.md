# 배치 도메인 이해

## JOB
* 계층 구조에서 가장 상위에 있는 개념으로서 하나의 배치 작업 자체를 의미
* 하나 이상의 Step으로 구성해야 함

### 기본 구현체
#### SimpleJob
* 순차적 Step을 실행시키는 JOB
#### FlowJob
* 특정 조건과 흐름에 따라 Step 을 구성하여 실행시키는 JOB

### JOB 흐름
1. JobLauncher로 Job을 실행 시킴
   1. JobLauncher의 run()메소드의 Job과 JobParameter를 받아서 
2. Job의 execute()로 steps(step의 리스트들) 을 실행 시킴 

### JOB 구조
#### Job - interface
* void execute(JobExecution jobExecution) 
  * Job 실행 메서드

#### AbstractJob - abstract class
* name
  * Job 이름
* restartable
  * 재시작 여부 - 기본 true
* JobRepository
  * 메타데이터 저장소
* JobExecutionListener
  * Job 이벤트 리스터
* JobParameterIncrementer
  * JobParameter 증가기
* JobParametersValidator
  * JobParameter 검증기
* SimpleStepHandler
  * Step 실행 핸들러

## JobInstance
* BATCH_JOB_INSTANCE라는 테이블에 생성됨
* Job과 JobParameter로 JobInstance가 없다면 생성
  * 있다면 있는 JobInstance를 반환
* 판별 기준은 JobName + JobKey(JobParameter 해시값)으로 판단
  * JobName = 일별정산, JobParamter = 2022.10.14
  

## JobParameter
* BATCH_JOB_EXECUTION_PARAMS 테이블에 생성됨
  * BATCH_JOB_INSTANCE라는 테이블과 1:1
* JobLauncher로 실행 시 Job과 함께 쓰임
* JobInstance 구별하기 위한 용도

### 주입 방법
* 앱 실행시
  * java -jar 스프링_프로젝트.jar parameterKey=parameterValue parameterKeyLong(long)=2L parameterKeyDate(date)=2022/10/17 parameterKeyDouble(double)=2.5 
* 코드로 실행
  * JobParameterBuilder, DefaultJobParametersConverter
* SpEL 이용
  * @JobScope, @StepScope 선언 후 @Value("#{jobParameter[jobParameterKey]}")

### Step에서 Parameter 꺼내기
```java
@Bean
public Step step1() {
    return stepBuilderFactory.get("step1")
            .tasklet(new Tasklet() {
                @Override
                public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

                    JobParameters jobParameters = contribution.getStepExecution().getJobExecution().getJobParameters();
                    // String value 꺼내기
                    String parameterKey = jobParameters.getString("parameterKey");

        // Map 형식으로 받기
        Map<String, Object> jobParameters1 = chunkContext.getStepContext().getJobParameters();
                    
                    System.out.println("step1 was executed");
                    return RepeatStatus.FINISHED;
                }
            }).build();
}
```

## JobExecution
* BATCH_JOB_EXECUTION 테이블에 생성됨
* JobInstance에 대한 한번의 시도를 의미하는 객체, Job 실행 중에 발생한 정보들을 저장하고 있는 객체
  * 시작시간, 종료시간, 상태(시작됨, 완료, 실패), 종료 상태의 속성을 가짐
* 실행 상태 결과가 COMPLETE면 재 실행 불가(완료된 것으로 판단)
  * FAILED면 완료되지 않은 것이므로 재실행 가능
* BATCH_JOB_INSTANCE와 BATCH_JOB_EXECUTION 테이블은 1:N 관계


## Step
* 독립적인 하나의 단계 실제 배치 처리를 컨트롤 하는데 필요한 모든 정보를 가지고 있는 도메인 객체

### Step - Interface
* void execution(StepExecution stepExecution);
  * Step을 실행시키는 execution 메소드
  * 실행 결과 상태는 StepExecution에 저장
### AbstractStep - abstract class
* name
  * Step 이름
* startLimit
  * Step 실행 제한 횟수
* allowStartIfComplete
  * Step 실행이 완료된 후 재 실행 여부
* stepExecutionListener
  * Step 이벤트 리스터
* jobRepository
  * Step 메타데이터 저장소
### Step 기본 구현체
* TaskletStep
  * 가장 기본이 되는 클래스, Tasklet 타입의 구현체들을 제어
```java
// 직접 생성한 Tasklet
public Step taskletStep(){
    return this.stepBuilderFactory.get("step")
        .tasklet(myTasklet())
        .build();
}

// ChunkOrientedTasklet을 실행
public Step taskletStep(){
    return this.stepBuilderFactory.get("step")
        .<Member,Member> chunk(100)
        .reader(reader())
        .writer(writer())
        .build();
}

```

* PartitionStep
  * 멀티 스레드 방식 Step을 여러개로 분리해서 실행
* JobStep
  * Step 내에서 Job을 실행
```java
public Step jobStep(){
    return this.stepBuilderFactory.get("step")
        .launcher(jobLauncher)
        .parametersExtractor(jobParametersExtractor())
        .build();
}

```
* FlowStep
  * Step 내에서 Flow를 실행하도록 한다.
```java
public Step flowStep(){
    return this.stepBuilderFactory.get("step")
        .flow(myFlow())
        .build();
}
```


## StepExecution
* BATCH_STEP_EXECUTION 테이블에 생성됨
* Step에 대한 한번의 시도를 의미하는 객체로서 Step 실행 중에 발생한 정보들을 저장하고 있는 객체
  * 시작시간, 종료시간, 상태(시작됨, 완료, 실패), commit count, rollback, count등의 속성을 가짐
* Step 이 매번 시도될 때마다 생성되며 각 Step 별로 생성된다.
  * 완료된 것은 실행 안함, 실패한 것만 재실행 됨
* 이전 단계 Step이 실패해서 현재 Step을 실행하지 않았다면 StepExecution을 실행하지 않는다. Step이 시작됐을 때만 StepExecution을 생성

### JobExecution과의 관계
* Step의 StepExecution이 모두 정상적으로 완료 되어야 JobExecution이 정상적으로 완료
* Step의 StepExecution 중 하나라도 실패하면 JobExecution은 실패
* StepExecution(N) : JobExecution(1) 

## StepContribution
* BATCH_STEP_EXECUTION 테이블에 생성됨
* 청크 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트 하는 도메인 객체
* 청크 커밋 직전에 StepExecution의 apply 메서드를 호출하여 상태를 업데이트
* ExitStatus의 기본 종료코드 외 사용자 정의 종료코드를 생성해서 적용 할 수 있음


## ExecutionContext
* DB에 직렬화된 한 값으로 저장
* 공유 범위
  * Step - StepExecution에 저장 각 Step끼리 공유 안됨
    * BATCH_STEP_EXECUTION_CONTEXT에 직렬화
  * Job - JobExecution에 저장 각 Job끼리 공유 안됨, Job과 그 안의 Step끼리 공유 가능
    * BATCH_JOB_EXECUTION_CONTEXT에 직렬화

```java
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

class ExecutionContextTasklet implements Tasklet {
  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    //Contribute에서 꺼내기
    ExecutionContext jobExecution = contribution.getStepExecution().getJobExecution().getExecutionContext();
    ExecutionContext stepExecution = contribution.getStepExecution().getExecutionContext();

    // ChunkContext에서 이름 꺼내기
    String jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobInstance().getJobName();
    String stepName = chunkContext.getStepContext().getStepExecution().getStepName();

  }
}
```


## JobRepository
* 커스터 마이징 방법
  * JDBC - JobRepositoryFactoryBean
  * In Memory - MapJobRepositoryFactoryBean
    * 테스트 용, 프로토 타입 용으로

```java

@Configuration
public class CustomBatchConfigurer extends BasicBatchConfigurer {
    private final DataSource dataSource;
    /**
     * Create a new {@link BasicBatchConfigurer} instance.
     *
     * @param properties                    the batch properties
     * @param dataSource                    the underlying data source
     * @param transactionManagerCustomizers transaction manager customizers (or
     *                                      {@code null})
     */
    protected CustomBatchConfigurer(BatchProperties properties, DataSource dataSource, TransactionManagerCustomizers transactionManagerCustomizers) {
        super(properties, dataSource, transactionManagerCustomizers);
        this.dataSource = dataSource;
    }

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(dataSource);
        jobRepositoryFactoryBean.setTransactionManager(getTransactionManager());
        jobRepositoryFactoryBean.setIsolationLevelForCreate("ISOLATION_READ_COMMITEED");
        jobRepositoryFactoryBean.setTablePrefix("SYSTEM_");
        return jobRepositoryFactoryBean.getObject();
    }
}



```

```java

@Component
public class JobRepositoryListener implements JobExecutionListener {
  @Autowired
  private JobRepository jobRepository;
  @Override
  public void beforeJob(JobExecution jobExecution) {

  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();

    JobParameters jobParameters = new JobParametersBuilder().addString("requestDate", "20221024").toJobParameters();

    /*
     * getLastJobExecution : 맨 마지막으로 저장된 JobExecution을 가져올 수 있다.
     * */

    JobExecution lastJobExecution = jobRepository.getLastJobExecution(jobName, jobParameters);
  }
}
```


## JobLauncher
* 배치 Job 실행 역할
* 동기적 실행
  * taskExecutor를 SyncTaskExecutor로 설정할 경우 (기본값은 SyncTaskExecutor)
  * JobExecute을 획득하고, 배치 처리를 최종 완료한 이후 Client에게 JobExecution을 반환
  * 스케줄러에 의한 배치 처리에 적합 함 - 배치 처리 시간이 길어도 상관없는 경우
* 비동기적 실행
  * taskExecutor가 SimpleAsyncTaskExecutor로 설정할 경우
  * JobExecution을 획득한 후 Client에게 바로 JobExecution을 반환하고 배치처리를 완료
  * Http 요청에 의한 배치처리에 적합함 - 배치 처리 시간이 길 경우 응답이 늦어지지 않도록 함
```java
@RestController
public class JobLauncherController {
    @Autowired
    private Job job;
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private BasicBatchConfigurer basicBatchConfigurer;
    
    @PostMapping("/batch")
    public String launch(@RequestBody Member member) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder().addString("id", member.getId()).addDate("date", new Date()).toJobParameters();
        jobLauncher.run(job,jobParameters);
        
        // 비동기적 실행을 할려면
        SimpleJobLauncher simpleJobLauncher = (SimpleJobLauncher) basicBatchConfigurer.getJobLauncher();
        simpleJobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        simpleJobLauncher.run(job,jobParameters);
        //
        
        return "batch compeleted";
    }
}

```
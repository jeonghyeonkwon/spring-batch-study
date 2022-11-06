# Step
```java
public Step batchStep(){
    return stepBuilderFactory.get("batchStep") // StepBuilder를 생성하는 팩토리, Step 의 이름을 매개 변수로 받음
        .tasklet(Tasklet)   // Tasklet 클래스 설정, 이 메서드를 실행하면 TaskletStepBuilder 반환
        .startLimit(10) // Step의 실행 횟수를 설정, 설정한 만큼 실행되고 초과시 오류 발생, 기본값은 INTEGER.MAX_VALUE
        .allowStartIfComplete(true) // Step의 성공, 실패와 상관없이 항상 Step을 실행하기 위한 설정
        .listener(StepExecutionListener)    // Step 라이프 사이클의 틍정 시점에 콜백 제공받도록 StepExecutionListener 설정
        .build()    // TaskletStep을 생성
}
```
## StepBuilderFactory /StepBuilder
### StepBuilderFactory
* StepBuilder를 생성하는 팩토리 클래스, get(String name) 제공
  * StepBuilderFactory.get("stepName")으로 Step 을 생성

### StepBuilder
* Step을 구성하는 설정 조건에 따라 다섯 개의 하위 빌더 클래스를 생성하고 실제 Step 생성을 위임한다
* TaskletStepBuilder
  * Tasklet을 생성하는 기본 빌더 클래스
* SimpleStepBuilder
  * TaskletStep을 생성하며 내부적으로 청크 기반의 작업을 처리하는 ChunkOrientedTasklet 클래스 생성
* PartitionStepBuilder
  * PartitionStep을 생성하며 멀티 스레드 방식으로 Job을 실행
* JobStepBuilder
  * JobStep을 생성하여 Step 안에서 Job을 실행
* FlowStepBuilder
  * FlowStep을 생성하여 Step 안에서 Flow를 실행한다

#### 순서
1. StepBuilderFactory에서 get메소드로 StepBuilder 생성
2. 구분에 따라 5개의 빌더가 생성된다
   * TaskletStepBuilder
   * SimpleStepBuilder
   * PartitionStepBuilder
   * JobStepBuilder
   * FlowStepBuilder

## TaskletStep
* 스프링 배치에서 제공하는 Step 구현체로서 Tasket을 실행시키는 도메인 객체
* ReapeatTemplate를 사용해서 Tasklet의 구문을 트랜잭션 경계 내에서 반복해서 실행
* Task 기반과 Chunk 기반으로 나누어서 Tasklet을 실행

### Task VS Chunk 기반 비교 (Step의 실행 단위 2가지)
* Task 기반
  * ItemReader 와 ItemWriter와 같은 청크 기반의 작업보다 단일 작업 기반으로 처리 되는 것이 더 효율적
  * 주로 Tasklet 구현체를 만들어 사용
  * 순서
    1. Job -> TaskletStep -> RepeatTemplate ->
    2. Tasklet -> Business Logic 반복
```java
public Step step(){
    return this.stepBuilderFactory.get("step")
        .tasklet(myTasklet())
        .build();
}
```

* Chunk 기반
  * 큰 덩어리를 n개씩 나누어 실행한다는 뜻
  * ItemReader,ItemProcessor, ItemWriter을 사용하며 ChunkOrientedTasklet 구현체 사용
  * 순서
      1. Job -> TaskletStep -> RepeatTemplate ->
      2. ChunkOrientedTasklet -> ItemReader OR ItemProcessor OR ItemWriter 반복
```java
public Step step(){
    return this.stepBuilderFactory.get("step")
        .<String, String> chunk(100)
        .reader().writer().build();
}
```

### tasklet()
* Tasklet 타입의 클래스 정의
  * Tasklet
    * Step 내에서 구성되고 실행되는 도메인 객체로서 주로 단일 테스크를 수행하기 위한 것
    * TaskletStep에 의해 반복적으로 수행되며 반환값에 따라 계속 수행 혹은 종료한다
    * RepeatStatus - Tasklet의 반복 여부 상태값
      * RepeatStatus.FINISHED - Tasklet 종료, return 을 null로 반환한 것과 동일
      * RepeatStatus.CONTINUABLE - Tasklet 반복
  * 이 메소드가 실행되면 TaskletStepBuilder가 반환
  * Step에 오직 하나의 Tasklet 설정이 가능하다
  * 익명 클래스 보다 구현해서 사용하는 것이 낫다
```java
public class CustomTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        contribution.getStepExecution().getStepName();
        chunkContext.getStepContext().getJobName();
        return RepeatStatus.FINISHED;
    }
}

```
#### 구조
```java
RepeatStatus execute(StepContribution, ChunkContext);
```


### startLimit()
* step 실행 횟수를 조정
* Step 마다 설정할 수 있다
* 초과하면 예외 발생(StartLimitExceededException 발생)
* start-limit의 기본은 Integer.MAX_VALUE
### allowStartIfComplete(Boolean)
* 재시작 가능한 job에서 Step의 이전 성공 여부와 상관없이 항상 step을 실행하기 위한 설정
* 실행 마다 유효성 검증하는 Step이나 사전 작업이 꼭 필요한 Step
```java
public Step step(){
    return this.stepBuilderFactory.get("step")
        .tasklet(myTasklet())
        .startLimit(10)
        .allowStartIfComplete(true)
        .build();
}
```

## JobStep
* Job 안에 Step인데 그 Step이 외부의 Job을 포함하고 있는것
  * Job1 -> Step -> Job2 이런 식
* 외부 Job이 실패하면 해당 Step이 실패하므로 최종 Job은 실패
* 커다란 시스템을 작은 모듈로 쪼개고 job의 흐름을 관리할 때 사용
* StepBuilderFactory > StepBuilder > JobStepBuilder > JobStep
```java
public Step jobStep(){
    return stepBuilderFactory.get("jobStep") // StepBuilder를 생성하는 팩토리, Step의 이름을 매개변수로 받음
        .job(Job)   // JobStep내 에서 실행 될 Job 설정, JobStepBuilder 반환
        .launcher(JobLauncher)  // Job을 실행할 JobLauncher설정
        .parametersExtractor(JobParametersExtractor)    // Step의 ExecutionContext를 Job이 실행되는 데 필요한 JobParameters로 변환
        .build();   // JobStep을 생성
}
```
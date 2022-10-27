# Job

## 초기화 설정

### BatchProperties
* Spring Batch의 환경 설정 클래스
```yaml
spring:
  batch:
    job:
      names: ${job.name:NONE}
    initialize-schema: NEVER
    tablePrefix: SYSTEM
```
* 실행 할때 옵션 주기(지정한 Job만 주기)
```shell
--job.name=helloJob
--job.name=helloJob,simpleJob
```

## JobBuilderFactory
* JobBuilder를 생성하는 팩토리 클래스 get(String name)제공
* JobBuilder
  * Job을 구성하는 설정 조건에 따라 두 개의 하위 빌더 클래스를 생성하고 실제 Job생성을 위임
  * SimpleJobBuilder
    * SimpleJob을 생성하는 Builder클래스
    * Job 실행과 관련된 여러 설정 API 제공
  * FlowJobBuilder
    * FlowJob을 생성하는 Builder
    * 내부적으로 FlowBuilder를 반환함으로써 Flow 실행과 관련된 여러 설정 API 제공

## SimpleJob
* SimpleJob은 Step을 실행시키는 Job구현체로서 SimpleJobBuilder에 의해 생성된다
* 여러 단계의 Step으로 구성할 수 있으며 Step을 순차적으로 실행
  * 그 Step이 실패 하면 다음 Step은 실행되지 않는다.
```java
jobBuilderFactory.get("batchJob") // JobBuilder를 생성하는 팩토리, Job의 이름을 매개변수로 받음
        .start(Step)    // 처음 실행 할 Step 설정, 최초 한번 설정, 이 메서드를 실행하면 SimpleJobBuilder 반환
        .next(Step)     // 다음 실행 할 Step 설정
        .incrementer(JobParametersIncrementer) // JobParameter의 값을 자동으로 증가해 주는 JobParametersIncrementer 설정
        .preventRestart(true)   // Job의 재시작 가능 여부, 기본 true
        .validator(JobParameterValidator)   //JobParameter를 실행하기 전에 올바른 구성이 되었는지 검증하는 JobParametersValidator 설정
        .listener(JobExecutionListener)    // Job 라이프 사이클의 특정 시점에 콜백 제공 받도록 JobExecutionListener 설정
        .build();   // SimpleJob 생성
```
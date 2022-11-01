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

### TaskletStep
* 스프링 배치에서 제공하는 Step 구현체로서 Tasket을 실행시키는 도메인 객체
* ReapeatTemplate를 사용해서 Tasklet의 구문을 트랜잭션 경계 내에서 반복해서 실행
* Task 기반과 Chunk 기반으로 나누어서 Tasklet을 실행

#### Task VS Chunk 기반 비교 (Step의 실행 단위 2가지)
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
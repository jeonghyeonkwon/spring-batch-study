# Step

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


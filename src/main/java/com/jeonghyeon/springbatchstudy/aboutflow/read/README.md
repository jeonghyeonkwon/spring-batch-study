# Flow
* Job과 Step은 고정되고, 정해진 패턴만 구성할 때 사용
* Flow는 복잡하고 유연하게 흐름을 구성할 수 있다.

```java
public Job batchJob(){
    return jobBuilderFactory.get("batchJob")
        .start(Step) // Flow 시작하는 Step 설정
        .on(String pattern) // Step의 실행 결과로 돌려받는 종료상태 (ExitStatus)를 캐치하여 매칭하는 패턴, TransitionBuilder 반환
        .to(Step)   // 다음으로 이동할 Step 지정
        .stop / fail() / end() / stopAndRestart() // Flow를 중지 / 실패 / 종료 하도록 Flow 종료
        .from(Step) // 이전 단계에서 정의한 Step의 Flow를 추가적으로 정의함
        .next(Step) // 다음으로 이동할 Step 지정
        .end()  // build() 앞에 위치하면 FlowBuilder를 종료하고 SimpleFlow 객체 생성
        .build(); // FlowJob 생성하고 flow 필드에 SimpleFlow 저장
}
```
## FlowJob
* Step을 순차적으로만 구성하는 것이 아닌 특정한 상태에 따라 흐름을 전환하도록 구성, FlowJobBuilder에 의해 생성
  * Step이 실패 하더라도 Job은 실패로 끝나지 않도록 해야 하는 경우
  * Step이 성공 했을 떄 다음에 실행해야 할 Step을 구분해서 실행 해야 하는 경우
  * 특정 Step은 전혀 실행되지 않게 구성 해야 하는 경우
* Flow와 Job의 흐름을 구성하는데만 관여하고 실제 비즈니스 로직은 Step에서 이루어진다
* 내부적으로 SimpleFlow 객체를 포함하고 있으며 Job 실행 시 호출한다

## 순서
* JobBuilderFactory > JobBuilder > JobFlowBuilder > FlowBuilder > FlowJob
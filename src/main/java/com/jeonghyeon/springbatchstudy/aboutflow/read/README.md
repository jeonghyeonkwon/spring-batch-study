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

### start() / next()
* start()
  * 파라미터로 Flow로 받으면 JobFlowBuilder로 반환
  * 파라미터로 Step로 받으면 SimpleJobBuilder로 반환
* next()
  * Step, Flow, JobExcutionDecider가 올 수 있다.

## Transition
### BatchStatus
* JobExecution과 StepExecution의 속성으로 Job과 Step의 종료 후 최종 상태
* SimpleJob
  * 마지막 Step의 값을 최종 Step값으로 반영
  * Step이 실패할 경우 해당 Step이 마지막 Step이 된다
* FlowJob
  * Flow내 Step의 ExitStatus 값을 FlowExecutionStatus 값으로 저장
  * 마지막 Flow의 FlowExecutionStatus 값을 Job의 최종 BatchStatus 값으로 반영
* COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKOWN 의 열거 타입이 있다
  * ABANDONED는 처리는 완료했지만 성공하지 못한 단계 재시작 안하고 건너뜀
### ExitStatus
* JobExecution과 StepExecution의 속성으로 Job과 Step의 실행 후 어떤 상태로 종료 되었는지 정의
* 기본은 BatchStatus와 ExitStatus는 동일
* SimpleJob
  * 마지막 Step의 ExitStatus 값을 Job의 최종 ExitStatus 값으로 반열
* FlowJob
  * Flow 내 Step의 ExitStatus 값을 FlowExecutionStatus 값으로 저장
  * 마지막 flow의 FlowExecutionStatus 값을 Job의 최종 ExitStatus 값으로 반영
* UNKNOWN, EXECUTING, COMPLETED, NOOP, FAILED, STOPPED

#### FlowExecutionStatus
* FlowExecution의 속성으로 Flow의 실행 후 최종 결과 상태가 무엇인지 정의
* COMPLETED, STOPPED, FAILED, UNKNOWN
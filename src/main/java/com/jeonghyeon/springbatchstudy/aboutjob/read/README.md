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
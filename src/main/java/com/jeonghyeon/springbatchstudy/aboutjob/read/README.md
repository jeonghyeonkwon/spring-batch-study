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

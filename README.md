# 스프링 배치 관련 공부한 내용 정리
## 출처
[인프런 - 스프링 배치 - Spring Boot 기반으로 개발하는 Spring Batch](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EB%B0%B0%EC%B9%98)

## 배치 핵심 패턴
* Read - 데이터베이스, 파일, 큐에서 다량의 데이터 조회
* Process - 특정 방법으로 데이터를 가공
* Write - 데이터를 수정된 양식으로 다시 저장

## 배티 아키텍처

### Application
* 개발자가 만든 모든 Job과 커스텀 코드


### Batch Core
* Job을 실행, 모니터링, 관리하는 API
* JobLauncher, Job, Step, Flow 등 속한다

### Batch Infrastructure
* Application, Batch Core가 여기서 빌드 된다.
* 실질적인 데이터 처리 담당
* Reader, Processor, Writer, Skip, Retry 등 속한다. 




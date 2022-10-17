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
* void execute(JobExecution) 
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
  
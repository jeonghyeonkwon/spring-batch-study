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

### @EnableBatchProcessing 

#### 어노테이션 실행 순서
1. SimpleBatchConfiguration
2. BatchConfigurerConfiguration
   * BasicBatchConfigurer
   * JpaBatchConfigurer
3. BatchAutoConfiguration

### 기본 Batch 실행 순서
* Job -> Step -> Tasklet

### Spring Batch Schema
#### Job 관련 테이블
* BATCH_JOB_INSTANCE
  * Job이 실행될 때 JobInstance 정보 저장
  * 동일한 job_name과 job_key로 중복 저장될 수 없다
  * 필드
    * JOB_INSTANCE_ID 
      * 기본키 
    * VERSION 
      * 업데이트 될 때 마다 1 증가
    * JOB_NAME 
      * Job을 구성할 때 부여하는 Job의 이름
    * JOB_KEY 
      * job_name과 jobParameter를 합쳐 해싱한 값
* BATCH_JOB_EXECUTION
  * job 의 실행정보가 저장되며 Job 생성, 시작, 종료 시간, 실행 상태, 메시지 등을 관리
  * 필드
    * JOB_EXECUTION_ID
      * JOB_EXECUTION을 식별할 수 있는 고유 키
    * VERSION
      * 업데이트 될 때 마다 1 증가
    * JOB_INSTANCE_ID
      * BATCH_JOB_INSTANCE의 키 저장 
    * CREATE_TIME
      * 실행이 생성된 시점 저장
    * START_TIME
      * 실행이 시작된 지점 저장
    * END_TIME
      * 실행이 끝난 지점 저장
      * 만약 오류로 중지 된다면 저장하지 않을 수 있음
    * STATUS
      * 실행 상태(COMPLETED, FAILED, STOPPED, ...)
    * EXIT_CODE
      * 실행 종료 코드 저장 (COMPLETED, FAILED,...)
    * EXIT_MESSAGE
      * STATUS가 실패일 경우 에러 메시지
    * LAST_UPDATED
      * 마지막 실행 시점을 저장
* BATCH_JOB_EXECUTION_PARAMS
  * Job과 함께 실행되는 JobParameter 정보를 저장
  * 필드
    * JOB_EXECUTION_ID
      * JOB_EXECUTION 식별 키
    * TYPE_CD
      * STRING, LONG, DATE, DOUBLE의 타입 정보
    * KEY_NAME
      * 파라미터 키 값
    * STRING_VAL
      * 파라미터 VALUE
    * DATE_VAL
      * 파라미터 VALUE
    * LONG_VAL
      * 파라미터 VALUE
    * DOUBLE_VAL
      * 파라미터 VALUE
    * IDENTIFYING
      * 식별 여부 (TRUE, FALSE)
* BATCH_JOB_EXECUTION_CONTEXT
  * Job의 실행 동안 여러가지 상태정보, 공유 데이터를 직렬화(Json 형식) 해서 저장
  * Step 간 서로 공유 가능함
  * 필드
    * JOB_EXECUTION_ID 
      * JOB_EXECUTION 식별 키
    * SHORT_CONTEXT
      * JOB의 실행 상태 정보, 공유 데이터 등의 정보를 문자열로 저
    * SERIALIZED_CONTEXT
      * 직렬화된 전체 컨텍스트
#### Step 관련 테이블
* BATCH_STEP_EXECUTION
  * Step의 실행정보가 저장되며 생성, 시작, 종료 시간, 실행 상태, 메시지 등을 관리
  * 필드
    * STEP_EXECUTION_ID
      * STEP을 식별할 수 있는 고유 키
    * VERSION
      * 업데이트 될 떄마다 1씩 증가
    * STEP_NAME
      * STEP의 이름
    * JOB_EXECUTION_ID
      * JobExecution의 기본키
    * START_TIME
      * 실행이 시작된 시점 
    * END_TIME
      * 실행이 종료된 시점
      * 만약 오류로 중지 된다면 저장하지 않을 수 있음
    * STATUS
      * 실행 상태를 저장 (COMPLETED, FAILED, STOPPED, ...)
    * COMMIT_COUNT
      * 트랜잭션당 커밋된 수를 저장
    * READ_COUNT
      * 실행도중 READ한 Item 수
    * FILTER_COUNT
      * 실행도중 필터링한 Item 수
    * WRITE_COUNT
      * 실행도중 저장하고 커밋한 Item 수 
    * READ_SKIP_COUNT
      * 실행도중 READ가 Skip한 Item 수
    * WRITE_SKIP_COUNT
      * 실행도중 Write가 Skip한 Item 수
    * PROCESS_SKIP_COUNT
      * 실행도중 Process가 Skip한 Item 수
    * ROLLBACK_COUNT
      * 실행도중 Rollback이 일어난 Item 수
    * EXIT_CODE
      * 실행 종료 코드를 저장(COMPLETED, FAILED, ...)
    * EXIT_MESSAGE
      * Status가 실패한 원인등 내용 저장
    * LAST_UPDATED
      * 마지막 실행 시점
* BATCH_STEP_EXECUTION_CONTEXT
  * Step의 실행 동안 여러가지 상태정보, 공유 데이터를 직렬화(Json 형식) 해서 저장
  * Step 별로 저장되며 Step 간 서로 공유할 수 없음
  * 필드
    * STEP_EXECUTION_ID
      * StepExecution 식별키
    * SHORT_CONTEXT
      * Step 실행 상태정보, 공유 데이터 등의 정보를 문자열로 저장
    * SERIALIZED_CONTEXT
      * 직렬화된 전체 컨텍스트
```sql

CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT ,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT  ,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME DATETIME(6) NOT NULL,
	START_TIME DATETIME(6) DEFAULT NULL ,
	END_TIME DATETIME(6) DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED DATETIME(6),
	JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	TYPE_CD VARCHAR(6) NOT NULL ,
	KEY_NAME VARCHAR(100) NOT NULL ,
	STRING_VAL VARCHAR(250) ,
	DATE_VAL DATETIME(6) DEFAULT NULL ,
	LONG_VAL BIGINT ,
	DOUBLE_VAL DOUBLE PRECISION ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	START_TIME DATETIME(6) NOT NULL ,
	END_TIME DATETIME(6) DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT ,
	READ_COUNT BIGINT ,
	FILTER_COUNT BIGINT ,
	WRITE_COUNT BIGINT ,
	READ_SKIP_COUNT BIGINT ,
	WRITE_SKIP_COUNT BIGINT ,
	PROCESS_SKIP_COUNT BIGINT ,
	ROLLBACK_COUNT BIGINT ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED DATETIME(6),
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
	ID BIGINT NOT NULL,
	UNIQUE_KEY CHAR(1) NOT NULL,
	constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
	ID BIGINT NOT NULL,
	UNIQUE_KEY CHAR(1) NOT NULL,
	constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_SEQ (
	ID BIGINT NOT NULL,
	UNIQUE_KEY CHAR(1) NOT NULL,
	constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_SEQ);

```
# Chapter5 JobRepository와 메타데이터

## jobRepository

- 잡이 실행될 때 잡의 상태를 저장해 관리한다.
- 잡의 재시작 또는 아이템 재처리 시 어떤 동작을 수행할지 JobRepository의 정보를 사용해 결정한다.
- 잡이 처리되는 데 걸리는 시간이나 오류로 인해 재시도된 앙템 수와 같은 배치 실행 추세분만 아니라, 잡이 어디까지 실행됐는지를 파악할 수 있다.(모니터링 영역)

</br >

## JobRepository 스키마

![image](https://user-images.githubusercontent.com/43977617/138586709-f7c7e641-0aa5-4d10-ae2b-853363e66dfa.png)

</br >

### BATCH_JOB_INSTANCE

- 스키마의 실제 시작점
- 잡의 논리적 실행을 나타낸다.

| 필드             | 설명                                                      |
| ---------------- | --------------------------------------------------------- |
| JOB_EXECUTION_ID | 기본 키                                                   |
| VERSION          | 낙관적 락에 사용되는 레코드 버전                          |
| JOB_NAME         | 실행된 잡의 이름                                          |
| JOB_KEY          | 잡 이름과 잡 파라미터의 해시 값,<br />JobInstance 식별 값 |

</br >

### BATCH_JOB_EXECUTION

- 배치 잡의 실제 실행 기록을 나타낸다.
- 잡실 실행될 때마다 새 레코드가 해당 테이블에 생성되고, 잡이 진행되는 동안 주기적으로 업데이트된다.

| 필드             | 설명                                      |
| ---------------- | ----------------------------------------- |
| JOB_EXECUTION_ID | 기본 키                                   |
| VERSION          | 낙관적 락에 사용되는 레코드 버전          |
| JOB_INSTANCE_ID  | BATCH_JOB_INSTANCE 외래 키                |
| CREATE_TIME      | 레코드 생성 시간                          |
| START_TIME       | 잡 실행 시작 시간                         |
| END_TIME         | 잡 실행 완료 시간                         |
| STATUS           | 잡 실행의 배치 상태                       |
| EXIT_CODE        | 잡 실행 종료 코드                         |
| EXIT_MESSAGE     | EXIT_CODE와 관련된 메시지나 스택 트레이스 |
| LAST_UPDATED     | 레코드 마지막 갱신 시간                   |

</br >

### BATCH_JOB_EXECUTION_CONTEXT

- JobExecution의 ExecutionContext를 저장하는 곳

| 필드               | 설명                                    |
| ------------------ | --------------------------------------- |
| JOB_EXECUTION_ID   | 기본 키                                 |
| SHORT_CONTEXT      | 트림 처리된(trimmed) SERIALIZED_CONTEXT |
| SERIALIZED_CONTEXT | 직렬화된 ExecutionContext               |

</br >

### BATCH_JOB_EXECUTION_PARAMS

- 잡이 실행될 때마다 사용된 잡 파리미터 저장

| 필드             | 설명                                   |
| ---------------- | -------------------------------------- |
| JOB_EXECUTION_ID | 기본 키                                |
| TYPE_CODE        | 파라미터 값의 타입을 나타내는 문자열   |
| KEY_NAME         | 파라미터 이름                          |
| STRING_VAL       | String 타입일 경우 파라미터 값         |
| DATE_VAL         | Date 타입일 경우 파라미터 값           |
| LONG_VAL         | Long 타입일 경우 파라미터 값           |
| DOUBLE_VAL       | Double 타입일 경우 파라미터 값         |
| IDENTIFYING      | 파라미터가 식별 여부를 나타내는 플래그 |

</br >

### BATCH_STEP_EXECUTION

- 스텝의 시작, 완료, 상태에 대한 메타데이터 저장
- 스텝 분석이 가능하도록 다양한 횟수 값(읽기, 처리, 쓰기) 값 저장

| 필드               | 설명                                                     |
| ------------------ | -------------------------------------------------------- |
| STEP_EXECUTION_ID  | 기본 키                                                  |
| VERSION            | 낙관적 락에 사용되는 레코드 버전                         |
| STEP_NAME          | 스텝 이름                                                |
| JOB_EXECUTION_ID   | BATCH_JOB_EXECUTION_ID 외래 키                           |
| START_TIME         | 스텝 실행 시작 시간                                      |
| END_TIME           | 스텝 실행 완료 시간                                      |
| STATUS             | 스텝 배치 상태                                           |
| COMMIT_COUNT       | 스텝 실행 중에 커밋된 트랜잭션 수                        |
| READ_COUNT         | 읽은 Item 수                                             |
| FILTER_COUNT       | ItemProcessor가 null을 반환해 필터링된 아이템 수         |
| WRITE_COUNT        | 기록된 Item 수                                           |
| READ_SKIP_COUNT    | ItemReader 내에서 예외가 던져졌을 때 건너뛴 아이템 수    |
| PROCESS_SKIP_COUNT | ItemProcessor 내에서 예외가 던져졌을 때 건너뛴 아이템 수 |
| WRITE_SKIP_COUNT   | ItemWriter 내에서 예외가 던져졌을 때 건너뛴 아이템 수    |
| ROLLBACK_COUNT     | 스텝 실행시 롤백된 트랜잭션 수                           |
| EXIT_CODE          | 스텝 실행 종료 코드                                      |
| EXIT_MESSAGE       | 스텝 실행시 반환된 메시지나 스택 트레이스                |
| LAST_UPDATED       | 레코드 마지막 갱신 시간                                  |

</br >

### BATCH_STEP_EXECUTION_CONTEXT

- StepExecution의 ExecutionContext는 스텝 수준에서 컴포넌트의 상태를 저장

| 필드               | 설명                                    |
| ------------------ | --------------------------------------- |
| STEP_EXEUCTION_ID  | 기본 키                                 |
| SHORT_CONTEXT      | 트림 처리된(trimmed) SERIALIZED_CONTEXT |
| SERIALIZED_CONTEXT | 직렬화된 ExecutionContext               |


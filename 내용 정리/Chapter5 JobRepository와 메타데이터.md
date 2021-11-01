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

</br >

## BatchConfigurer 인터페이스

`BatchConfigurer` 인터페이스는 스프링 배치 인프라스트럭처 컴포넌트의 구성을 커스터마이징하는 데 사용되는 전략 인터페이스이다.

- `@EnableBatchProcessing` 애너테이션을 적용하면 스프링 배치는 `BatchConfigurer` 인터페이스를 사용해 프레임워크에서 사용되는 각 인프라스트럭처 컴포넌트의 인스턴스를 얻는다.
- 먼저 `BatchConfigurer` 구현체에서 빈을 생성한다.
- 그다음 `SimpleBatchConfiguration`에서 스프링의 `ApplicationContext`에 생성한 빈을 등록한다.

**여기서 노출되는 컴포넌트의 커스터마이징이 필요하다면 일반적으로 `BatchConfigurer`에서 커스터마이징하면 된다.**

![image](https://user-images.githubusercontent.com/43977617/139520156-3a9a7172-9805-4ef7-9a67-3489cc174f7c.png)

- `PlatformTransactionManager`: 프레임워크가 제공하는 모든 트랜잭션 관리 시에 스프링 배치가 사용되는 컴포넌트
- `JobExplorer`: `JobRepository`의 데이터를 읽기 전용으로 볼 수 있는 기능을 제공

대부분 이 모든 인터페이스를 직접 구현할 필요 없다.

스프링 배치가 제공하는 `DefaultBatchConfigurer`를 사용하면 앞서 언급했던 컴포넌트에 대한 모든 기본 옵션이 제공된다.

**일반적으로 여러 컴포넌트 중 한두 개의 구성만 재정의하므로, `DefaultBatchConfigurer`를 상속해 원하는 컴포넌트만 재정의하면 된다.**

</br >

## JobRepository 커스터마이징

`JobRepository`를 커스터마이징 하려면 `createJobRespository()` 메서드를 재정의해야 하는데 일반적으로 ApplicationContext에 두 개 이상의 데이터 소스가 존재하는 경우 사용한다.

`JobRepository`는 `JobRepositoryFactoryBean`이라는 `FactoryBean`을 통해 생성된다. 즉, `JobRepositoryFactoryBean`을 통해 `JobRepository`를 생성해주면 된다.

```java
@RequiredArgsConstructor
public class CustomBatchConfigurer extends DefaultBatchConfigurer {

    @Qualifier("repositoryDataSource")
    private final DataSource dataSource;

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        factoryBean.setDatabaseType(DatabaseType.MYSQL.getProductName());

        // 테이블 접두어 변경 (default: BATCH_)
        factoryBean.setTablePrefix("FOO_");

        // 트랜잭션 격리 레벨 변경 (default: ISOLATION_SERIALIZED)
        factoryBean.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ");

        factoryBean.setDataSource(this.dataSource);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}
```

위 예제에서 `repositoryDataSource`라는 데이터 소스는 자동와이어링된다. 이때 `repositoryDataSource`라는 `DataSource` 타입의 빈이 `ApplicationContext`의 어딘가에 있다고 가정한다.

create 로 시작하는 이름을 가진 메서드 중 어느 것도 스프링 컨테이너가 빈 정의로 직접 호출하지 않는다. 그렇기 때문에 `InitializingBean.afterPropertiesSet()` 및 `FactoryBean.getObject()` 메서드를 호출해줘야 한다.

</br >

## TransactionManager 커스터마이징

`BatchConfigurer.getTransactionManager()` 메서드를 호출하면 배치 처리에 사용할 목적으로 어딘가에 정의해둔 `PlatformTransacionmanager`가 명시적으로 반환된다.

```java
@RequiredArgsConstructor
public class CustomBatchConfigurer extends DefaultBatchConfigurer {

    @Qualifier("batchTransactionManager")
    private final PlatformTransactionManager transactionManager;

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}
```

위 예제에서 `TransactionManager`를 명시적으로 반환했다.

그 이유는 `TransactionManager`가 생성되지 않은 경우에는 `DefaultBatachConfigurer`가 기본적으로 `setDataSource` 수정자 내에서 `DataSourceTransactionManager`를 자동으로 생성하기 때문이다.

![image](https://user-images.githubusercontent.com/43977617/139659300-cb6eb8fd-3409-4b3a-a1ce-dfa7fc19e338.png)

</br >

## JobExplorer 커스터마이징

`JobExplorer`는 배치 메타데이터를 읽기 전용으로 제공한다.

기본적인 데이터 접근 계층은 `JobRepository`와 `JobExplorer` 간의 공유되는 동일한 공통 DAO 집합이다. 그러므로  `JobRepository`나  `JobExplorer` 를 커스터마이징할 때 데이터베이스로부터 데이터를 읽어들이는 데 사용되는 모든 애트리뷰트는 동일하다.

```java
@Override
protected JobExplorer createJobExplorer() throws Exception {
    JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
    factoryBean.setDataSource(this.dataSource);
    factoryBean.setTablePrefix("FOO_");
    factoryBean.afterPropertiesSet();
    return factoryBean.getObject();
}
```

위 예제를 보면 JobRepository를 커스터마이징했던 것과 비슷한 것을 느낄 수 있다. DataSource, 테이블 접두어 등의 구성을 구성했다.

> JobRepository와 JobExplorer는 동일한 데이터 저장소를 사용하므로 둘 중 하나만 커스터마이징하기보다는 둘 다 커스터마이징하는게 좋다.

</br >

## JobLauncher 커스터마이징

`JobLauncher`는 스프링 배치 잡을 실행하는 진입점이다.

스프링 부트는 기본적으로 스프링 배치가 제공하는 `SimpleJobLauncher`를 사용한다. **그러므로 스프링 부트의 기본 메커니즘으로 잡을 실행할 때 대부분 `JobLauncher`를 커스터마이징할 필요가 없다.**

만약 별도의 방식으로 잡을 구동하는 방법을 외부에서 제공하려고 할 때 `SimpleJobLauncher`의 동작 방식을 커스터마이징하면 된다.

![image](https://user-images.githubusercontent.com/43977617/139661064-7f964db3-49ae-48be-a10a-7962fa090c75.png)

`DefaultBatchConfigurer`에는 위와같이 적혀있다. 이 메서드를 재정의해서 `JobLauncher`를 커스터마이징하면 된다.


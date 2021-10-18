# Chapter4 잡과 스텝 이해하기

## 잡(Job)

잡은 처음부터 끝까지 독립적으로 실행할 수 있는 고유하며 순서가 지정된 여러 스텝의 목록

### 특징

- 유일하다.
  - 스프링 배치의 잡은 자바나 XML을 사용해 구성하며, 구성한 내용을 재사용할 수 있다.
  - 즉, 잡을 여러번 실행하기 위해 동일한 잡을 여러번 정의할 필요가 없다.
- 순서를 가진 여러 스텝의 목록이다.
  - 모든 스텝을 논리적인 순서대로 실행할 수 있도록 구성한다.
  - 예로 거래 정보를 불러오기 전에 고객의 거래명세서를 생성할 수 없다. 거래 내용을 잔액에 적용할 때까지 계좌의 잔액을 계산할 수 없다.
- 처음부터 끝까지 실행 가능하다.
  - 잡은 **외부 의존성 없이 실행**할 수 있는 일련의 스텝이다.
  - 즉, 완료 상태에 도달할 때까지 **추가적인 상호작용 없이 실행하는 처리**이다.
- 독립적이다.
  - 각 배치 잡은 외부 의존성의 영향을 받지 않고 실행할 수 있어야 하지만 잡이 의존성을 가질 수 없다는 것을 의미하지 않는다. 잡은 이러한 의존성을 관리할 수 있어야 한다.
  - 만약 파일이 없다면 오류를 자연스럽게 처리하고 파일이 전달될 떄까지 기다리지 않는다.

</br >

## 잡 러너

잡의 실행을 잡 러너에서 시작된다. 잡 러너는 잡 이름과 여러 파라미터를 받아들여 잡을 실행시키는 역할을 한다.

### CommandLineJobRunner

- CommandLineJobRunner는 스크립트를 이용하거나 명령행에서 직접 잡을 실행할 때 사용한다.
- CommandLineJobRunner는 스프링을 부트스트랩하고, 전달받은 파라미터를 사용해 요청된 잡을 실행한다.

### JobRegistryBackgroundJobRunner

- JobRegistryBackgroundJobRunner는 스프링이 부트스트랩될 떄 실행 가능한 잡을 가지고 있는 JobRegistry를 생성한다.

뒤 두 잡 러너를 통해 잡을 시작할 수 있다. 또 다른 방법으로는 `JobLauncherCommandLineRunner`를 사용해 잡을 시작할 수 있다. 이 `CommandeLineRunner`구현체는  별도의 구성이 없다면 기본적으로 `ApplicationContext`에 정의된 job 타입의 모든 빈을 기동 시에 실행한다.

</br >

## JobInstance

- 배치 잡이 실행되면 `JobInstance`가 생성된다.
- `JobInstance`는 잡 이름과 잡 파라미터로 식별할 수 있다.
- `JobInstance`는 한 번 성공적으로 완료되면 다시 실행시킬 수 없다. 즉, 동일한 잡 이름과 잡 파라미터를 사용하는 잡은 한 번만 실행할 수 있다.
- `JobInstance`를 식별하기 위해 `JobRepository`가 사용하는 데이터베이스 `BATCH_JOB_INSTANCE`와 `BATCH_JOB_EXECUTION_PARAMS` 테이블을 사용한다,
- `JobExecution`은 잡 실행의 실제 시도를 의미한다. 잡이 처음부터 끝까지 단번에 실행 완료됐다면 `JobInstance`와 `JobExecution`은 하나씩만 존재한다.
- 첫 번째 잡 실행 후 오류 상태로 종료됐다면, 해당 `JobInstance`를 실행하려고 시도할 때마다 새로운 `JobExecution`이 생성된다.
- 각 `JobExecution`은 `BATCH_JOB_EXECUTION` 테이블의 레코드로 지정된다. 또, `JobExecution`이 실행될 때의 상태는 `BATCH_JOB_EXECUTION_CONTEXT` 테이블에 저장된다.

</br >

## 유효성 검증

스프링 배치에서 잡 파라미터를 검증할 수 있는 방법이 여러개 존재한다.

### JobParametersValidator

첫 번째 방법으로 `JobParametersValidator` 인터페이스를 구현하고 해당 구현체를 잡 내에 구성하면 된다. 이 방법은 

~~~java
public class ParameterValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String fileName = parameters.getString("fileName");

        // fileName 파라미터가 있는지 check
        if (!StringUtils.hasText(fileName)) {
            throw new JobParametersInvalidException("fileName parameter is missing");
            // 파일이 .csv로 끝나는지 check
        } else if (!StringUtils.endsWithIgnoreCase(fileName, "csv")) {
            throw new JobParametersInvalidException("fileName parameter does not use the csv file extension");
        }
    }
}
~~~

위 코드에서는 두 가지를 검증한다.

1. fileName 파라미터가 존재하는지
2. 해당 파일이 `.csv`로 끝나는지

둘 중 하나라도 실패하게 된다면 `JobParametersInvalidException`이 발생할 것이다.

</br >

### DefaultJobParameterValidator

두 번째 방법은 `DefaultJobParameterValidator`를 사용하여 모든 필수 파라미터가 누락없이 전달됐는지 확인할 수 있다.

`DefaultJobParameterValidator`에는 `requiredKeys`와 `opetionalKeys`메서드가 있다. 이를 통해 필수 파라미터 목록과 필수가 아닌 파라미터 목록의 정의할 수 있다.

~~~java
public JobParametersValidator validator() {
    DefaultJobParametersValidator validator = new DefaultJobParametersValidator();

    // 필수 파라미터
    validator.setRequiredKeys(new String[]{"fileName"});

    // 필수가 아닌 파라미터
    validator.setOptionalKeys(new String[]{"name"});

    // 위에서 정의한 두 가지 파라미터 외에 변수가 전달되면 검증 실패
    // 만약에 옵션 키가 구성돼 있지 않고 필수 키만 구성돼 있다면, 필수 키를 전달하기만 하면 그 외 어떤 키를 조합하더라도 유효성 검증 통과
    return validator;
 }
~~~

위 코드에서는 `fileName`을 필수 파라미터로 구성했고, `name`은 옵션 파라미터로 구성했다.

따라서 `fileName`을 잡 파라미터로 전달하지 않으면 오류가 발생한다. 또한, name을 옵션 파라미터로 구성하였는데, 여기서 `fileName`과 `name`외의 파라미터를 전달하면 오류가 발생한다.

만약 옵션 파라미터가 구성돼 있지 않고, 필수 파라미터만 구성돼 있으면, 필수 파라미터를 전달하기만 하면 그 외 어떤 파라미터를 전달해도 검증을 통과한다.

</br >

### CompositeJobParametersValidator

`JobBuilder`의 메서드는 하나의 `JobParameterValidator` 인스턴스만 지정하게 돼 있다. 만약 두 개의 유효성 검증을 하고 싶다면 `CompositeJobParametersValidator`를 사용하면 된다.

아래 예제는 두 개의 유효성 검증을 할 수 있도록 변경되 잡 구성이다.

```java
@Bean
public CompositeJobParametersValidator compositeValidator() {
    final CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
  
    DefaultJobParametersValidator defaultValidator = new DefaultJobParametersValidator();
    validator.setRequiredKeys(new String[]{"fileName"});
    validator.setOptionalKeys(new String[]{"name"});
    defaultValidator().afterPropertiesSet();

    validator.setValidators(
            Arrays.asList(new ParameterValidator(),
                    defaultValidator));
  
    return validator;
}
```

</br >

## 잡 파리미터 증가시키기

동일한 파라미터를 사용해 동일한 잡을 두 번 수행했을 때 예외가 발생한다.

### SimpleJobLauncher

![image](https://user-images.githubusercontent.com/43977617/137686062-45a9b034-3a22-47ee-bd25-1c6e685d2d62.png)

위 코드는 `SimpleJobLauncher`의 `run` 메서드이다. `jobName`과 `jobParameters`를 통해 `jobJobExecution`을 얻게 되는데, 해당 `jobExeuction`이 이미 존재하고 `status`가 `ReStartable`이 아니면 오류를 발생시킨다.

이때 `JobParametersIncrementer`를 사용하여 위와 같은 오류를 피할 수 있다.

</br >

### RunIdIncrementer

첫 번째 방법은 스프링 배치에서 제공하는 `RunIdIncrementer`를 사용하면 된다.

![image](https://user-images.githubusercontent.com/43977617/137687157-ba9debd6-3a38-4cfc-9cf3-e91db5cf7867.png)

- `RunIdIncrementer`를 보면 `RUN_ID_KEY`로 "run.id"가 선언되어 있는걸 볼 수 있다.
- 그리고 `Parameters`에 `run.id` 가 포함되어 있지 않으면 `run.id`를 직접 추가해주고, 존재한다면 값을 1씩 증가시킨다.
- 그러므로 동일한 파라미터를 사용해 동일한 잡을 두 번 수행해도 `run.id`의 값이 바뀌기 때문에 오류가 나지 않는다.

마지막으로 `RunIdIncrementer`의 key인 `run.id` 를 `JobParametersValidator`에 추가해주면 된다.

```java
//...
public JobParametersValidator validator() {
    DefaultJobParametersValidator validator = new DefaultJobParametersValidator();

    validator.setRequiredKeys(new String[]{"fileName"});
    validator.setOptionalKeys(new String[]{"name", "run.id"});

    return validator;
 }

@Bean(name = JOB_NAME)
public Job job() {
    return this.jobBuilderFactory.get(JOB_NAME)
            .validator(validator)
            .start(step1())
            .incrementer(new RunIdIncrementer())
            .build();
}
//...
```

</br >

이번에는 직접 `JobParametersIncrementer`를 구현해보자. 잡 실행 시마다 타임스탬프를 파라미터로 사용하는 `JobParametersIncrementer`를 구현해 보겠다.

`RunIdIncrementer`처럼 `JobParametersIncrementer`를 상속받아 구현하면 된다.

~~~java
public class DailyJobTimestamper implements JobParametersIncrementer {

    private static final String TIMESTAMP_KEY = "currentDate";

    @Override
    public JobParameters getNext(JobParameters parameters) {
        return new JobParametersBuilder(parameters)
                .addDate(TIMESTAMP_KEY, new Date())
                .toJobParameters();
    }
}
~~~

이제 `JobParametersValidator`에 `currentDate`를 새로 추가해주면 된다.

~~~java
//...
public JobParametersValidator validator() {
    DefaultJobParametersValidator validator = new DefaultJobParametersValidator();

    validator.setRequiredKeys(new String[]{"fileName"});
    validator.setOptionalKeys(new String[]{"name", DailyJobTimestamper.TIMESTAMP_KEY});

    return validator;
 }

@Bean(name = JOB_NAME)
public Job job() {
    return this.jobBuilderFactory.get(JOB_NAME)
            .validator(validator)
            .start(step1())
            .incrementer(new DailyJobTimestamper())
            .build();
}
//...
~~~

</br >

## 잡 리스너 적용하기

스프링 배치의 생명 주기의 여러 시점에 로직을 추가할 수 있는 기능을 제공한다. `JobExecutionListener`를 사용하면 된다. `JobExecutionListener` 인터페이스는 `beforeJob`과 `afterJob` 두 메서드를 사용하여 잡 실행주기에서 가장 먼저 또는 가장 나중에 실행시킬 메서드를 정의할 수 있다.

### 사용 사례

- 알림
  - 스프링 클라우드 태스크(Sping Cloud Task)는 잡의 시작이나 종료를 다른 시스템에 알리는 메시지 큐 메시지를 생성하는 `JobExecutionListener`를 제공한다.
- 초기화
  - `beforeJob`메서드를 사용하여 잡 실행 전에 필요한 값들을 초기화 시켜놀 수 있다.
- 정리
  - `afterJob`메서드를 사용하여 파일을 삭제하거나 보관하는 작업들을 실행할 수 있다
  - 이 정리 작업은 잡의 성공/실패에 영향을 미치지 않지만 실행돼야 한다

</br >

잡 리스너를 작성하는 방법으로 두 가지가 있다. 첫 번째 방법은 `JobExecutionListener`인터페이스를 구현하는 방법이다.

```java
public class JobLoggerListenerV1 implements JobExecutionListener {

    private static final String START_MESSAGE = "%s is beginning execution";
    private static final String END_MESSAGE = "%s has completed with the status %s";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println(String.format(START_MESSAGE,
                jobExecution.getJobInstance().getJobName()));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println(String.format(END_MESSAGE,
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus()));
    }
}
```

`JobExecutionListener` 인터페이스를 상속한 리스너를 하나 만들었다. 이 리스너를 사용하기 위해서는 `JobBuilder`의 `listener` 메서드를 호출하면 된다.

```java
//...
@Bean(name = JOB_NAME)
public Job job() {
    return this.jobBuilderFactory.get(JOB_NAME)
            .start(step1())
            .validator(validator)
            .incrementer(new DailyJobTimestamper())
            .listener(new JobLoggerListenerV1())
            .build();
}
//...
```

</br >

두 번째 방법은 애너테이션을 사용하는 것이다. 스프링 배치는 리스너 용도로 사용하는 `@BeforeJob`, `@AfterJob` 애너테이션을 제공한다.

첫 번째 방법과 차이점으로는 `JobExecutionListener` 인터페이스를 구현하지 않아도 된다.

~~~java
public class JobLoggerListenerV2 {

    private static final String START_MESSAGE = "%s is beginning execution";
    private static final String END_MESSAGE = "%s has completed with the status %s";

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println(String.format(START_MESSAGE,
                jobExecution.getJobInstance().getJobName()));
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        System.out.println(String.format(END_MESSAGE,
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus()));
    }
}
~~~

위 리스너를 잡에 주입하려면 `JobListenerFactoryBean`을 사용하여 래핑해줘야 한다.

```java
@Bean(name = JOB_NAME)
public Job job() {
    return this.jobBuilderFactory.get(JOB_NAME)
            .start(step1())
            .validator(validator)
            .incrementer(new DailyJobTimestamper())
            .listener(JobListenerFactoryBean.getListener(
                    new JobLoggerListenerV2()))
            .build();
}
```

</br >

## ExecutionContext

ExecutionContext는 배치의 상태를 저장한다.

JobExecution와 StepExecution과의 관계는 다음과 같다.

![image](https://user-images.githubusercontent.com/43977617/137696276-d969cf84-a60d-4d53-9a7b-a88cfa5f6a82.png)

</br >

## ExecutionContext 조작 방법

```java
public class HelloTasklet implements Tasklet {
    private static final String HELLO_WORLD = "Hello, %s";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String name =
                (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("name");

        // job context
        final ExecutionContext jobContext =
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext();

        // step context
        final ExecutionContext stepContext =
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getExecutionContext();

        jobContext.put("user.name", "name");

        System.out.println(String.format(HELLO_WORLD, name));
        return RepeatStatus.FINISHED;
    }
}
```

위 코드는 job과 step의 `ExecutionContext`를 사용하는 방법을 보여준다.

</br >

`JobExecution`의 `ExecutionContext`를 조작하는 방법으로 `StepExecution`을 승격하는 방법이 있다.

`ExecutionContextPromotionListener`를 사용하여 `StepExecution`을 `JobExecution`으로 승격시킬 수 있다.

```java
//...
@Bean(name = "one" + STEP_NAME)
public Step step1() {
    return this.stepBuilderFactory.get("one" + STEP_NAME)
            .tasklet(new HelloTasklet())
            .listener(promotionListener())
            .build();
}

// step의 ExecutionContext에서 "name"키를 찾으면 Job의 ExecutionContext에 복사 (Step -> Job ExecutionContext로 승격)
@Bean
public StepExecutionListener promotionListener() {
    ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
    listener.setKeys(new String[]{"name"});
    return listener;
}
//...
```


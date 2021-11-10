# Chapter6 잡 실행하기

## 배치 잡 실행시키기

### JobLauncherCommandLIneRunner

- 스프링 배치의 `JobLauncher`를 사용해 잡을 실행한다.
- 스프링 부트가 `ApplicationContext` 내에 구성된 모든 `CommandLineRunner`를 실행할 때, 클래스패스에 spring-boot-starter-batch가 존재한다면 `JobLauncherCommandLIneRunner`는 컨텍스트 내에서 찾아낸 모든 잡을 실행한다.

</br >

### 애플리케이션 기동 시 실행시킬 잡 정의하는 방법

- 애플리케이션이 기동되는 시점에 잡을 실행시키지 않으려면 `spring.batch.job.enabled` 프로퍼티를 `false`로 설정하면 된다.
- 이렇게 설정하면 애플리케이션 기동 시 어떤 잡도 실행되지 않는다.

</br >

### 여러 잡의 정의돼어 있을 때 애플리케이션 기동 시 특정 잡 실행시키는 방법

만약 어떤 잡이 다른 잡을 실행시키는 즉, 부모 잡이 자식 잡을 실행시켜야 하는 경우가 있을 수 있다. 이때 `spinrg.batch.job.name` 프로퍼티를 사용해 애플리케이션 기동 시 실행할 잡을 구성할 수 있다.

- 쉼표로 구분된 잡 목록을 가져와 순서대로 실행한다.

</br >

## REST 방식으로 잡 실행하기

REST 방식으로 잡 실행 방법을 알아보기 전에 `JobLauncher`에 대해 먼저 알아보자.

### JobLauncher

~~~java
public interface JobLauncher {
	public JobExecution run(Job job, JobParameters jobParameters) throws JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException;
}
~~~

- `JobLauncher` 인터페이스는 `run` 메서드 하나만 존재한다. `JobParameter` 파라미터를 전달받는다.
- `JobLauncher`가 사용하는 `TaskExecutor`를 적절히 구성해 실행 방식을 선택할 수 있다.

스프링 배치는 기본적으로 `JobLauncher`의 구현체인 `SimpleJobLauncher`를 제공한다. 

`SimpleJobLauncher`는 동기식 `TaskExecutor`를 사용해 잡을 동기식으로 실행한다.

</br >

예제에 사용될 잡은 다음과 같이 구성했다.

```java
@Configuration
@RequiredArgsConstructor
public class RestJobConfiguration {

    public static final String JOB_NAME = "chap6_rest_job";
    public static final String STEP_NAME = "-chap6_rest_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer()) // Job 파라미터 증가
                .start(restStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step restStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("first step ran today");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
```

다음은 잡을 실행시키는 컨트롤러 예제이다.

```java
@RestController
@RequiredArgsConstructor
public class JobLaunchingController {

    private final JobLauncher jobLauncher;
    private final ApplicationContext context;
    private final JobExplorer jobExplorer;

    @PostMapping("/job/run")
    public ExitStatus runJob(@RequestBody JobLauncherRequest request) throws Exception {
        Job job = this.context.getBean(request.getName(), Job.class);
        final JobParameters jobParameters = new JobParametersBuilder(request.getJobParameters(), this.jobExplorer)
                .getNextJobParameters(job) // 파라미터 증가시키기 위한 메서드 (RunIdIncrementer 활성화)
                .toJobParameters();

        return this.jobLauncher.run(job, jobParameters)
                .getExitStatus();
    }
}
```

위 컨트롤러의 runJob 메서드에서는 두 가지 일이 일어난다.

- `ApplicationContext`를 이용해 실행할 `Job` 빈을 가져온다.
- `Job` 객체 및 `JobParameters` 객체를 가지고 왔다면 이 두 객체를 `JobLauncher`에 전달해 잡을 실행한다.

위 코드에 `getNextJobParameters` 메서드가 있다. 이 메서드를 붙여주지 않으면 Job에 설정한 `incrementer(new RunIdIncrementer())`가 동작하지 않는다.

### getNextJobParameters(Job job)

- 이 메서드는 `Job`이 `JobParametersIncrementer`를 가지고 있는지 해당 `Job`을 보고서 판단한다.
- `JobParametersIncrementer`를 가지고 있다면 마지막 `JobExecution`에 사용됐던 `JobParameters`에 적용한다.
- 또한 이 실행이 재시작인지 여부를 판단하고 `JobParameters`를 적절하게 처리한다.
- 만약 위의 일들에 해당되지 않으면 아무것도 변경되지 않는다.




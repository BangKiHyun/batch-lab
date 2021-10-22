# Chapter4-2 잡과 스텝 이해하기

## 스텝(Step)

### 태스크릿(Tasklet) 모델

- Tasklet 인터페이스를 사용해 execute 메서드가 RepeatStatus.FINISHED를 반환할 때까지 트랜잭션 범위 내에서 반복적으로 실행되는 코드를 만들 수 있다.

</br >

### 청크(Chunk) 기반 처리 모델

![image](https://user-images.githubusercontent.com/43977617/137887686-9fe73320-3433-447d-a42b-f1322a7e3a74.png)

- 최소 2~3개의 중 컴포넌트로 구성된다. (ItemReader, ItemProcessor, ItemWriter)
- 이러한 컴포넌트를 사용해 레코드를 청크 또는 레코드 그룹 단위로 처리한다.
- 각 청크는 자체 트랜잭션으로 실행된다
- 처리에 실패했다면 마지막으로 성공한 트랜잭션 이후부터 다시 시작할 수 있다.

### ItemReader

- 첫 번째로 실행되는 작업
- 청크 단위로 처리할 모든 레코드를 반복적으로 메모리로 읽어온다.
- 커밋 간격에 도달할 때까지 반복한다.

### ItemProcessor

- 두 번째로 실행되는 작업
- 메모리로 읽어온 아이템은 반복적으로 ItemProcessor를 거쳐간다.
- ItemReader가 읽은 아이템 수만큼 반복한다.

### ItemWriter

- 단일 호출시 물리적 쓰기를 일괄적으로 처리함으로써 IO최적화를 이룬다.

</br >

## 스텝 구성

스프링 배치는 기본적으로 각 스텝이 상태(state)와 다음 상태로 이어지는 전이(transition)의 모음을 나타내는 상태 머신이다.

### 태스크릿 스텝

태스크릿 스텝은 스프링 배치가 제공하는 두 가지 주요 스텝 유형 중 하나로, 만드는 방법으로 두 가지 유형이 있다.

### 1. Tasklet 인터페이스 직접 구현 방법(람다)

첫 번째 방법으로 람다를 사용해 Tasklet을 구현하는 방법이다.

```java
public class LambdaTaskletConfiguration {

    public static final String JOB_NAME = "chap4_tasklet_job";
    public static final String STEP_NAME = "-chap4_tasklet_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(lambdaTaskletStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step lambdaTaskletStep() {
        return stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("Hello! lambda tasklet");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }
}
```

Tasklet 구현체의 처리가 완료되면 `org.springframework.batch.repeat.RepeatStatus` 객체를 반환해야 한다. 반환 값은 `CONTINUABLE`, `FINISHED` 둘 중 하나를 선택

- CONTINUABLE
  - 스프링 배치에게 해당 태스크릿을 다시 실행하라고 말하는 것
  - 어떤 조건이 충족될 때까지 특정 태스크릿을 반복해서 실행해야 할 때, 태스크릿의 실행 횟수, 트랜잭션 등을 추적해주기를 바랄 때, 조건이 충족될 때까지 태스크릿이 CONTINUABLE을 반환하도록 하면 된다.
- FINISHED
  - 처리의 성공 여부에 관계없이 이 태스크릿의 처리를 완료하고 다음 처리를 이어서 하겠다는 의미

</br >

### 2. Tasklet 구현체 사용 방법

`CallableTaskletAdapter`, `MethodInvokingTaskletAdapter`, `SystemCommandTasklet` 세 가지의 서로 다른 Tasklet 구현체가 있다.

### 2-1. CallableTaskletAdapter

`CallableTaskletAdapter`는 `java.util.concurrent.Callable<RepeatStatus>`인터페이스의 구현체를 구성할 수 있게 해주는 어댑터다.

`CallableTaskletAdapter`는 `Callable` 객체의 `call()` 메서드를 호출하고, `call()` 메서드가 반환하는 값을 반환하면 된다.

```java
@Configuration
@RequiredArgsConstructor
public class CallableTaskletConfiguration {

    public static final String JOB_NAME = "callable_job";
    public static final String STEP_NAME = "-callable_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job callableJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(callableStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step callableStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(tasklet())
                .build();

    }

    @Bean(name = "callable_tasklet")
    public CallableTaskletAdapter tasklet() {
        CallableTaskletAdapter taskletAdapter = new CallableTaskletAdapter();
        taskletAdapter.setCallable(callableObject());
        return taskletAdapter;
    }

    @Bean
    public Callable<RepeatStatus> callableObject() {
        return () -> {
            System.out.println("Hello. This is callable tasklet");
            System.out.println("This was executed in another thread");
            return RepeatStatus.FINISHED;
        };
    }
}
```

`CallableTaskletAdapter`는 태스크릿이 스텝이 실행되는 스레드와 별개의 스레드에서 실행되지만 스텝과 병렬로 실행되는 것은 아니다.

스텝이 실행될 때 Callable 객체가 유효한 RepeatStatus 객체를 반환해야 다른 스텝을 실행할 수 있다.

</br >

### 2-2. MethodInvokingTaskletAdapter

`MethodInvokingTaskletAdapter`는 기존에 존재하던 다른 클래스 내의 메서드를 잡 내의 테스크릿처럼 실행할 수 있다.

예로 배치 잡 내에서 한 번만 실행하고 싶은 로직을 어떤 서비스가 이미 갖고 있다고 가정해 봤을때, 해당 메서드를 MethodInvokingTaskletAdapter 구현체를 사용해 호출할 수 있다.

~~~java
@Configuration
@RequiredArgsConstructor
public class MethodInvokingTaskletConfiguration {

    public static final String JOB_NAME = "chap4_methodInvoking_job";
    public static final String STEP_NAME = "-chap4_methodInvoking_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(methodInvokingTaskletStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step methodInvokingStep() {
        return stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(methodInvokingTasklet())
                .build();
    }

    @Bean
    public MethodInvokingTaskletAdapter methodInvokingTasklet() {
        final MethodInvokingTaskletAdapter taskletAdapter = new MethodInvokingTaskletAdapter();
        taskletAdapter.setTargetObject(service());
        taskletAdapter.setTargetMethod("serviceMethod");
        return taskletAdapter;
    }

    @Bean
    public CustomeService service() {
        return new CustomeService();
    }
}
~~~

```java
public class CustomeService {

    public void serviceMethod() {
        System.out.println("Hello! Method Invoking Tasklet!");
        System.out.println("Service method was called");
    }
}
```

MethodInvokingTaskletAdapter를 보면 실행할 객체와 메서드를 지정해 준다.

어댑터는 파라미터 없이 메서드를 호출하고, 해당 메서드가 `ExisStatus` 타입을 반환하지 않는 한 결괏값으로 `COMPLETED`를 반환한다. `ExisStatus`를 반환하면 메서드가 반환 값이 태스크릿에서 반환된다.

</br >

### 2-3. SystemCommandTasklet

`SystemCommandTasklet`은 시스템 명령을 실행할 때 사용한다. 지정한 시스템 명령은 비동기로 실행된다.

```java
@Configuration
@RequiredArgsConstructor
public class SystemCommandTaskletConfiguration {

    public static final String JOB_NAME = "chap4_systemCommand_job";
    public static final String STEP_NAME = "-chap4_systemCommand_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(systemCommandStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step systemCommandStep() {
        return stepBuilderFactory.get("first" + STEP_NAME)
                .tasklet(systemCommandTasklet())
                .build();
    }

    @Bean
    public SystemCommandTasklet systemCommandTasklet() {
        final SystemCommandTasklet systemCommandTasklet = new SystemCommandTasklet();
        systemCommandTasklet.setCommand("rm -rf /tmp.txt");
        systemCommandTasklet.setTimeout(5000);
        systemCommandTasklet.setInterruptOnCancel(true);
        return systemCommandTasklet;
    }
}
```

위 코드에서 `setInterruptOnCancel` 메서드는 선택 사항으로, 잡이 비정상적으로 종료될 때 시스템 프로세스와 관련된 스레드를 강제로 종료할지 여부를 스프링 배치에게 알려주는 데 사용한다.

</br >

## 청크 기반 스텝

청크는 커밋 간격에 의해 정의된다.

커밋 간격을 50개 Item으로 설정했다면 잡은 50개 아이템을 읽고(read), 50개 아이템으로 처리(process)한다음에 한번에 50개 아이템을 기록(write)한다.

```java
@Configuration
@RequiredArgsConstructor
public class ChunkStepConfiguration {

    public static final String JOB_NAME = "chap4_chunk_job";
    public static final String STEP_NAME = "-chap4_chunk_step";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean(name = JOB_NAME)
    public Job chunkJob() {
        return this.jobBuilderFactory.get(JOB_NAME)
                .start(chunkStep())
                .build();
    }

    @Bean(name = "first" + STEP_NAME)
    public Step chunkStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .<String, String>chunk(10) //커밋 간격 10
                .reader(itemReader(null))
                .writer(itemWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> itemReader(
            @Value("#{jobParameters['imputFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<String>()
                .name("itemReader")
                .resource(inputFile)
                .lineMapper(new PassThroughLineMapper())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> itemWriter(
            @Value("#{jobParameters['outputFile']}") Resource outputFile) {
        return new FlatFileItemWriterBuilder<String>()
                .name("itemWriter")
                .resource(outputFile)
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }
}
```

청크 기반 스텝은 `build` 메서드를 호출되기 전에 리더 및 라이터를 가져온다. 위 코드는 커밋 간격의 10으로 설정했다. 그러므로 10개의 단위로 레코드를 처리한 후 작업이 커밋된다.

커밋 간격을 지정하는건 굉장히 중요하다. 위 코드에서는 10개의 레코드르 읽고, 처리할 때까지 어떤 레코드도 쓰기 작업을 하지 않는다. 즉, 9개의 아이템을 처리한 후 오류가 발생하면, 스프링 배치는 현재 청크(트랜잭션)를 롤백하고 잡이 실패했다고 표시한다.

커밋 간격의 1로 설정하게 되면 잡은 아이템 하나를 읽어 바로 해당 아이템을 처리하고 쓴다. 이렇게 했을 때 아이템 한 건당 이뤄질 뿐만 아니라 잡의 상태는 JobRepository에 갱신된다.

</br >

### 청크 크기 구성하기

청크 크기를 구성하는 방법으로 두 가지가 있다. 첫 번째는 정적 커밋 개수 설정, 두 번째는 `CompletionPolicy` 구현체 사용 방법이다.

다음은 정적으로 커밋 개수를 설정한 코드다.

```java
//...
    @Bean(name = "first" + STEP_NAME)
    public Step chunkStep() {
        return this.stepBuilderFactory.get("first" + STEP_NAME)
                .<String, String>chunk(1000)
                .reader(itemReader())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ListItemReader<String> itemReader() {
        final List<String> itmes = new ArrayList<>(100_000);
        for (int idx = 0; idx < 100_000; idx++) {
            itmes.add(UUID.randomUUID().toString());
        }
        return new ListItemReader<>(itmes);
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println(">> current item = " + item);
            }
        };
    }
//...
```

이 방법은 커밋 간격을 하드 코딩해 청크 크기를 정의한다. 하지만 크기가 동일하지 않은 청크를 처리해야할 할때는 어떻게 해야할까?

예를 들어 계좌 하나의 모든 거래 내역을 단일 트랜잭션으로 처리해야 하는 상황 등이 있을 수 있다. 이때 `CompletionPolicy`를 사용해서 청크가 완료되는 시점을 프로그래밍 방식으로 정의할 수 있다.

기본적으로 `SimpleCompletionPolicy`를 사용한다. `SimpleCompletionPolicy`는 처리된 아이템 개수를 세는데, 이 개수가 미리 구성해둔 임계값에 도달하면 청크 완료로 표시한다.

`TimeoutTerminationPolicy`이라는 구현체를 사용해 타임아웃 값을 구성할 수 있다. 이를 통해 청크 내에서 처리 시간이 해당 시간이 넘을 때 해당 청크가 완료된 것으로 간주되고, 모든 트랜잭션 처리가 정상적으로 계속된다.

`CompositeCompletionPolicy`는 청크 완료 여부를 결정하는 여러 정책을 함께 구성할 수 있다. 자신이 포함하고 있는 여러 정책 중 하나라도 청크 완료라고 판단되면 해당 청크가 완료된 것으로 표시된다.

다음 코드는 아이템의 정상 커밋 횟수가 1000개 및 3밀리초의 타임아웃을 이용한 예제다.

```java
//...
    @Bean
    public CompletionPolicy completionPolicy() {
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();
        policy.setPolicies(new CompletionPolicy[]{
                new TimeoutTerminationPolicy(3),
                new SimpleCompletionPolicy(1000)});
        return policy;
    }
//...
```

</br >

### CompletionPolicy 구성 요소

- isComplete(RepeatContext, RepeatStatus)
  - 청크 완료 여부의 상태를 기반으로 결정 로직을 수행한다.
- isComplete(RepeatContext)
  - 내부 상태를 이용해 청크 완료 여부를 판단한다.
- start(RepeatContext)
  - 가장 먼저 호출되는 메서드
  - 청크의 시작을 알 수 있도록 정책을 초기화한다.
  - 즉, 청크 시작 시 해당 구현체가 필요로하는 모든 내부 상태를 초기화한다.
- update(RepeatContext)
  - 각 아이템마다 내부 카운터를 하나씩 증가시킨다.

다음은 청크 시작마다 랜덤하게 20 미만의 수를 지정하고 해당 개수만큼의 아이템이 처리되면 청크를 완료하는 코드다.

```java
public class RandomChunkSizePolicy implements CompletionPolicy {

    private int chunkSize;
    private int totalProcessed;
    private Random random = new Random();

    @Override
    public boolean isComplete(RepeatContext context, RepeatStatus result) {
        if (RepeatStatus.FINISHED == result) return true;
        else return isComplete(context);
    }

    @Override
    public boolean isComplete(RepeatContext context) {
        return this.totalProcessed >= chunkSize;
    }

    @Override
    public RepeatContext start(RepeatContext parent) {
        this.chunkSize = random.nextInt(20);
        this.totalProcessed = 0;
        System.out.println("The chunk size has been set to " + this.chunkSize);
        return parent;
    }

    @Override
    public void update(RepeatContext context) {
        this.totalProcessed++;
    }
}
```

![image](https://user-images.githubusercontent.com/43977617/138232174-7a491c62-b083-4e2d-a3a5-c70be251d105.png)

코드를 보면 새 청크가 시작될 때마다 청크 크기가 변하는 걸 볼 수 있다.

</br >

## 스텝 리스너

스텝 리스너를 통해 각각 스텝과 청크의 시작과 끝에 특정 로직을 실행시킬 수 있다.

### StepExecutionListener

- `beforeStep` 메서드: `void` 반환
- `afterStep` 메서드: `ExitStatus`를 반환
  - 이 기능을 통해 잡 처리의 성공 여부를 판별할 수 있다.
  - 예로, 파일을 가져온 후 데이터베이스에 올바른 개수의 레코드가 기록됐느지 여부를 확인하는 등 기본적인 무결성 검사를 수행할 수 있다.

### ChunkListener

- `beforeChunk` 메서드: `void` 반환
- `afterChunk` 메서드: `void` 반환

다음은 애노테이션을 사용해 `StepExecutionListener`를 구현한 예제다.

```java
public class LogginStepStartStopListener {

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        System.out.println(stepExecution.getStepName() + "has begun!");
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println(stepExecution.getStepName() + "has ended!");
        return stepExecution.getExitStatus();
    }
}
```

![image](https://user-images.githubusercontent.com/43977617/138238469-6702638f-ad63-4055-8e69-ab65a0aba92d.png)

결과를 보면 스텝 시작 전과 후 listener가 잘 동작된 걸 확인할 수 있다.

</br >

## 스텝 플로우

각 스텝을 순서대로 실행시킨다면 스프링 배치는 매우 제한적으로 사용할 수 밖에 없다. 하지만 프레임워크는 잡 흐름을 커스터마이징할 수 있는 여러 가지 강력한 방법을 제공한다.

### 조건 로직

전이(transition)을 구성하면 스텝을 다른 순서로 실행할 수 있다.

~~~java
/**
/ stepA가 성공이면 stepB 실행
/ stepA가 실패면 stepC 실행
*/
@Bean
public Job job() {
	return this.jobBuilderFactory.get("job")
				.start(stepA())
				.on("*").to(stepB())
				.from(stepA()).on("FAILED").to(stepC())
				.end()
				.build();
}
~~~

- `on()`: 스프링 배치가 스텝이 ExitStatus를 평가해 어떤 일을 수행할지 결정할 수 있도록 구성하게 해준다.
- `*`: 0개 이상의 문자를 일치시킨다는 것을 의미한다. 예를 들어 `*C`는 `C`, `COMPLETE`, `CORRECT`와 일치한다.
- `?`: 1개의 문자를 일치시킨다는 것을 의미한다. 예를 들어 `?AT`는 `CAT`, `KAT`과 일치하지만 `THAT`과는 일치하지 않는다.

</br >

### JobExecutionDecider

`JobExecutionDecider`는 다음에 무엇을 해야 할지 프로그래밍적으로 결정할 수 있는 방법을 제공한다.

`JobExecutionDecider` 인터페이스는 `decide()` 메서드만 존재하고, `JobExecution`과 `StepExecution`을 파라미터로 전달받고, `FlowExecutionStatus`를 반환한다.

```java
public class RandomDecider implements JobExecutionDecider {

    private Random random = new Random();

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (random.nextBoolean()) {
            return new FlowExecutionStatus(FlowExecutionStatus.COMPLETED.getName());
        } else {
            return new FlowExecutionStatus(FlowExecutionStatus.FAILED.getName());
        }
    }
}
```

</br >

### 잡 종료하기

먼저 잡의 종료 상태는 다음과 같다.

- Completed
  - 스프링 배치 처리가 성공적으로 종료됐음을 의미
  - Completed로 종료되면 동일한 파라미터를 사용해 다시 실행할 수 없다.
- Failed
  - 잡이 성공적으로 완료되지 않았음을 의미한다.
  - Failed 상태로 종료된 잡은 스프링 배치를 사용해 동일한 파라미터로 다시 실행할 수 있다.
- Stopped
  - Stopped 상태로 종료된 잡은 다시 시작할 수 있다.
  - Stepped 상태는 잡에 오류가 발생하지 않았지만 중단된 위치에서 잡을 다시 시작할 수 있다.
  - 스텝 사이에 사람의 개입이 필요하거나 다른 검사나 처리가 필요한 상황에 매우 유용하다.

</br >

### 종료와 관련된 메서드

- `end()`: 스텝이 반환한 상태가 무엇이든 상관없이 잡의 상태를 Completed로 저장한다.
- `fail()`: 잡의 상태가 Failed 상태로 종료되도록 한다.
- `stoppedRestart()`: 잡의 상태를 Stopped로 바꾼다. 그리고, 잡을 재실행하면 처음 스탭이 아닌 `stoppedRestart()`에 정의한 스텝부터 실행된다.

</br >

## 플로우 외부화하기


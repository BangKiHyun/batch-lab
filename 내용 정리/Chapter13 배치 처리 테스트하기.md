# Chapter13 배치 처리 테스트하기

## JUnit과 Mockito를 사용한 단위 테스트

### 단위 테스트

- 격리된 단일 컴포넌트를 반복 가능한 방식으로 수행하는 테스트
- 일반적으로 단위 테스트의 범위는 메서드 하나이다.

단위 테스트는 단 하나의 테스트를 다른 반복 가능한 방식(동일한 시나리오 반복 수행, 회귀 테스트) 으로 테스트

이때, 각 의존성의 통합을 테스트하는 것이 아닌, 개별 컴포넌트의 동작 방식을 테스트하는 것.

</br >

## JUnit

자바로된 프레임워크를 테스트하는 최적의 표준화된 방식. 즉, 단위 테스트를 할 수 있는 기능을 제공하는 간단한 프레임워크

### 생명주기

- @BeforeEach
- @Test
- @AfterEach

</br >

## Mock 객체

테스트 환경에서 필요한 의존성을 대체하고, 외부 의존성의 영향 없이 비즈니스 로직을 실행하게끔 도와줌.

참고로 목 객체는 스텁이 아니다. 

스텁: 실행 중 특정 동작을 모킹하도록 하드 코딩된 로직이 포함.

</br >

### 목 객체 동작 방법 2가지

1. 프록시 기반 방법
   - 프록시 객체를 사용해 코드가 의존하는 실제 객체를 모킹하는 방법
   - 목 프레임워크를 사용해 프록시 객체를 만든 후, 해당 프록시 객체를 필요로 하는 객체에게 Setter나 생성자를 사용해 세팅
2. 클래스 재매핑 방법
   - 클래스 로더에게 로딩되는 클래스 파일에 대한 참조를 재매핑하도록 지시
   - 예로 MyDependency.class라는 파일명을 가진 MyDependency 클래스가 있지만, 해당 클래스를 대신해 해당 클래스를 모킹한 MyMock클래스를 사용하기를 원한다고 가정할 때, 이러한 유형을 목 객체를 사용하면 실제로 클래스로더 내의 MyDependency에서 MyMock.class로의 참조를 재매핑

</br >

## Mockito

- 확인이 필요한 동작을 모킹해, 중요한 동작만 검증할 수 있게 해줌.
- Mockito는 spring-boot-starter-test 의존성에 포함되어있음.



### 행동 주도 설계(Behavior Driven Design)

- given: 어떠한 입력이 주어지고
- when: 어떠한 조치가 발생하면
- then: 어떠한 결과가 나온다.

</br >

## 통합 테스트

### 통합 테스트란?

- 애플리케이션을 *부트스트랩하고 동일한 의존성을 사용해 실행함을써 자동화 테스트를 한 단계 더 끌어올리는 것
- 서로 다른 여러 컴포넌트 간의 상호작용이 정상적으로 수행되는지 테스트하는 것

> 부트스트랩: 한 번 시작되면 알아서 진행되는 일련의 과정

</br >

### 주요 사용 사례

- 데이터베이스와의 상호작용 테스트
- 스프링 빈과의 상호작용(서비스가 올바르게 연결돼 있는가 등) 테스트

</br >

## 스프링 배치 테스트

### 잡과 스텝 스코프 빈 테스트

- TestExecutionListener
  - 테스트 메서드 실행 전후에 수행돼야 하는 일을 정의하는 스프링 API
  - @BeforeEach, @AfterEach 애너테이션보다 더 재사용 가능. 테스트 케이스의 모든 메서드에 원하는 동작을 더욱 재사용 가능한 방식으로 삽입 가능
- StepScopeTestExecutionListener
  - 테스트 케이스에서 팩토리 메서드를 사용해 StepExecution을 가져오고, 반환된 컨텍스트를 현재 테스트 메서드 컨텍스트로 사용
  - 각 테스트 메서드가 실행되는 동안 스텝 켄텍스트(StepContext)를 제공
  - @BeforeEach -> StepScopeTestExecutionListener(getStepExecution) -> Test -> StepScopeTestExecutionListener(스텝 관리 끝내기) -> AfterEach

### @ContextConfiguration

- 애플리케이션 컨텍스트(ApplicationContext)를 만드는 클래스(XML 구성을 사용한다면 리소스)를 지정하는 곳
- 즉, ApplicationContext를 빌드하는 데 필요한 클래스 제공

### @SpringBatchTest

스프링 배치 잡을 테스트하는 데 ApplicationContext에 자동으로 테스트할 수 있는 많은 유틸리티 제공. 다음은 해당 애너테이션이 제공하는 중요한 빈이다.

- `JobLauncherTestUtils`: 잡이나 스텝을 실행하는 인스턴스
- `JobRepositoryTestUtils`: JobRepository에서 JobExecutions을 생성하는 데 사용
- `StepScopeTestExecutionListner`, `JobScopeTestExecutionListner`: 스텝 스코프와 잡 스코프 빈을 테스트하는 데 사용



</br >

## @Mock vs InjectMocks

@Mock이 붙은 목객체를 @InjectMocks이 붙은 객체에 주입시킬 수 있다.

보통 @InjectMocks(Service) @Mock(Repository) 이런식으로 Service테스트 목객체에 DAO 목객체를 주입시켜 사용한다.


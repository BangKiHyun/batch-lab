# Chapter8 ItemProcessor

## ItemProcessor란?

읽은 데이터를 사용해 특정 작업을 수행

- 입력의 유효성 검증
  - `ItemProcessor`가 유효성 검증을 수행하면 입력 방법에 상관없이 처리 전에 객체의 유효성 검증을 수행할 수 있다.
- 기존 서비스의 재사용
  - `ItemProcessorAdapter`를 사용해 입력 데이터를 다루는 기존의 서비스를 재사용할 수 있다.
- 스크립트 실행
  - ScriptItemProcessor를 사용하여 특정 스크립트를 ItermProcessor로 실행할 수 있다.
  - 스크립트에 입력으로 아이템을 제공하고 스크립트의 출력을 반환 값으로 가져올 수 있다.
- `ItemrProcessor`체인
  - 동일한 트랜잭션 내에서 단일 아이템으로 여러 작업을 수행하려는 상황이 있을 수 있다.
  - 이때 스프링 배치를 사용해 각 아이템에 대해 순서대로 실행될 `ItemProcessor` 목록을 만들 수 있다.

</br >

### 주의할 점

- ItemProcessor가 받아들이는 입력 아이템의 타입과 반환하는 타입이 같을 필요가 없다.
- 즉, ItemProcessor는 자신이 전달받은 입력 아이템 객체의 타입을 쓰기 작업을 수행하기 위한 다른 타입으로 변환해 반환할 수 있다.
- **최종적으로 ItemProcessor가 반환하는 타입은 ItemWriter가 입력으로 사용하는 타입이 돼야 한다.**
- `ItemProcessor`가 `null`을 반환하면 **해당 아이템의 이후 모든 처리가 중지된다.**
  - `ItemReader`는 더 이상 읽어들일 입력 데이터가 없을 때, `null`을 반환해 스프링 배치에게 알려준다.
  - 하지만 `ItemProcessor`가 `null`을 반환했을 때 **다른 아이템의 처리가 계속 이뤄진다.**
- ItemProcessor는 멱등이어야 한다. 아이템은 내결함성 시나리오에서 두 번 이상 전달될 수도 있다.
  - 멱등성: 연산을 여러 번 적용하더라도 결과가 달라지지 않는 성질
  - 내결함성: 시스템의 어떤 부분에 고장이 발생해도 시스템이 설계된 대로 계속 작동하는 것


</br >

## ValidatingItemProcessor

`ItemReader`에서 유형과 포맷 관련된 데이터 유효성 검증을 수행할 수 있지만, 아이템이 구성된 이후에 수행하는 비즈니스 규칙에 따른 유효성 검증은 리더가 아닌 다른 곳에서 수행되도록 하는 것이 좋다.

`ValidatingItemProcessor`는 입력 데이터 유효성 검증에 사용할 수 있는 `ItemProcessor`구현체이다.

- 빈 유효성 검증을 위한 자바 사양으로 javax.validation.* 코드를 통해 수행되는 유효성 검증은 애너테이션을 적용해 구성할 수 있다.
- spring-boot-starter-validation 스타터를 사용해 JSR-303 유효성 검증 도구의 하이버네이트 구현체를 가져옴.

### BeanValidatingItemProcessor

- 스프링 배치가 각 아이템을 검증하는 매커니즘 제공
- BeanValidatingItemProcessor 는 JSR-303을 활용해 유효성 검증을 제공하는 ValidationItemProcessor를 상속한 ItemProcessor



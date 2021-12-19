# Chapter9 ItemWriter

## ItemWriter 소개

기본에 `ItemWriter`는 `ItemReader`와 동일하게 각 아이템이 처리되는 대로 출력했다. 그러나 **스프링 배치2에서 청크 기반 처리 방식이 도입되면서 청크 하나가 완성되면 해당 아이템 목록이 `ItemWriter`에 전달돼 쓰기 작업이 수행된다.**

이와 같이 청크기반 처리 방식이 도입된 후, ItemWriter를 호출하는 횟수가 이전보다 훨씬 적어졌다.

`ItemWriter` 인터페이스는 `write` 메서드가 있고, 아이템 목록`List<T>`

![image](https://user-images.githubusercontent.com/43977617/146668109-38c958ef-bdeb-4f97-bc9d-a345e9ca606b.png)

</br >

## FlatFileItemWriter

- `FlatFileItemWriter`는 텍스트 파일 출력을 만들 때 사용할 수 있도록 스프링 배치가 제공하는 `ItemWriter` 인터페이스 구현체
- 리소스와 `LineAggregator` 구현체로 구성됨
  - `LineAggregator`: 객체를 기반으로 출력 문자열 생성

### FiedlExtractor

- 제공되는 아이템의 필드에 접근할 수 있도록 함
- 스프링 배치가 제공하는 두 개의 FieldExtractor 인터페이스
  - BeanWrapperFieldExtractor: 클래스의 접근자(getter)를 사용해 자바 빈 프로퍼티에 접근
  - PassThroughFieldExtractor: 아이템을 바로 반환

### FlatFileItemWriter와 트랜잭션

- `FlatFileItemWriter`는 쓰기 데이터의 노출을 제한해 롤백이 가능한 커밋 전 마지막 순간까지 출력 데이터의 저장을 지연시킴
  - 트랜잭션 주기 내에서 실제 쓰기 작업을 가능한 한 늦게 수행
- `TransactionSynchronizationAdapter`의 `beforeCommit` 메서드를 사용해 해당 매커니즘을 구현
  - 즉, 데이터를 실제로 디시 디스크에 기록하기 직전에 `PlatformTransactionManager`가 트랜잭션을 커밋한다.

### FlatFileItemWriter 예제

```java
@Bean(name = STEP_NAME + "FlatFileWriter")
@StepScope
public FlatFileItemWriter<Customer> flatItemWriter(
        @Value("#{jobParameters['outputFile']}") ClassPathResource outputFile) {
    return new FlatFileItemWriterBuilder<Customer>()
            .name(STEP_NAME + "FlatFileWriter")
            .resource(outputFile)
            .formatted()
            .format("%s %s lives at %s %s in %s %s.") // 포매팅 지정
            .names(new String[]{"firstName",
                    "middleInitial",
                    "lastName",
                    "address",
                    "city",
                    "state",
                    "zip"})
            .build();
}
```

- 출력 파일 생성 경로를 잡 파리미터로 주입
- 사용할 이름 및 리소스 지정
- `FormattedBuilder`를 반환받고, 출력물의 포맷을 구성

</br >

### 구분자로 구분된 파일

구분자로 구분된 파일을 읽어 구분자를 쉼표(,)에서 세미콜론(;)으로 변경

```java
@Bean(name = STEP_NAME + "Delimited")
@StepScope
public FlatFileItemWriter<Customer> delimitedItemWriter(
        @Value("#{jobParameters['outputFile']}") ClassPathResource outputFile) {
    return new FlatFileItemWriterBuilder<Customer>()
            .name(STEP_NAME + "Delimited")
            .resource(outputFile)
            .delimited() // default: 콤마(,)
            .delimiter(";") // 세미콜론(;)으로 변경
            .names(new String[]{"firstName",
                    "middleInitial",
                    "lastName",
                    "address",
                    "city",
                    "state",
                    "zip"})
            .build();
}
```

- `FormatterLineAggregator` 대신 `DelimitedLineAggregator`를 사용


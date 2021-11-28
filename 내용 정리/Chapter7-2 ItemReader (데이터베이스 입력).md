# Chapter7-2 ItemReader (데이터베이스 입력)

## JDBC

`JdbcTemplate`이 전체 `ResultSet`에서 한 로우씩 순서대로 가져오면서, 모든 로우를 필요한 도메인 객체로 변환해 메모리에 적재한다.

이에 대한 대안으로 스프링 배치는 한 번에 처리할 만큼의 레코드만 로딩하는 별도의 두 가지 기법을 제공하는데, 커서(cursor)와 페이징(paging)이다.

커서 기반과 페이지 기반 JDBC 리더를 구현하려면 두 가지 작업이 필요하다.

- 필요한 쿼리를 실행할 수 있도록 리더를 구성
- 스프링의 `JdbcTemplate`이 `ResultSet`을 도메인 객체로 매핑하는 데 필요한 `RowMapper` 구현체

</br >

## 커서(Cussor)

커서는 표준 `ResultSet`으로 구현된다. `ResultSet`이 open되면 `next()` 메서드를 호출할 때마다 데이터베이스에서 배치 레코드를 반환한다.

커서 기법으로 읽을 때는 최초에 레코드 하나를 반환하면 **한 번에 하나씩 레코드를 스트리밍하면서 다음 레코드로 진행된다.**

</br >

### JdbcCursorItemReader

`JdbcCursorItemReader`를 사용하여 요청에 따라 결과를 반환할 수 있도록 커서를 열어 쿼리를 실행할 수 있다.

`JdbcCursorItemReader`는 `ResultSet`을 생성하면서 커서를 연 다음, 스프링 배치가 `read` 메서드를 호출할 때마다 도메인 객체로 매핑할 로우를 가져온다.

### JdbcCursorItemReader 구성을 위한 최소 의존성

- 데이터 소스(DataSource)
- 실행할 쿼리
- 사용할 `RowMapper` 구현체

```java
@Bean
public JdbcCursorItemReader<CustomerDB> cursorItemReader(DataSource dataSource) {
    return new JdbcCursorItemReaderBuilder<CustomerDB>()
            .name("cursorItemReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM customerDB")
            .rowMapper(new CustomerDBRowMapper())
            .build();
}
```

</br >

### SQL 파라미터 설정

`JdbcCursorItemReader`를 사용하여 SQL에 파라미터를 설정하는 방법이 있다. `PreparedStatementSetter` 구현체를 사용하면 된다.

`RowMapper`는 `ResultSet` 로우를 도메인 객체로 매핑하지만, `PreparedStatementSetter`는 파라미터를 SQL문에 매핑을 한다는 차이가 있다.

</br >

### ArgumentPreparedStatementSetter

`PreparedStatementSetter`를 직접 구현할 수도 있지만 스프링에서 제공해 주는 몇가지 유용한 구현체가 있다. 그중 `ArgumentPreparedStatementSetter` 인스턴스는 객체 배열을 전달받는다.

배열에 담긴 객체가 `SqlParameterValue` 타입이 아니라면, 해당 객체는 담긴 순서대로 `PreparedStatement`의 `?의` 위치에 값으로 설정된다.

배열 내 객체가 `SqlParameterValue` 인스턴스면 `SqlParameterValue` 타입에는 값을 설정하는 방법이 담긴 메타데이터가 포함돼 있다. 따라서 메타데이터에 정의된 내용에 따라 파라미터를 설정한다.

```sql
@Bean
public JdbcCursorItemReader<CustomerDB> cursorItemReaderWithParameter(DataSource dataSource) {
    return new JdbcCursorItemReaderBuilder<CustomerDB>()
            .name("cursorItemReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM customerDB WHERE city = ?")
            .rowMapper(new CustomerDBRowMapper())
            .preparedStatementSetter(citySetter(null))
            .build();
}

@Bean
@StepScope
public ArgumentPreparedStatementSetter citySetter(
        @Value("#{jobParameters['city']}") String city) {
    return new ArgumentPreparedStatementSetter(new Object[]{city});
}
```

</br >

### 단점

- 대용량 데이터의 레코드를 처리할 때 매번 요청을 할 때마다 네트워크 오베허드가 추가됨.
- `ResultSet`은 스레드 세입(Thread Safe)이 보장되지 않으므로 다중 스레드 환경에서 사용할 수 없음.

</br >

## 페이징(Paging)

페이징은 데이터베이스에서 페이지라고 부르는 청크 크기만큼의 레코드를 가져오는 것이다. 각 페이지는 해당 페이지만큼의 레코드만 가져올 수 있는 고유한 SQL 쿼리를 통해 생성된다.

각 페이지는 사전에 정의된 개수만큼 데이터베이스가 반환할 레코드로 구성된다.

페이징 기법을 사용할 때도 커서 기법과 마찬가지로 잡이 처리할 아이템은 여전히 한 건씩 처리된다. 다만 페이징 기법은 한 번에 SQL 쿼리 하나를 실행해 레코드 하나씩을 가져오는 대신, **각 페이지마다가 새로운 쿼리를 실행한 뒤 쿼리 결과를 한 번에 메모리에 적재한다.**

각 데이터베이스마다 개별적인 페이징 구현체를 제공한다.

1. 사용하려는 데이터베이스 전용 `PagingQueryProvider` 구현체 구성
2. 리더가 `SqlPagingQueryProviderFactoryBean`을 사용하도록 구성
   - 이 팩토리는 사용하는 데이터베이스가 어떤 것인지 감지할 수 있다.

일반적으로 `SqlPagingQueryProviderFactoryBean`를 쓰면 사용 중인 데이터베이스를 자동으로 감지해 적절한 `PagingQueryProvider`를 반환한다.

</br >

### JdbcPagingItemReader

`JdbcPagingItemReader`를 사용하여 JDBC 페이징 처리를 할 수 있다.

### JdbcPagingItemReader 구성에 필요한 네 가지 의존성

- 데이터 소스(DataSource)
- `PagingQueryProvider` 구현체
- 직접 개발한 `RowMapper` 구현체
- 페이지의 크기

```java
@Bean
public JdbcPagingItemReader<CustomerDB> pagingItemReader(DataSource dataSource,
                                                         PagingQueryProvider queryProvider,
                                                         @Value("#{jobParameters['city']}") String city) {
    Map<String, Object> parameterValues = new HashMap<>(1);
    parameterValues.put("city", city);

    return new JdbcPagingItemReaderBuilder<CustomerDB>()
            .name("pagingItemReader")
            .dataSource(dataSource)
            .queryProvider(queryProvider)
            .parameterValues(parameterValues)
            .pageSize(10)
            .rowMapper(new CustomerDBRowMapper())
            .build();
}

@Bean
public SqlPagingQueryProviderFactoryBean pagingQueryProvider(DataSource dataSource) {
    SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
    factoryBean.setSelectClause("SELECT *");
    factoryBean.setFromClause("FROM CustomerDB");
    factoryBean.setWhereClause("WHERE city =: city"); //namedParameter 사용 가능
    factoryBean.setSortKey("lastName");
    factoryBean.setDataSource(dataSource);
    
    return factoryBean;
}
```

위 코드에서 `PagingQueryProvider` 구성을 위한 다섯 가지 항목을 설정했다.

- SELECT 절
- FROM 절
- WHERE 절
- ORDER BY (sortKey) 절
  - 페이징 기법은 한 번에 쿼리 하나를 실행한 뒤 결과를 스트리밍받는 대신 각 페이지에 해당하는 쿼리를 실행한다.
  - 각 페이지의 쿼리를 실행할 때마다 동일한 레코드 정렬 순서를 보장하려면 order by 절이 필요하다.
  - 주의할 점으로 이 정렬 키가 ResultSet 내에서 중복되지 않아야 한다.
- 데이터 소스 참조 설정


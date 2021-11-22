# Chapter7 ItemReader

## ItemReader 인터페이스

`ItemReader<T>` 인터페이스는 스텝에 입력을 제공할 때 사용하는 `read`라는 단일 메서드를 정의한다.

```java
public interface ItemReader<T> {
   @Nullable
   T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
}
```

스프링 배치가 `ItemReader`의 `read` 메서드를 호출하면, 해당 메서드는 스텝 내에서 처리할 아이템 한 개를 반환한다.

해당 아이템은 구성된 특정 `ItemProcessor`로 전달되면 그 뒤 청크의 일부분에 포함돼 `ItemWriter`로 전달된다.

</br >

## 플랫 파일

플랫 파일이란 한 개 또는 그 이상의 레코드가 포함된 특정 파일을 말한다. 플랫 파일은 파일의 내용을 봐도 데이터의 의미를 알 수 없다는 점에서 XML 파일과 차이가 있다.

플랫 파일에는 파일 내에 데이터의 포맷이나 의미를 정의하는 메타데이터가 없다. 이와 반대로 XML 파일은 태그를 사용해 데이터에 의미를 부여한다.

### FlatFileItemReader

`FlatFileItemReader`는 메인 컴포넌트 두 개로 이뤄진다.

- Resource
  - 읽어들일 대상 파일을 나타내는 스프링의 Resource
- LineMapper
  - 스프링 JDBC에서 `RowMapper`가 담당하는 것과 비슷한 역할을 한다.
  - JDBC에서 `RowMapper`를 사용하면 필드의 묶음을 나타내는 `ResultSet`을 객체로 매핑할 수 있다.

</br >

파일을 읽을 때는 파일에서 레코드 한 개에 해당하는 문자열이 `LineMapper` 구현체에 전달된다.

가장 많이 사용되는 `LineMapper` 구현체는 `DefaultLineMapper`이다. `DefaultLineMapper`는 파일에서 읽은 원시 `String`을 대상으로 두 단계 처리를 거쳐 이후 처리에 사용할 도메인 객체로 변환한다.

이 두 단계 처리는 `LineTokenizer`와 `FieldSetMapper`가 담당한다.

### LineTokenizers

- `LineTokenizer` 구현체가 해당 줄을 파싱해 `FieldSet`으로 만든다.
- `LineTokenizer`에 제공되는 `String`은 파일에서 가져온 한 줄 전체를 나타낸다.
- 레코드 내의 각 필드를 도메인 객체로 매핑하려면 해당 줄을 파싱해 각 필드를 나타내는 데이터의 모음으로 변환할 수 있어야 한다.
- 스프링 배치의 `FieldSet`은 한 줄에 해당하는 필드의 모음을 나타낸다.(`ResultSet`과 유사)

### FieldSetMapper

- `FieldSetMapper` 구현체는 `FieldSet`을 도메인 객체로 매핑한다.
- `LineTokenizer`가 한 줄을 여러 필드로 나눈것을 도메인 객체의 필드로 매핑한다.
- JDBC에서 `RowMapper`가 `ResultSet`의 로우를 도메인 객체로 매핑하는 것과 유사

</br >

## 고정 너비 파일

파일 포맷을 설명할 메타데이터가 전혀 제공되지 않기 때문에 고정 너비 파일을 처리할 때 포맷을 정의하는 것은 매우 중요하다.

```java
@Bean
@StepScope
public FlatFileItemReader<Customer> fixedLengthCustomerItemReader(
        @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
    return new FlatFileItemReaderBuilder<Customer>()
            .name("fixedLengthCustomerItemReader")
            .resource(inputFile)
            .fixedLength()
            .columns(new Range[]{new Range(1, 11), new Range(12, 12), new Range(13, 22),
                    new Range(23, 26), new Range(27, 46), new Range(47, 62), new Range(63, 64),
                    new Range(65, 69)})
            .names("firstName", "middleInitial", "lastName",
                    "addressNumber", "street", "city", "state",
                    "zipCode")
            .targetType(Customer.class)
            .build();
}
```

### FixedLengthTokenizer 빌더

각 줄을 파싱해 `FieldSet`으로 만드는 `LineTokenizer`의 구현체

`FixedLengthTokenizer` 빌더에 필요한 두 가지 구성 항목

1. 레코드 내 각 칼럼의 이름을 지정
2. `Range` 객체의 배열을 지정
   - 각 `Range`의 인스턴스는 파싱해야 할 칼람의 시작 위치와 종료 위치를 나타낸다.

이 외에 추가 항목으로 `FieldSetFactory`와 `strict` 플래그가 있다.

- FieldSetFactory
  - `FieldSet`을 생성하는데 사용되면 `DefaultFieldSetFactory`가 제공됨
- strict 플래그
  - 정의된 파싱 정보보다 많은 항목이 레코드에 포함돼 있을 때의 처리 방법 제공
  - 기본으로 더 많은 항목이 포함돼 있으면 예외를 던지도록 true로 설정되어 있음

![image](https://user-images.githubusercontent.com/43977617/142716207-b10152d9-994f-49f8-afd0-6b4bdf5807f9.png)

![image](https://user-images.githubusercontent.com/43977617/142802583-a02485d9-c699-4e48-b43f-3adcfa68309d.png)

</br >

### customerFixed.txt

```
Aimee      CHoover    7341Vel Avenue          Mobile          AL35928
Jonas      UGilbert   8852In St.              Saint Paul      MN57321
Regan      MBaxter    4851Nec Av.             Gulfport        MS33193
Octavius   TJohnson   7418Cum Road            Houston         TX51507
Sydnee     NRobinson  894 Ornare. Ave         Olathe          KS25606
```

### 실행결과

![image](https://user-images.githubusercontent.com/43977617/142716976-4457c555-f2be-4f26-b5b5-3dae087c5cfc.png)

잡 실행 결과를 보면 지정한 포맷 문자열에 맞춰 출력된다.

</br >

## 필드가 구분자로 구분된 파일

구분자로 구분된 파일에서는 특정 문자를 구분자로 사용해서 레코드 내 각 필드를 구분한다.

구분자로 구분된 레코드를 읽는 방법은 고정 너비 레코드를 읽는 방법과 거의 유사하다.

- `LineTokenizer`를 사용해서 레코드를 `FieldSet`으로 변환한다.
- `FieldSetMapper`를 사용해 `FieldSet`을 사용하려는 도메인 객체로 매핑한다.

`DelimitedLineTokenizer`를 사용해 각 레코드를 `FieldSet`으로 변환한다.

```java
@Bean
@StepScope
public FlatFileItemReader<Customer> delimitedCustomerItemReader(
        @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
    return new FlatFileItemReaderBuilder<Customer>()
            .name("delimitedCustomerItemReader")
            .resource(inputFile)
            .delimited()//default: 쉼표(,)
            .names("firstName", "middleInitial", "lastName",
                    "addressNumber", "street", "city", "state",
                    "zipCode")
            .targetType(Customer.class)
            .build();
}
```

`DelimitedLineTokenizer`에 설정된 구분자 기본값은 쉼표(,)이다.

</br >

### customerDelimited.txt

```
Aimee,C,Hoover,7341,Vel Avenue,Mobile,AL,35928
Jonas,U,Gilbert,8852,In St.,Saint Paul,MN,57321
Regan,M,Baxter,4851,Nec Av.,Gulfport,MS,33193
```

### 실행결과

![image](https://user-images.githubusercontent.com/43977617/142718304-65a6cc35-5834-45a1-a2a2-f8cf528efb82.png)

</br >

`DelimitedLineTokenizer`에 유용한 두 가지 선택 항목을 제공한다.

1. 구분자 설정
   - `delimiter()` 메서드를 통해 설정 가능
2. 인용 문자로 사용할 값 구성
   - `quoteCharacter()` 메서드를 통해 설정 가능

</br >

## 필드 매핑

새로운 객체 형식을 사용하려면 FieldSet을 도메인 객체로 매핑하는 방법을 변경하면 된다. `FieldSetMapper` 인터페이스의 구현체를 새로 만들면 된다.

### FieldSetMapper

`FieldSetMapper` 인터페이스는 `maptFieldSet`이라는 단일 메서드로 구성되어있다.

`maptFieldSet` 메서드는 `LineTokenizer`에서 반환된 `FieldSet`을 도메인 객체의 필드로 매핑하는 데 사용된다.

![image](https://user-images.githubusercontent.com/43977617/142718486-d67f0891-7b63-48e8-a513-aca19a509b19.png)

</br >

FieldSet에서 값을 가져오는 메서드는 두 가지 유형이 있다.

1. 정수 값을 파라미터로 받는 것으로 정수값은 레코드에서 가져올 필드의 인덱스를 나타낸다. (0부터 시작)
2. 필드의 이름을 받는 것이다.

![image](https://user-images.githubusercontent.com/43977617/142718614-0c61ce3e-1f6c-4335-b9c6-61dd12818049.png)

</br >

### CustomerFieldSetMapper

~~~java
public class CustomerFieldSetMapper implements FieldSetMapper<CustomerAddress> {

    @Override
    public CustomerAddress mapFieldSet(FieldSet fieldSet) {
        CustomerAddress customer = new CustomerAddress();

        customer.setAddress(fieldSet.readString("addressNumber") +
                " " + fieldSet.readString("street"));
        customer.setCity(fieldSet.readString("city"));
        customer.setFirstName(fieldSet.readString("firstName"));
        customer.setLastName(fieldSet.readString("lastName"));
        customer.setMiddleInitial(fieldSet.readString("middleInitial"));
        customer.setState(fieldSet.readString("state"));
        customer.setZipCode(fieldSet.readString("zipCode"));

        return customer;
    }
}
~~~

</br >

### FlatFileItemReader

```java
@Bean
@StepScope
public FlatFileItemReader<CustomerAddress> mapperCustomerItemReader(
        @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
    return new FlatFileItemReaderBuilder<CustomerAddress>()
            .name("mapperCustomerItemReader")
            .resource(inputFile)
            .delimited()
            .names("firstName", "middleInitial", "lastName",
                    "addressNumber", "street", "city", "state",
                    "zipCode")
            .fieldSetMapper(new CustomerFieldSetMapper())
            .build();
}
```

</br >

### 실행결과

![image](https://user-images.githubusercontent.com/43977617/142718895-2ebc6a50-3e23-4cc8-baf0-605c2cbbacec.png)

</br >

## 커스텀 레코드 파싱

`LineTokenizer` 구현체를 직접 만들어 원하는 대로 각 레코드를 파싱할 수 있다.

```java
@RequiredArgsConstructor
public class CustomerFileLineTokenizer implements LineTokenizer {

    private static final String DELIMITER = ",";

    private final FieldSetFactory fieldSetFactory;

    private final String[] names = new String[]{"firstName",
            "middleInitial",
            "lastName",
            "address",
            "city",
            "state",
            "zipCode"};

    /***
     * 구분자를 통해 여러 필드를 만든 후
     * 세 번째와 네 번째 필드를 묶어 단일 필드로 합친다.
     */
    @Override
    public FieldSet tokenize(String record) {
        String[] fields = record.split(DELIMITER);
        List<String> parsedFields = new ArrayList<>();
        for (int idx = 0; idx < fields.length; idx++) {
            if (idx == 4) {
                parsedFields.set(idx - 1, parsedFields.get(idx - 1) + " " + fields[idx]);
            } else {
                parsedFields.add(fields[idx]);
            }
        }
        return fieldSetFactory.create(parsedFields.toArray(new String[0]), names);
    }
}
```

</br >

### FlatFileItemReader

```java
@Bean
@StepScope
public FlatFileItemReader<CustomerAddress> customLineTokenizerItemReader(
        @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
    return new FlatFileItemReaderBuilder<CustomerAddress>()
            .name("customLineTokenizerItemReader")
            .lineTokenizer(new CustomerFileLineTokenizer(new DefaultFieldSetFactory()))
            .targetType(CustomerAddress.class)
            .resource(inputFile)
            .build();
}
```

</br >

### 실행 결과

![image](https://user-images.githubusercontent.com/43977617/142719718-60ccba0f-4ac6-448f-96ae-cff09dfb9e2a.png)

</br >

## 여러 가지 레코드 포맷

파일 내 각 레코드가 동일한 포맷이 아닐때가 있다.

1. 복잡도
   - 파일 내에 여러 가지 레코드 포맷이 존재할때
   - 각 레코드 포맷마다 무수히 많은 필드가 포함되어 있을 때
2. 관심사 분리
   - LineTokenizer의 목적은 레코드를 파싱하는 것 그 이상도 이하도 아니다.
   - 레코드 파싱을 넘어 어떤 레코드 유형인지를 판별하는 데 사용해서는 안된다.

스프링 배치는 이런 점을 감안해 별도의 `LineMapper` 구현체인 `PatterMatchingCompositeLineMapper`를 제공한다.

`PatterMatchingCompositeLineMapper`를 사용하면 여러 `LineTokenizer`로 구성된 `Map`을 선언할 수 있으며, 각 `LineTokenizer`가 필요로 하는 여러 `FieldSetMapper`로 구성된 `Map`을 선언할 수 있다.

각 맵의 키는 레코드의 패턴이다. **`LineMapper`는 이 패턴을 이용해서 각 레코드를 어떤 `LineTokenizer`로 파싱할지 식별한다.**

### inputFile

```
CUST,Warren,Q,Darrow,8272 4th Street,New York,IL,76091
TRANS,1165965,2011-01-22 00:13:29,51.43
CUST,Ann,V,Gates,9247 Infinite Loop Drive,Hollywood,NE,37612
CUST,Erica,I,Jobs,8875 Farnam Street,Aurora,IL,36314
TRANS,8116369,2011-01-21 20:40:52,-14.83
TRANS,8116369,2011-01-21 15:50:17,-45.45
```

위 파일은 두 가지 포맷으로 구성돼 있다.

1. 건물 번호와 거리명이 합쳐져 있는 고객 정보 포맷 (접두어 CUST)
2. 거래 래코드로 쉼표로 구분된 세 가지 필드가 있다. (접두어 TRANS)
   - 계좌번호
   - 거래일시
   - 금액

</br >

`PatternMatchingCompositeLineMapper`를 사용하여, `LineTokenizer` `Map`에는 `DelimitedLineTokenizer` 인스턴스 두 개를, 각 `DelimitedLineTokenizer`에는 레코드 포맷에 맞는 필드를 구성하면 된다.

```java
    @Bean
    public PatternMatchingCompositeLineMapper patternMatchingLineMapper() {
        Map<String, LineTokenizer> lineTokenizerMap = new HashMap<>(2);
        lineTokenizerMap.put("CUST*", customerLineTokenizer()); //고객 정보 포맷
        lineTokenizerMap.put("TRANS*", transactionLineTokenizer()); //거래 레코드 포맷

        Map<String, FieldSetMapper> fieldSetMapperMap = new HashMap<>(2);
        BeanWrapperFieldSetMapper<CustomerAddress> customerFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        customerFieldSetMapper.setTargetType(CustomerAddress.class);

        fieldSetMapperMap.put("CUST*", customerFieldSetMapper);
        fieldSetMapperMap.put("TRANS*", new TransactionFieldSetMapper());

        PatternMatchingCompositeLineMapper lineMapper = new PatternMatchingCompositeLineMapper();
        lineMapper.setTokenizers(lineTokenizerMap);
        lineMapper.setFieldSetMappers(fieldSetMapperMap);

        return lineMapper;
    }

    @Bean
    public DelimitedLineTokenizer customerLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("firstName",
                "middleInitial",
                "lastName",
                "address",
                "city",
                "state",
                "zipCode");
        lineTokenizer.setIncludedFields(1, 2, 3, 4, 5, 6, 7); //prefix 무시 (인덱스 시작: 0)
        return lineTokenizer;
    }

    @Bean
    public DelimitedLineTokenizer transactionLineTokenizer() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("prefix", "accountNumber", "transactionDate", "amount");
        return lineTokenizer;
    }
```

### PatternMatchingCompositeLineMapper 흐름

- 레코드가 CUST*로 시작하면 해당 레코드를 `customerLineTokenizer`에 전달해 파싱
- 레코드를 `FieldSet`으로 파싱한 뒤 파싱된 `FieldSet`을 `FieldSetMapper`로 전달
  - 매핑 작업에는 프레임워크가 제공하는 `BeanWrapperFieldSetMapper`를 사용

</br >

거레 레코드를 위한 `FieldSetMapper`를 보면 커스텀화 한 것을 볼 수 있다. `TransactionFieldSetMapper`는 다음과 같다.

~~~java
public class TransactionFieldSetMapper implements FieldSetMapper<Transaction> {

    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) throws BindException {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(fieldSet.readString("accountNumber"));
        transaction.setAmount(fieldSet.readDouble("amount"));
        transaction.setTransactionDate(fieldSet.readDate("transactionDate", "yyyy-MM-dd HH:mm:ss"));
        return transaction;
    }
}
~~~

커스텀 FieldSetMapper가 필요한 이유는 일반적이지 않은 타입의 필드를 변환할 때 필요하다.

BeanWrapperFieldSetMapper가 특수한 타입의 필드를 변환할 수 없기 때문이다.

여기서 `Transaction`객체는 `String` 타입 외에 `Double`, `Date` 타입이 필요하기 때문에 커스텀 `FieldSetMapper`를 만들어 사용했다.

</br >

### 실행 결과

![image](https://user-images.githubusercontent.com/43977617/142751788-6bd51e6e-036e-4728-8c95-69183de77147.png)

</br >

## 여러 줄에 걸친 레코드

이번에는 각 레코드를 독립적으로 처리하는 대신 `Customer` 객체가 내부에 `Transaction` 객체의 컬렉션을 가지고 있도록 처리하도록 해보자.

### CustomerFileReader.java

```java
public class CustomerFileReader implements ItemStreamReader<CustomerTransactions> {

    private Object curItem = null;
    private ItemStreamReader<Object> delegate;

    public CustomerFileReader(ItemStreamReader<Object> delegate) {
        this.delegate = delegate;
    }

    /***
     * 파일에서 고객 레코드를 읽음
     * 다음 고객 레코드를 만나기 전까지 현재 처리 중인 고객 레코드와 관련된 거래 내역 레코드를 한 줄씩 읽어들임
     * 고객 레코드를 발견하면 현재 처리 중인 고객의 레코드의 처리가 끝난 것으로 간주해 커스텀 ItemReader로 반환
     */
    @Override
    public CustomerTransactions read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (curItem == null) {
            curItem = delegate.read();
        }
        CustomerTransactions item = (CustomerTransactions) curItem;
        curItem = null;

        if (item != null) {
            item.setTransactionList(new ArrayList<>());

            while (peek() instanceof Transaction) {
                item.getTransactionList().add((Transaction) curItem);
                curItem = null;
            }
        }
        return item;
    }

    /***
     * 현재 레코드를 캐시(curItem)에 저장
     */
    private Object peek() throws Exception {
        if (curItem == null) {
            curItem = delegate.read();
        }
        return curItem;
    }
    //...
}
```

</br >

### Customertransactions toString 메서드 오버라이드

```java
@Override
public String toString() {
    StringBuilder output = new StringBuilder();

    output.append(firstName);
    output.append(" ");
    output.append(middleInitial);
    output.append(". ");
    output.append(lastName);

    if (transactionList != null && transactionList.size() > 0) {
        output.append(" has ");
        output.append(transactionList.size());
        output.append(" transactions.");
    } else {
        output.append(" has no transactions.");
    }

    return output.toString();
}
```

</br >

### 주의할 점

위 코드는 `ItemReader`가 반환하는 객체는 `CustomerTransactions` 객체다. 따라서 `CustomerTransactions` 객체는 커밋 카운트 등에서 사용되는 아이템으로 취급된다.

각 `CustomerTransactions` 객체는 구성된 특정 `ItemProcessor`에서 한 번만 처리되면 마찬가지로 구성된 특정 `ItemWriter`에서 한 번만 처리된다.

### customerMultiFormat1.csv

~~~
CUST,Warren,Q,Darrow,8272 4th Street,New York,IL,76091
TRANS,1165965,2011-01-22 00:13:29,51.43
CUST,Ann,V,Gates,9247 Infinite Loop Drive,Hollywood,NE,37612
CUST,Erica,I,Jobs,8875 Farnam Street,Aurora,IL,36314
TRANS,8116369,2011-01-21 20:40:52,-14.83
TRANS,8116369,2011-01-21 15:50:17,-45.45
TRANS,8116369,2011-01-21 16:52:46,-74.6
TRANS,8116369,2011-01-22 13:51:05,48.55
TRANS,8116369,2011-01-21 16:51:59,98.53
~~~

읽어들일 파일을 보면 `CustomerTransactions` 객체와 매핑되는 레코드(prefix: CUST)가 3개가 있다.

</br >

### 테스트코드

~~~java
@DisplayName("ItemStreamReader 인터페이스 구현을 통한 여러 레코드 포맷 파일 읽기")
@Test
public void customerFileItemReaderTest() throws Exception {
    //given
    final JobParameters jobParameters = new JobParametersBuilder()
            .addString("customerFile", "input/customerMultiFormat1.csv")
            .toJobParameters();

    //when
    final JobExecution execution = jobLauncher.launchJob(PatternMatchingJobConfiguration.JOB_NAME, jobParameters);
    final List<StepExecution> stepExecutions = new ArrayList<>(execution.getStepExecutions());

    //then
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(stepExecutions.get(0).getReadCount()).isEqualTo(3);
    assertThat(stepExecutions.get(0).getWriteCount()).isEqualTo(3);
}
~~~

![image](https://user-images.githubusercontent.com/43977617/142754462-264dc9c1-d964-4c04-a55e-de6ba4696a74.png)

</br >

## 여러 개의 소스

`MultiResourceItemReader`를 사용하여 한 번에 여러 파일을 처리할 수 있다.

```java
@Bean
@StepScope
public MultiResourceItemReader customerMultiFileItemReader(
        @Value("#{jobParameters['customerFile']}") ClassPathResource[] inputFiles) {
    return new MultiResourceItemReaderBuilder<>()
            .name("customerMultiFileItemReader")
            .resources(inputFiles)
            .delegate(customerMultiFileReader())
            .build();
}
```

MultiResourceItemReader는 주요 컴포넌트 세 개를 전달받는다.

1. reader의 이름
2. Resouce 객체의 배열 (읽어들여야 할 파일 목록)
3. 실제 작업을 수행할 위임 컴포넌트

`MultiResourceItemReader`를 보면 읽어들일 `Resource`를 입력받는다. 그러므로 `FlatFileItemReader`는 따로 `Resource`를 입력받지 않아도 된다.

```java
@Bean
public CustomerFileReader customerMultiFileReader() {
    return new CustomerFileReader(customerMultiItemReader());
}

@Bean
public FlatFileItemReader customerMultiItemReader() {
    return new FlatFileItemReaderBuilder<CustomerTransactions>()
            .name("customerMultiItemReader")
            .lineMapper(customerTransactionsLineMapper())
            .build();
}
```

</br >

기존 `CustomerFileReader`는 `ItemStreamReader` 인터페이스를 구현하고 위임 컴포넌트의 타입으로 `ItemStreamReader` 인터페이스를 사용했다.

여기서는 `ResourceAwareItemReaderItemStream` 인터페이스를 사용해야 한다. 해당 인터페이스는 리소스에서 입력을 읽는 모든 `ItemReader`를 지원한다.

```java
public class CustomerFileReader implements ResourceAwareItemReaderItemStream<CustomerTransactions> {

    private Object curItem = null;
    private ResourceAwareItemReaderItemStream<Object> delegate;

    public CustomerFileReader(ResourceAwareItemReaderItemStream<Object> delegate) {
        this.delegate = delegate;
    }

    //...

    @Override
    public void setResource(Resource resource) {
        this.delegate.setResource(resource);
    }
}
```

</br >

### 실행 결과

![image](https://user-images.githubusercontent.com/43977617/142762716-d026c18d-9fbd-4956-92d5-ab68a9a35208.png)

</br >

주의할 점으로 `customerMultiFormat1.csv`, `customerMultiFormat2.csv`, `customerMultiFormat3.csv` 이 세 가지 파일을 처리하는 잡을 시작했는데 `customerMultiFormat2.csv` 파일을 처리하는 과정에서 에러가 발생했다고 가정하자.

이후 잡이 재시작 되기 전에 `customerMultiFormat4.csv` 파일이 추가됐다. 잡이 처음 실행될 때는 `customerMultiFormat4.csv`가 존재하지 않았음에도 다시 시작하는 잡은 `customerMultiFormat4.csv` 파일도 실행 대상으로 보고 처리한다.

이러한 문제를 해결하기 위해 각 배치 실행 시 사용할 디렉터리를 별도로 생성하는 것이 좋다.

</br >

## XML

XML은 파일 내 데이터를 설명할 수 있는 태그를 사용해서 파일에 포함된 데이터를 설명한다.

XML 파서로 DOM 파서와 SAX 파서를 많이 사용한다.

- DOM 파서는 XML문서를 메모리에 모두 로드한 후 값을 채워 넣고 트리 구조로 읽어 들인다. 그렇기 때문에 성능상 큰 부하가 발생할 수 있다.
- SAX 파서는 특정 엘리먼트를 만나면 이벤트를 발생시키는 이벤트 기반 파서다.
  - XML 문서를 메모리에 전부 로딩하는 것이 아니라서 메모리 사용량이 적고 단순히 읽기만 할 때 속도가 빠르다.
- [DOM vs SAX](https://www.geeksforgeeks.org/difference-between-sax-parser-and-dom-parser-in-java/)

스프링 배치에서는 StAX 파서를 사용한다. StAX 파서도 SAX 파서와 비슷한 이벤트 기반 파서이긴 하지만 XML 문서 내 각 섹션을 독립적으로 파싱하는 기능을 제공한다.

StAX를 사용하면 한 번에 처리해야 할 아이템을 나타내는 파일 내 각 섹션을 읽을 수 있다.

</br >

### customer.xml

~~~xml
<customers>
    <customer>
        <firstName>Laura</firstName>
        <middleInitial>O</middleInitial>
        <lastName>Minella</lastName>
        <address>2039 Wall Street</address>
        <city>Omaha</city>
        <state>IL</state>
        <zipCode>35446</zipCode>
        <transactions>
            <transaction>
                <accountNumber>829433</accountNumber>
                <transactionDate>2010-10-14 05:49:58</transactionDate>
                <amount>26.08</amount>
            </transaction>
        </transactions>
    </customer>
    <customer>
        <firstName>Michael</firstName>
        <middleInitial>T</middleInitial>
        <lastName>Buffett</lastName>
        <address>8192 Wall Street</address>
        <city>Omaha</city>
        <state>NE</state>
        <zipCode>25372</zipCode>
        <transactions>
            <transaction>
                <accountNumber>8179238</accountNumber>
                <transactionDate>2010-10-27 05:56:59</transactionDate>
                <amount>-91.76</amount>
            </transaction>
            <transaction>
                <accountNumber>8179238</accountNumber>
                <transactionDate>2010-10-06 21:51:05</transactionDate>
                <amount>-25.99</amount>
            </transaction>
        </transactions>
    </customer>
</customers>
~~~

XML을 처리할 때 스프링 배치는 사용자가 정의한 XML 프래그먼트를 도메인 객체로 파싱한다.

> XML 프래그먼트: 시작 태그부터 종료 태그까지의 XML 블록

파일 내에서 미리 지정한 XML 프래그먼트를 만날 때마다 단일 레코드로 간주하고 처리 대상 아이템으로 변환한다.

</br >

### CustomerXML.java

```java
@XmlRootElement(name = "customer") //매칭되는 리먼트 지정
public class CustomerXML {

    private String firstName;
    private String middleInitial;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private List<Transaction> transactionList;

    @XmlElementWrapper(name = "transactions") // 감싸져 있는 앨리먼트앨 지정
    @XmlElement(name = "transaction") //컬렉션 내 각 앨리먼트 지정
    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }
    //...
}
```

</br >

### Job Code

```java
@Configuration
@RequiredArgsConstructor
public class XMLJobConfiguration {
  
    //...

    @Bean(name = STEP_NAME)
    public Step XMLStep() {
        return this.stepBuilderFactory.get(STEP_NAME)
                .<CustomerXML, CustomerXML>chunk(10)
                .reader(XMLFileReader(null))
                .writer(XMLitemWriter())
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<CustomerXML> XMLFileReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
        return new StaxEventItemReaderBuilder<CustomerXML>()
                .name("XMLFileReader")
                .resource(inputFile)
                .addFragmentRootElements("customer") //시작 프레그먼트 설정
                .unmarshaller(customerMarshaller())
                .build();
    }

    //대상 클래스를 알려주는 코드
    @Bean
    public Jaxb2Marshaller customerMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(new Class[]{CustomerXML.class, Transaction.class});
        return jaxb2Marshaller;
    }

    //...
}
```

</br >

### 실행 결과

![image](https://user-images.githubusercontent.com/43977617/142764557-527ab726-f7e9-4061-901c-7a5b9d50bb5a.png)

</br >

## JSON

`JsonItemReader`를 사용하는 방법과 `JsonItemReader`가 제공하는 기능을 사용하여 JOSN 문서를 읽을 수 있다.

### JsonItemReader

- `JsonItemReader`는 JOSN 청크를 읽어서 객체로 파싱한다.
- `JsonItemReader`가 동작할 때 실제 파싱 작업은 `JsonObjectReader` 인터페이스 구현체에게 위임된다.
- 스프링 배치에서 `JsonObjectReader` 인터페이스 구현체 두 개를 제공한다.
  - Jackson
  - Gson

</br >

### customer.json

```json
[
  {
    "firstName": "Laura",
    "middleInitial": "O",
    "lastName": "Minella",
    "address": "2039 Wall Street",
    "city": "Omaha",
    "state": "IL",
    "zipCode": "35446",
    "transactionList": [
      {
        "accountNumber": 829433,
        "transactionDate": "2010-10-14 05:49:58",
        "amount": 26.08
      }
    ]
  },
  {
    "firstName": "Michael",
    "middleInitial": "T",
    "lastName": "Buffett",
    "address": "8192 Wall Street",
    "city": "Omaha",
    "state": "NE",
    "zipCode": "25372",
    "transactionList": [
      {
        "accountNumber": 8179238,
        "transactionDate": "2010-10-27 05:56:59",
        "amount": -91.76
      },
      {
        "accountNumber": 8179238,
        "transactionDate": "2010-10-06 21:51:05",
        "amount": -25.99
      }
    ]
  }
]
```

json 문서를 보면 객체로 구성된 배열이 최상단에 하나만 존재한다. 이렇게 배열이 최상단에 하나만 존재하는 완전항 형태의 문서여야 `JosnItemReader`가 올바르게 동작한다.

</br >

### JsonItemReader 구성

`JsonItemReader`를 구성하기 위해 빌더를 사용한다. 빌더에는 세 가지 의존성이 필요하다.

1. 배치를 재시작할 때 사용되는 배치 이름
2. 파싱에 사용한 `JsonObjectReader`
3. 읽어들일 리소스

```java
@Bean
@StepScope
public JsonItemReader<CustomerTransactions> jsonFileReader(
        @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));

    JacksonJsonObjectReader<CustomerTransactions> jsonObjectReader = new JacksonJsonObjectReader<>(CustomerTransactions.class);
    jsonObjectReader.setMapper(objectMapper);

    return new JsonItemReaderBuilder<CustomerTransactions>()
            .name("jsonFileReader")
            .jsonObjectReader(jsonObjectReader)
            .resource(inputFile)
            .build();
}
```

- `ObjectMapper`는 Jackson이 Json을 읽고 쓰는데 사용한다.
- `JacksonJsonObjectReader`를 생성한다. `JacksonJsonObjectReader`에 반환할 클래스(`CustomerTransactions`) 및 `ObjectMapper`를 설정해준다.
- 마지막으로 `JosnItemReader`를 생성한다. 이때 빌더에 필요한 세 가지 의존성을 설정해주면 된다.

</br >

### 실행 결과

![image](https://user-images.githubusercontent.com/43977617/142767305-0537fd32-3bd7-4392-9687-85dfc4575c5a.png)

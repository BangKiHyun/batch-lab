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

이 두 단계 처리는 `LineTokenizer`와 `FiledSetMapper`가 담당한다.

### LineTokenizer

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

![image-20211120145654673](/Users/bang/Library/Application Support/typora-user-images/image-20211120145654673.png)

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


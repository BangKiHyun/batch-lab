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


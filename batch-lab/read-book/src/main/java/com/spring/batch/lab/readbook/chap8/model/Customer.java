package com.spring.batch.lab.readbook.chap8.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Customer {

    @NotNull(message="First name is required")
    @Pattern(regexp="[a-zA-Z]+", message="First name must be alphabetical")
    private String firstName;

    @Size(min=1, max=1)
    @Pattern(regexp="[a-zA-Z]", message="Middle initial must be alphabetical")
    private String middleInitial;

    @NotNull(message="Last name is required")
    @Pattern(regexp="[a-zA-Z]+", message="Last name must be alphabetical")
    private String lastName;

    @NotNull(message="Address is required")
    @Pattern(regexp="[0-9a-zA-Z\\. ]+")
    private String address;

    @NotNull(message="City is required")
    @Pattern(regexp="[a-zA-Z\\. ]+")
    private String city;

    @NotNull(message="State is required")
    @Size(min=2,max=2)
    @Pattern(regexp="[A-Z]{2}")
    private String state;

    // @Size, @Pattern나눈 이유
    // @Pattern 애너테이션 하나만으로 충분히 커버할 수 있지만
    // 각각 적용하면 원하는 고유한 메시지를 지정할 수 있고, 필드 값의 길이가 잘못됐는지 또는 형식이 잘못됐는지 식별할 수 있다.
    @NotNull(message="Zip is required")
    @Size(min=5,max=5)
    @Pattern(regexp="\\d{5}")
    private String zip;

    public Customer(Customer original) {
        this.firstName = original.getFirstName();
        this.middleInitial = original.getMiddleInitial();
        this.lastName = original.getLastName();
        this.address = original.getAddress();
        this.city = original.getCity();
        this.state = original.getState();
        this.zip = original.getZip();
    }
}

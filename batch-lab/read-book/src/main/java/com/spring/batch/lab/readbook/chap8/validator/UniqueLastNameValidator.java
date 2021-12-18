package com.spring.batch.lab.readbook.chap8.validator;

import com.spring.batch.lab.readbook.chap8.model.Customer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// lastName이 고유해야 한다는 가정하에 해당 상태를 검증해주는 Custom Validator
public class UniqueLastNameValidator extends ItemStreamSupport
        implements Validator<Customer> {

    private Set<String> lastNames = new HashSet<>();

    @Override
    public void validate(Customer value) throws ValidationException {
        if (lastNames.contains(value.getLastName())) {
            throw new ValidationException("Duplicate last name was found: " + value.getLastName());
        }
        this.lastNames.add(value.getLastName());
    }

    // Execution 간 상태를 유지하기 위해 사용
    @Override
    public void open(ExecutionContext executionContext) {
        String lastNames = getExecutionContextKey("lastNames");

        // 만약 저장돼어있는 lastName이면 스텝 처리가 시작되기 전에 해당 값으로 원복
        if (executionContext.containsKey(lastNames))
            this.lastNames = (Set<String>) executionContext.get(lastNames);
    }

    // Execution 간 상태를 유지하기 위해 사용
    // 트랜잭션이 커밋되면 청크당 한 번 호출
    // 다음 청크에 오류가 발생할 경우 현재 상태를 저장
    @Override
    public void update(ExecutionContext executionContext) {
        Iterator<String> itr = lastNames.iterator();
        HashSet<String> copiedLastNames = new HashSet<>();
        while (itr.hasNext()) {
            copiedLastNames.add(itr.next());
        }
        executionContext.put(getExecutionContextKey("lastNames"), copiedLastNames);
    }
}

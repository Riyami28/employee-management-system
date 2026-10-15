package com.kloudspot.ems.dto;

import com.kloudspot.ems.TestData;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void validRequest() {
        assertThat(validator.validate(TestData.employeeRequest())).isEmpty();
    }

    @Test
    void allFieldsRequired() {
        Set<ConstraintViolation<EmployeeRequest>> violations = validator.validate(new EmployeeRequest());

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("fullName", "email", "phoneNumber", "department", "designation",
                        "dateOfJoining", "salary", "status");
    }

    @Test
    void blankTextRejected() {
        EmployeeRequest request = TestData.employeeRequest();
        request.setFullName("   ");
        request.setDepartment("");

        assertThat(fieldsWithErrors(request)).contains("fullName", "department");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "missing-at.example.com", "name@domain", "two@@example.com"})
    void invalidEmailRejected(String email) {
        EmployeeRequest request = TestData.employeeRequest();
        request.setEmail(email);

        assertThat(fieldsWithErrors(request)).containsExactly("email");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "98765-43210", "phone-number", "+91 98765 43210", "1234567890123456"})
    void invalidPhoneRejected(String phone) {
        EmployeeRequest request = TestData.employeeRequest();
        request.setPhoneNumber(phone);

        assertThat(fieldsWithErrors(request)).containsExactly("phoneNumber");
    }

    @ParameterizedTest
    @ValueSource(strings = {"9876543210", "+919876543210", "02212345678"})
    void validPhoneAccepted(String phone) {
        EmployeeRequest request = TestData.employeeRequest();
        request.setPhoneNumber(phone);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void salaryMustNotBeNegative() {
        EmployeeRequest request = TestData.employeeRequest();

        request.setSalary(new BigDecimal("-1"));
        assertThat(fieldsWithErrors(request)).containsExactly("salary");

        request.setSalary(BigDecimal.ZERO);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void salaryPrecision() {
        EmployeeRequest request = TestData.employeeRequest();
        request.setSalary(new BigDecimal("1000.123"));

        assertThat(fieldsWithErrors(request)).containsExactly("salary");
    }

    @Test
    void nameTooLong() {
        EmployeeRequest request = TestData.employeeRequest();
        request.setFullName("A".repeat(101));

        assertThat(fieldsWithErrors(request)).containsExactly("fullName");
    }

    private Set<String> fieldsWithErrors(EmployeeRequest request) {
        return validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}

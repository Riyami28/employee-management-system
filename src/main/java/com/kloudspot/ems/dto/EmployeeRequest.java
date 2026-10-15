package com.kloudspot.ems.dto;

import com.kloudspot.ems.entity.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeRequest {

    public static final String PHONE_REGEX = "^\\+?[0-9]{10,15}$";

    @Schema(example = "Ananya Sharma")
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Schema(example = "ananya.sharma@example.com")
    @NotBlank(message = "Email address is required")
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Please enter a valid email address")
    @Size(max = 150, message = "Email address must not exceed 150 characters")
    private String email;

    @Schema(example = "+919876543210")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = PHONE_REGEX, message = "Phone number must contain 10 to 15 digits and may start with +")
    private String phoneNumber;

    @Schema(example = "Engineering")
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Schema(example = "Software Engineer")
    @NotBlank(message = "Designation is required")
    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    @Schema(example = "2024-07-15")
    @NotNull(message = "Date of joining is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfJoining;

    @Schema(example = "850000.00")
    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", message = "Salary must not be negative")
    @Digits(integer = 10, fraction = 2, message = "Salary can have at most 10 digits and 2 decimal places")
    private BigDecimal salary;

    @Schema(example = "ACTIVE")
    @NotNull(message = "Employment status is required")
    private EmploymentStatus status;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public EmploymentStatus getStatus() {
        return status;
    }

    public void setStatus(EmploymentStatus status) {
        this.status = status;
    }
}

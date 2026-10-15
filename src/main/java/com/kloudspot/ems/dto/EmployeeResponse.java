package com.kloudspot.ems.dto;

import com.kloudspot.ems.entity.EmploymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "EMP-1001")
    private String employeeId;

    @Schema(example = "Ananya Sharma")
    private String fullName;

    @Schema(example = "ananya.sharma@example.com")
    private String email;

    @Schema(example = "+919876543210")
    private String phoneNumber;

    @Schema(example = "Engineering")
    private String department;

    @Schema(example = "Software Engineer")
    private String designation;

    @Schema(example = "2024-07-15")
    private LocalDate dateOfJoining;

    @Schema(example = "850000.00")
    private BigDecimal salary;

    @Schema(example = "ACTIVE")
    private EmploymentStatus status;

    @Schema(description = "UTC", example = "2026-10-15T06:37:12")
    private LocalDateTime createdAt;

    @Schema(description = "UTC", example = "2026-10-15T06:37:12")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

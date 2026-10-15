package com.kloudspot.ems;

import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.entity.Employee;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.mapper.EmployeeMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestData {

    private TestData() {
    }

    public static EmployeeRequest employeeRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setFullName("Ananya Sharma");
        request.setEmail("ananya.sharma@example.com");
        request.setPhoneNumber("+919876543210");
        request.setDepartment("Engineering");
        request.setDesignation("Software Engineer");
        request.setDateOfJoining(LocalDate.of(2024, 7, 15));
        request.setSalary(new BigDecimal("850000.00"));
        request.setStatus(EmploymentStatus.ACTIVE);
        return request;
    }

    public static EmployeeRequest employeeRequest(String fullName, String email, String department,
                                                  EmploymentStatus status) {
        EmployeeRequest request = employeeRequest();
        request.setFullName(fullName);
        request.setEmail(email);
        request.setDepartment(department);
        request.setStatus(status);
        return request;
    }

    public static Employee savedEmployee(Long id) {
        return savedEmployee(id, employeeRequest());
    }

    public static Employee savedEmployee(Long id, EmployeeRequest request) {
        Employee employee = new Employee();
        EmployeeMapper.updateEntity(employee, request);
        employee.setId(id);
        employee.assignEmployeeCode();
        return employee;
    }

    public static EmployeeResponse employeeResponse(Long id) {
        return EmployeeMapper.toResponse(savedEmployee(id));
    }
}

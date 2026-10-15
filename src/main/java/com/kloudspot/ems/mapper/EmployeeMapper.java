package com.kloudspot.ems.mapper;

import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.entity.Employee;

import java.util.Locale;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeId(employee.getEmployeeCode());
        response.setFullName(employee.getFullName());
        response.setEmail(employee.getEmail());
        response.setPhoneNumber(employee.getPhoneNumber());
        response.setDepartment(employee.getDepartment());
        response.setDesignation(employee.getDesignation());
        response.setDateOfJoining(employee.getDateOfJoining());
        response.setSalary(employee.getSalary());
        response.setStatus(employee.getStatus());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    public static EmployeeRequest toRequest(EmployeeResponse employee) {
        EmployeeRequest request = new EmployeeRequest();
        request.setFullName(employee.getFullName());
        request.setEmail(employee.getEmail());
        request.setPhoneNumber(employee.getPhoneNumber());
        request.setDepartment(employee.getDepartment());
        request.setDesignation(employee.getDesignation());
        request.setDateOfJoining(employee.getDateOfJoining());
        request.setSalary(employee.getSalary());
        request.setStatus(employee.getStatus());
        return request;
    }

    public static void updateEntity(Employee employee, EmployeeRequest request) {
        employee.setFullName(request.getFullName().trim());
        employee.setEmail(normalizeEmail(request.getEmail()));
        employee.setPhoneNumber(request.getPhoneNumber().trim());
        employee.setDepartment(request.getDepartment().trim());
        employee.setDesignation(request.getDesignation().trim());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setSalary(request.getSalary());
        employee.setStatus(request.getStatus());
    }

    // Emails are stored in lower case so the uniqueness check ignores case.
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}

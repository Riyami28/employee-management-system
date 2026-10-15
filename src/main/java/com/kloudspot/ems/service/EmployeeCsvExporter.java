package com.kloudspot.ems.service;

import com.kloudspot.ems.dto.EmployeeResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeCsvExporter {

    static final String HEADER = "Employee ID,Full Name,Email,Phone Number,Department,Designation,"
            + "Date of Joining,Salary,Status";

    public String toCsv(List<EmployeeResponse> employees) {
        StringBuilder csv = new StringBuilder(HEADER).append("\r\n");
        for (EmployeeResponse employee : employees) {
            csv.append(String.join(",",
                            escape(employee.getEmployeeId()),
                            escape(employee.getFullName()),
                            escape(employee.getEmail()),
                            escape(employee.getPhoneNumber()),
                            escape(employee.getDepartment()),
                            escape(employee.getDesignation()),
                            escape(String.valueOf(employee.getDateOfJoining())),
                            escape(employee.getSalary().toPlainString()),
                            escape(employee.getStatus().getDisplayName())))
                    .append("\r\n");
        }
        return csv.toString();
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        String safeValue = value;
        // Stop spreadsheets from running values like "=SUM(...)" as formulas.
        if (!safeValue.isEmpty() && "=+-@".indexOf(safeValue.charAt(0)) >= 0 && !isPhoneNumber(safeValue)) {
            safeValue = "'" + safeValue;
        }
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n") || safeValue.contains("\r")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private static boolean isPhoneNumber(String value) {
        return value.matches("\\+[0-9]+");
    }
}

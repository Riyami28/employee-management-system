package com.kloudspot.ems.dto;

import com.kloudspot.ems.entity.EmploymentStatus;

// Any value may be null or blank; those filters are ignored.
public record EmployeeFilter(String search, String department, EmploymentStatus status) {

    public static EmployeeFilter empty() {
        return new EmployeeFilter(null, null, null);
    }
}

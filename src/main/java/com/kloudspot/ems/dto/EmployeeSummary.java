package com.kloudspot.ems.dto;

public class EmployeeSummary {

    private final long totalEmployees;
    private final long activeEmployees;
    private final long inactiveEmployees;
    private final long totalDepartments;

    public EmployeeSummary(long totalEmployees, long activeEmployees, long inactiveEmployees, long totalDepartments) {
        this.totalEmployees = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.inactiveEmployees = inactiveEmployees;
        this.totalDepartments = totalDepartments;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public long getActiveEmployees() {
        return activeEmployees;
    }

    public long getInactiveEmployees() {
        return inactiveEmployees;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }
}

package com.kloudspot.ems.service;

import com.kloudspot.ems.TestData;
import com.kloudspot.ems.dto.EmployeeResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeCsvExporterTest {

    private final EmployeeCsvExporter exporter = new EmployeeCsvExporter();

    @Test
    void writesHeaderAndRows() {
        EmployeeResponse employee = TestData.employeeResponse(1L);

        String csv = exporter.toCsv(List.of(employee));

        assertThat(csv.split("\r\n")).containsExactly(
                EmployeeCsvExporter.HEADER,
                "EMP-1001,Ananya Sharma,ananya.sharma@example.com,+919876543210,Engineering,Software Engineer,"
                        + "2024-07-15,850000.00,Active");
    }

    @Test
    void emptyList() {
        assertThat(exporter.toCsv(List.of())).isEqualTo(EmployeeCsvExporter.HEADER + "\r\n");
    }

    @Test
    void quotesSpecialCharacters() {
        assertThat(EmployeeCsvExporter.escape("Sales, North")).isEqualTo("\"Sales, North\"");
        assertThat(EmployeeCsvExporter.escape("The \"Boss\"")).isEqualTo("\"The \"\"Boss\"\"\"");
    }

    @Test
    void preventsFormulaInjection() {
        assertThat(EmployeeCsvExporter.escape("=SUM(A1:A9)")).isEqualTo("'=SUM(A1:A9)");
        assertThat(EmployeeCsvExporter.escape("@cmd")).isEqualTo("'@cmd");
        assertThat(EmployeeCsvExporter.escape("+919876543210")).isEqualTo("+919876543210");
    }
}

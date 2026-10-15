package com.kloudspot.ems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudspot.ems.TestData;
import com.kloudspot.ems.dto.EmployeeFilter;
import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.dto.PageResponse;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.exception.DuplicateEmailException;
import com.kloudspot.ems.exception.EmployeeNotFoundException;
import com.kloudspot.ems.exception.InvalidRequestException;
import com.kloudspot.ems.service.EmployeeCsvExporter;
import com.kloudspot.ems.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EmployeeRestController.class, DepartmentRestController.class})
@Import(EmployeeCsvExporter.class)
class EmployeeRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestData.employeeRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value("EMP-1001"))
                .andExpect(jsonPath("$.fullName").value("Ananya Sharma"))
                .andExpect(jsonPath("$.dateOfJoining").value("2024-07-15"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createWithInvalidDataReturns400WithFieldErrors() throws Exception {
        EmployeeRequest request = TestData.employeeRequest();
        request.setFullName("");
        request.setEmail("not-an-email");
        request.setPhoneNumber("123");
        request.setSalary(new java.math.BigDecimal("-5"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/employees"))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").value("Please enter a valid email address"))
                .andExpect(jsonPath("$.fieldErrors.phoneNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").value("Salary must not be negative"));

        verifyNoInteractions(employeeService);
    }

    @Test
    void createWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(8));
    }

    @Test
    void createWithInvalidStatusOrMalformedJsonReturns400() throws Exception {
        String json = objectMapper.writeValueAsString(TestData.employeeRequest()).replace("ACTIVE", "RETIRED");

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("ACTIVE or INACTIVE")));

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithDuplicateEmailReturns409() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new DuplicateEmailException("ananya.sharma@example.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestData.employeeRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("An employee with email 'ananya.sharma@example.com' already exists"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void getByIdReturnsEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP-1001"))
                .andExpect(jsonPath("$.email").value("ananya.sharma@example.com"));
    }

    @Test
    void getMissingEmployeeReturns404() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found with id 99"));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/employees/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'id'"));
    }

    @Test
    void updateReturns200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestData.employeeRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateMissingEmployeeReturns404() throws Exception {
        when(employeeService.updateEmployee(eq(9L), any(EmployeeRequest.class)))
                .thenThrow(new EmployeeNotFoundException(9L));

        mockMvc.perform(put("/api/employees/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestData.employeeRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    void deleteMissingEmployeeReturns404() throws Exception {
        doThrow(new EmployeeNotFoundException(5L)).when(employeeService).deleteEmployee(5L);

        mockMvc.perform(delete("/api/employees/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listPassesSearchFiltersAndPagingToService() throws Exception {
        PageResponse<EmployeeResponse> page =
                new PageResponse<>(List.of(TestData.employeeResponse(1L)), 1, 5, 6, 2, false, true);
        when(employeeService.searchEmployees(any(EmployeeFilter.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/employees")
                        .param("page", "1")
                        .param("size", "5")
                        .param("search", "ananya")
                        .param("department", "Engineering")
                        .param("status", "ACTIVE")
                        .param("sortBy", "fullName")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].employeeId").value("EMP-1001"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(true));

        ArgumentCaptor<EmployeeFilter> filter = ArgumentCaptor.forClass(EmployeeFilter.class);
        verify(employeeService).searchEmployees(filter.capture(), eq(1), eq(5), eq("fullName"), eq("asc"));
        assertThat(filter.getValue())
                .isEqualTo(new EmployeeFilter("ananya", "Engineering", EmploymentStatus.ACTIVE));
    }

    @Test
    void listUsesDefaultsWhenNoParametersGiven() throws Exception {
        when(employeeService.searchEmployees(any(EmployeeFilter.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true, true));

        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());

        verify(employeeService).searchEmployees(EmployeeFilter.empty(), 0, 10, "id", "desc");
    }

    @Test
    void listWithUnknownStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/employees").param("status", "RETIRED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'RETIRED' for parameter 'status'"));
    }

    @Test
    void listWithInvalidSortReturns400() throws Exception {
        when(employeeService.searchEmployees(any(EmployeeFilter.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new InvalidRequestException("Cannot sort by 'password'"));

        mockMvc.perform(get("/api/employees").param("sortBy", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot sort by 'password'"));
    }

    @Test
    void exportReturnsCsvAttachment() throws Exception {
        when(employeeService.findAllEmployees(any(EmployeeFilter.class), anyString(), anyString()))
                .thenReturn(List.of(TestData.employeeResponse(1L)));

        mockMvc.perform(get("/api/employees/export").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"employees.csv\""))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("EMP-1001,Ananya Sharma")));

        verify(employeeService).findAllEmployees(new EmployeeFilter(null, "Engineering", null), "id", "desc");
    }

    @Test
    void departmentsReturnsDistinctNames() throws Exception {
        when(employeeService.getDepartments()).thenReturn(List.of("Engineering", "Finance"));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Engineering"))
                .andExpect(jsonPath("$[1]").value("Finance"));
    }
}

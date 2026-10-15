package com.kloudspot.ems.controller;

import com.kloudspot.ems.TestData;
import com.kloudspot.ems.dto.EmployeeFilter;
import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.dto.EmployeeSummary;
import com.kloudspot.ems.dto.PageResponse;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.exception.DuplicateEmailException;
import com.kloudspot.ems.exception.EmployeeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({EmployeeWebController.class, HomeController.class})
class EmployeeWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.kloudspot.ems.service.EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        when(employeeService.getDepartments()).thenReturn(List.of("Engineering", "Finance"));
        when(employeeService.getSummary()).thenReturn(new EmployeeSummary(1, 1, 0, 1));
    }

    @Test
    void homeRedirectsToEmployeeList() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));
    }

    @Test
    void listPageShowsEmployeesSummaryAndPagination() throws Exception {
        PageResponse<EmployeeResponse> page =
                new PageResponse<>(List.of(TestData.employeeResponse(1L)), 0, 1, 3, 3, true, false);
        when(employeeService.searchEmployees(any(EmployeeFilter.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/employees").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attribute("pageNumbers", List.of(0, 1, 2)))
                .andExpect(content().string(containsString("Ananya Sharma")))
                .andExpect(content().string(containsString("EMP-1001")))
                .andExpect(content().string(containsString("Total employees")))
                .andExpect(content().string(containsString("aria-label=\"Next page\"")));
    }

    @Test
    void listPagePassesFiltersToService() throws Exception {
        when(employeeService.searchEmployees(any(EmployeeFilter.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true, true));

        mockMvc.perform(get("/employees")
                        .param("search", "ananya")
                        .param("department", "Engineering")
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filtersActive", true))
                .andExpect(content().string(containsString("No employees match your filters")));

        verify(employeeService).searchEmployees(
                new EmployeeFilter("ananya", "Engineering", EmploymentStatus.INACTIVE), 0, 10, "id", "desc");
    }

    @Test
    void listPageShowsEmptyStateWhenThereAreNoEmployees() throws Exception {
        when(employeeService.searchEmployees(any(EmployeeFilter.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true, true));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No employees yet")));
    }

    @Test
    void detailsPageShowsAllFields() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/details"))
                .andExpect(content().string(containsString("ananya.sharma@example.com")))
                .andExpect(content().string(containsString("+919876543210")))
                .andExpect(content().string(containsString("15 July 2024")))
                .andExpect(content().string(containsString("₹850,000.00")));
    }

    @Test
    void missingEmployeeShowsNotFoundPage() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Employee not found with id 99")));
    }

    @Test
    void addPageShowsEmptyForm() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(content().string(containsString("Add Employee")));
    }

    @Test
    void createWithValidDataRedirectsToListWithSuccessMessage() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class))).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(post("/employees")
                        .param("fullName", "Ananya Sharma")
                        .param("email", "ananya.sharma@example.com")
                        .param("phoneNumber", "+919876543210")
                        .param("department", "Engineering")
                        .param("designation", "Software Engineer")
                        .param("dateOfJoining", "2024-07-15")
                        .param("salary", "850000")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("successMessage", "Ananya Sharma (EMP-1001) was added successfully."));
    }

    @Test
    void createWithInvalidDataShowsFormWithErrors() throws Exception {
        mockMvc.perform(post("/employees")
                        .param("fullName", "")
                        .param("email", "wrong")
                        .param("salary", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeHasFieldErrors("employee",
                        "fullName", "email", "phoneNumber", "department", "designation", "dateOfJoining",
                        "salary", "status"))
                .andExpect(content().string(containsString("Please fix the highlighted fields")))
                .andExpect(content().string(containsString("Salary must be a valid number")));

        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    void createWithDuplicateEmailShowsEmailError() throws Exception {
        when(employeeService.createEmployee(any(EmployeeRequest.class)))
                .thenThrow(new DuplicateEmailException("ananya.sharma@example.com"));

        mockMvc.perform(post("/employees")
                        .param("fullName", "Ananya Sharma")
                        .param("email", "ananya.sharma@example.com")
                        .param("phoneNumber", "+919876543210")
                        .param("department", "Engineering")
                        .param("designation", "Software Engineer")
                        .param("dateOfJoining", "2024-07-15")
                        .param("salary", "850000")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeHasFieldErrorCode("employee", "email", "duplicate"))
                .andExpect(content().string(containsString("already exists")));
    }

    @Test
    void editPageIsPrefilled() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(get("/employees/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(content().string(containsString("value=\"ananya.sharma@example.com\"")))
                .andExpect(content().string(containsString("value=\"2024-07-15\"")))
                .andExpect(content().string(containsString("action=\"/employees/1\"")));
    }

    @Test
    void updateRedirectsToDetailsPage() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(post("/employees/1")
                        .param("fullName", "Ananya Sharma")
                        .param("email", "ananya.sharma@example.com")
                        .param("phoneNumber", "+919876543210")
                        .param("department", "Engineering")
                        .param("designation", "Software Engineer")
                        .param("dateOfJoining", "2024-07-15")
                        .param("salary", "850000")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void deleteRedirectsToListWithSuccessMessage() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(TestData.employeeResponse(1L));

        mockMvc.perform(post("/employees/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attribute("successMessage", "Ananya Sharma (EMP-1001) was deleted."));

        verify(employeeService).deleteEmployee(1L);
    }
}

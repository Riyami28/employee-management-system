package com.kloudspot.ems.service;

import com.kloudspot.ems.TestData;
import com.kloudspot.ems.dto.EmployeeFilter;
import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.dto.EmployeeSummary;
import com.kloudspot.ems.dto.PageResponse;
import com.kloudspot.ems.entity.Employee;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.exception.DuplicateEmailException;
import com.kloudspot.ems.exception.EmployeeNotFoundException;
import com.kloudspot.ems.exception.InvalidRequestException;
import com.kloudspot.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Nested
    class CreateEmployee {

        @Test
        void createsEmployee() {
            EmployeeRequest request = TestData.employeeRequest();
            when(employeeRepository.existsByEmail("ananya.sharma@example.com")).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
                Employee employee = invocation.getArgument(0);
                employee.setId(5L);
                return employee;
            });

            EmployeeResponse response = employeeService.createEmployee(request);

            assertThat(response.getId()).isEqualTo(5L);
            assertThat(response.getEmployeeId()).isEqualTo("EMP-1005");
            assertThat(response.getFullName()).isEqualTo("Ananya Sharma");
            assertThat(response.getEmail()).isEqualTo("ananya.sharma@example.com");
            assertThat(response.getDepartment()).isEqualTo("Engineering");
            assertThat(response.getSalary()).isEqualByComparingTo("850000.00");
            assertThat(response.getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
        }

        @Test
        void normalizesInput() {
            EmployeeRequest request = TestData.employeeRequest();
            request.setFullName("  Ananya Sharma  ");
            request.setEmail("  Ananya.Sharma@Example.COM ");
            request.setDepartment(" Engineering ");
            when(employeeRepository.existsByEmail("ananya.sharma@example.com")).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
                Employee employee = invocation.getArgument(0);
                employee.setId(1L);
                return employee;
            });

            employeeService.createEmployee(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());
            Employee saved = captor.getValue();
            assertThat(saved.getFullName()).isEqualTo("Ananya Sharma");
            assertThat(saved.getEmail()).isEqualTo("ananya.sharma@example.com");
            assertThat(saved.getDepartment()).isEqualTo("Engineering");
        }

        @Test
        void rejectsDuplicateEmail() {
            EmployeeRequest request = TestData.employeeRequest();
            request.setEmail("ANANYA.SHARMA@example.com");
            when(employeeRepository.existsByEmail("ananya.sharma@example.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("ananya.sharma@example.com");
            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    class GetEmployee {

        @Test
        void returnsEmployee() {
            when(employeeRepository.findById(7L)).thenReturn(Optional.of(TestData.savedEmployee(7L)));

            EmployeeResponse response = employeeService.getEmployeeById(7L);

            assertThat(response.getId()).isEqualTo(7L);
            assertThat(response.getEmployeeId()).isEqualTo("EMP-1007");
            assertThat(response.getEmail()).isEqualTo("ananya.sharma@example.com");
        }

        @Test
        void throwsWhenMissing() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                    .isInstanceOf(EmployeeNotFoundException.class)
                    .hasMessage("Employee not found with id 99");
        }
    }

    @Nested
    class UpdateEmployee {

        @Test
        void updatesEmployee() {
            Employee existing = TestData.savedEmployee(3L);
            EmployeeRequest changes = TestData.employeeRequest();
            changes.setFullName("Ananya Sharma-Rao");
            changes.setDesignation("Senior Software Engineer");
            changes.setSalary(new BigDecimal("1200000.00"));
            changes.setStatus(EmploymentStatus.INACTIVE);
            when(employeeRepository.findById(3L)).thenReturn(Optional.of(existing));
            when(employeeRepository.existsByEmailAndIdNot("ananya.sharma@example.com", 3L)).thenReturn(false);
            when(employeeRepository.save(existing)).thenReturn(existing);

            EmployeeResponse response = employeeService.updateEmployee(3L, changes);

            assertThat(response.getEmployeeId()).isEqualTo("EMP-1003");
            assertThat(response.getFullName()).isEqualTo("Ananya Sharma-Rao");
            assertThat(response.getDesignation()).isEqualTo("Senior Software Engineer");
            assertThat(response.getSalary()).isEqualByComparingTo("1200000.00");
            assertThat(response.getStatus()).isEqualTo(EmploymentStatus.INACTIVE);
        }

        @Test
        void rejectsEmailOfAnotherEmployee() {
            EmployeeRequest changes = TestData.employeeRequest();
            changes.setEmail("taken@example.com");
            when(employeeRepository.findById(3L)).thenReturn(Optional.of(TestData.savedEmployee(3L)));
            when(employeeRepository.existsByEmailAndIdNot("taken@example.com", 3L)).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateEmployee(3L, changes))
                    .isInstanceOf(DuplicateEmailException.class);
            verify(employeeRepository, never()).save(any());
        }

        @Test
        void throwsWhenMissing() {
            when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(42L, TestData.employeeRequest()))
                    .isInstanceOf(EmployeeNotFoundException.class);
            verify(employeeRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteEmployee {

        @Test
        void deletesEmployee() {
            Employee existing = TestData.savedEmployee(4L);
            when(employeeRepository.findById(4L)).thenReturn(Optional.of(existing));

            employeeService.deleteEmployee(4L);

            verify(employeeRepository).delete(existing);
        }

        @Test
        void throwsWhenMissing() {
            when(employeeRepository.findById(4L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(4L))
                    .isInstanceOf(EmployeeNotFoundException.class);
            verify(employeeRepository, never()).delete(any(Employee.class));
        }
    }

    @Nested
    class SearchEmployees {

        private final EmployeeFilter filter = new EmployeeFilter("ananya", "Engineering", EmploymentStatus.ACTIVE);

        @Test
        void buildsPageRequest() {
            when(employeeRepository.findAll(anySpecification(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            employeeService.searchEmployees(filter, 2, 5, "fullName", "asc");

            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(employeeRepository).findAll(anySpecification(), pageable.capture());
            assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
            assertThat(pageable.getValue().getPageSize()).isEqualTo(5);
            assertThat(pageable.getValue().getSort())
                    .isEqualTo(Sort.by(Sort.Direction.ASC, "fullName").and(Sort.by(Sort.Direction.ASC, "id")));
        }

        @Test
        void usesDefaultSort() {
            when(employeeRepository.findAll(anySpecification(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            employeeService.searchEmployees(EmployeeFilter.empty(), 0, 10, null, null);

            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(employeeRepository).findAll(anySpecification(), pageable.capture());
            assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "id"));
        }

        @Test
        void sortsByEmployeeId() {
            when(employeeRepository.findAll(anySpecification(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            employeeService.searchEmployees(EmployeeFilter.empty(), 0, 10, "employeeId", "ASC");

            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(employeeRepository).findAll(anySpecification(), pageable.capture());
            assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
        }

        @Test
        void mapsPageResponse() {
            Pageable pageable = PageRequest.of(0, 2);
            List<Employee> employees = List.of(TestData.savedEmployee(1L), TestData.savedEmployee(2L));
            when(employeeRepository.findAll(anySpecification(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(employees, pageable, 5));

            PageResponse<EmployeeResponse> page = employeeService.searchEmployees(filter, 0, 2, "id", "asc");

            assertThat(page.getContent()).extracting(EmployeeResponse::getEmployeeId)
                    .containsExactly("EMP-1001", "EMP-1002");
            assertThat(page.getPage()).isZero();
            assertThat(page.getSize()).isEqualTo(2);
            assertThat(page.getTotalElements()).isEqualTo(5);
            assertThat(page.getTotalPages()).isEqualTo(3);
            assertThat(page.isFirst()).isTrue();
            assertThat(page.isLast()).isFalse();
        }

        @Test
        void rejectsUnknownSortField() {
            assertThatThrownBy(() -> employeeService.searchEmployees(filter, 0, 10, "password", "asc"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Cannot sort by 'password'");
            verifyNoInteractions(employeeRepository);
        }

        @Test
        void rejectsInvalidSortDirection() {
            assertThatThrownBy(() -> employeeService.searchEmployees(filter, 0, 10, "salary", "upwards"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("asc' or 'desc");
            verifyNoInteractions(employeeRepository);
        }

        @ParameterizedTest(name = "page={0}, size={1}")
        @CsvSource({"-1, 10", "0, 0", "0, 101"})
        void rejectsInvalidPaging(int page, int size) {
            assertThatThrownBy(() -> employeeService.searchEmployees(filter, page, size, "id", "asc"))
                    .isInstanceOf(InvalidRequestException.class);
            verifyNoInteractions(employeeRepository);
        }
    }

    @Nested
    class Lookups {

        @Test
        void findsAllForExport() {
            when(employeeRepository.findAll(anySpecification(), any(Sort.class)))
                    .thenReturn(List.of(TestData.savedEmployee(1L), TestData.savedEmployee(2L)));

            List<EmployeeResponse> employees = employeeService.findAllEmployees(EmployeeFilter.empty(), "id", "asc");

            assertThat(employees).hasSize(2);
        }

        @Test
        void returnsDepartments() {
            when(employeeRepository.findDistinctDepartments()).thenReturn(List.of("Engineering", "Finance"));

            assertThat(employeeService.getDepartments()).containsExactly("Engineering", "Finance");
        }

        @Test
        void returnsSummary() {
            when(employeeRepository.count()).thenReturn(10L);
            when(employeeRepository.countByStatus(EmploymentStatus.ACTIVE)).thenReturn(7L);
            when(employeeRepository.countByStatus(EmploymentStatus.INACTIVE)).thenReturn(3L);
            when(employeeRepository.countDistinctDepartments()).thenReturn(4L);

            EmployeeSummary summary = employeeService.getSummary();

            assertThat(summary.getTotalEmployees()).isEqualTo(10);
            assertThat(summary.getActiveEmployees()).isEqualTo(7);
            assertThat(summary.getInactiveEmployees()).isEqualTo(3);
            assertThat(summary.getTotalDepartments()).isEqualTo(4);
        }
    }

    private static Specification<Employee> anySpecification() {
        return ArgumentMatchers.any();
    }
}

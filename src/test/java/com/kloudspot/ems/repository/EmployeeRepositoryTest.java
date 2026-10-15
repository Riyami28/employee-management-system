package com.kloudspot.ems.repository;

import com.kloudspot.ems.TestData;
import com.kloudspot.ems.dto.EmployeeFilter;
import com.kloudspot.ems.entity.Employee;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.mapper.EmployeeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.kloudspot.ems.entity.EmploymentStatus.ACTIVE;
import static com.kloudspot.ems.entity.EmploymentStatus.INACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee ananya;

    @BeforeEach
    void setUp() {
        ananya = save("Ananya Sharma", "ananya.sharma@example.com", "Engineering", ACTIVE);
        save("Rahul Verma", "rahul.verma@example.com", "Engineering", INACTIVE);
        save("Priya Nair", "priya.nair@example.com", "Finance", ACTIVE);
        save("Arjun Mehta", "arjun@sharma-consulting.example.com", "Sales", ACTIVE);
        save("Kavya Iyer", "kavya.iyer@example.com", "Finance", INACTIVE);
    }

    @Test
    void searchMatchesNameIgnoringCase() {
        assertThat(names(new EmployeeFilter("SHARMA", null, null)))
                .containsExactlyInAnyOrder("Ananya Sharma", "Arjun Mehta"); // Arjun matches by email
    }

    @Test
    void searchMatchesEmail() {
        assertThat(names(new EmployeeFilter("priya.nair@", null, null))).containsExactly("Priya Nair");
    }

    @Test
    void searchMatchesEmployeeId() {
        assertThat(names(new EmployeeFilter(ananya.getEmployeeCode(), null, null))).containsExactly("Ananya Sharma");
    }

    @Test
    void filtersByDepartment() {
        assertThat(names(new EmployeeFilter(null, "Finance", null)))
                .containsExactlyInAnyOrder("Priya Nair", "Kavya Iyer");
    }

    @Test
    void filtersByStatus() {
        assertThat(names(new EmployeeFilter(null, null, INACTIVE)))
                .containsExactlyInAnyOrder("Rahul Verma", "Kavya Iyer");
    }

    @Test
    void combinesSearchDepartmentAndStatus() {
        assertThat(names(new EmployeeFilter("a", "Engineering", ACTIVE))).containsExactly("Ananya Sharma");
        assertThat(names(new EmployeeFilter("priya", "Engineering", null))).isEmpty();
    }

    @Test
    void blankFiltersReturnEveryone() {
        assertThat(names(new EmployeeFilter("  ", "", null))).hasSize(5);
    }

    @Test
    void paginatesFilteredResults() {
        Page<Employee> firstPage = employeeRepository.findAll(
                EmployeeSpecifications.matching(new EmployeeFilter(null, null, ACTIVE)),
                PageRequest.of(0, 2, Sort.by("fullName")));
        Page<Employee> secondPage = employeeRepository.findAll(
                EmployeeSpecifications.matching(new EmployeeFilter(null, null, ACTIVE)),
                PageRequest.of(1, 2, Sort.by("fullName")));

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).extracting(Employee::getFullName)
                .containsExactly("Ananya Sharma", "Arjun Mehta");
        assertThat(secondPage.getContent()).extracting(Employee::getFullName).containsExactly("Priya Nair");
    }

    @Test
    void employeeCodeIsDerivedFromGeneratedId() {
        assertThat(ananya.getEmployeeCode()).isEqualTo("EMP-" + (1000 + ananya.getId()));
    }

    @Test
    void emailExistenceChecks() {
        assertThat(employeeRepository.existsByEmail("ananya.sharma@example.com")).isTrue();
        assertThat(employeeRepository.existsByEmail("nobody@example.com")).isFalse();
        assertThat(employeeRepository.existsByEmailAndIdNot("ananya.sharma@example.com", ananya.getId())).isFalse();
        assertThat(employeeRepository.existsByEmailAndIdNot("priya.nair@example.com", ananya.getId())).isTrue();
    }

    @Test
    void databaseRejectsDuplicateEmail() {
        Employee duplicate = new Employee();
        EmployeeMapper.updateEntity(duplicate,
                TestData.employeeRequest("Someone Else", "ananya.sharma@example.com", "Sales", ACTIVE));

        assertThatThrownBy(() -> employeeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void departmentsAreDistinctAndSorted() {
        assertThat(employeeRepository.findDistinctDepartments()).containsExactly("Engineering", "Finance", "Sales");
        assertThat(employeeRepository.countDistinctDepartments()).isEqualTo(3);
    }

    @Test
    void countsByStatus() {
        assertThat(employeeRepository.countByStatus(ACTIVE)).isEqualTo(3);
        assertThat(employeeRepository.countByStatus(INACTIVE)).isEqualTo(2);
    }

    private Employee save(String name, String email, String department, EmploymentStatus status) {
        Employee employee = new Employee();
        EmployeeMapper.updateEntity(employee, TestData.employeeRequest(name, email, department, status));
        Employee saved = employeeRepository.save(employee);
        saved.assignEmployeeCode();
        return employeeRepository.saveAndFlush(saved);
    }

    private List<String> names(EmployeeFilter filter) {
        return employeeRepository.findAll(EmployeeSpecifications.matching(filter)).stream()
                .map(Employee::getFullName)
                .toList();
    }
}

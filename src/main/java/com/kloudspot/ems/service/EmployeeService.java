package com.kloudspot.ems.service;

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
import com.kloudspot.ems.mapper.EmployeeMapper;
import com.kloudspot.ems.repository.EmployeeRepository;
import com.kloudspot.ems.repository.EmployeeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    public static final int MAX_PAGE_SIZE = 100;

    public static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "fullName", "email", "department", "designation", "dateOfJoining", "salary", "status");

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        String email = EmployeeMapper.normalizeEmail(request.getEmail());
        if (employeeRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        Employee employee = new Employee();
        EmployeeMapper.updateEntity(employee, request);

        Employee saved = employeeRepository.save(employee);
        saved.assignEmployeeCode();
        return EmployeeMapper.toResponse(saved);
    }

    public EmployeeResponse getEmployeeById(Long id) {
        return EmployeeMapper.toResponse(findEmployeeOrThrow(id));
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployeeOrThrow(id);

        String email = EmployeeMapper.normalizeEmail(request.getEmail());
        if (employeeRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }

        EmployeeMapper.updateEntity(employee, request);
        return EmployeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeOrThrow(id);
        employeeRepository.delete(employee);
    }

    public PageResponse<EmployeeResponse> searchEmployees(EmployeeFilter filter, int page, int size,
                                                          String sortBy, String sortDirection) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDirection));

        Page<EmployeeResponse> result = employeeRepository
                .findAll(EmployeeSpecifications.matching(filter), pageable)
                .map(EmployeeMapper::toResponse);
        return PageResponse.from(result);
    }

    public List<EmployeeResponse> findAllEmployees(EmployeeFilter filter, String sortBy, String sortDirection) {
        return employeeRepository
                .findAll(EmployeeSpecifications.matching(filter), buildSort(sortBy, sortDirection))
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    public List<String> getDepartments() {
        return employeeRepository.findDistinctDepartments();
    }

    public EmployeeSummary getSummary() {
        return new EmployeeSummary(
                employeeRepository.count(),
                employeeRepository.countByStatus(EmploymentStatus.ACTIVE),
                employeeRepository.countByStatus(EmploymentStatus.INACTIVE),
                employeeRepository.countDistinctDepartments());
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("Page number must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        String property = StringUtils.hasText(sortBy) ? sortBy.trim() : "id";
        if ("employeeId".equals(property)) {
            property = "id";
        }
        if (!SORTABLE_FIELDS.contains(property)) {
            throw new InvalidRequestException("Cannot sort by '" + sortBy + "'. Allowed values: employeeId, "
                    + String.join(", ", SORTABLE_FIELDS.stream().sorted().toList()));
        }

        Sort.Direction direction = parseDirection(sortDirection);
        Sort sort = Sort.by(direction, property);
        // Sort by id too, so rows with equal values keep the same order across pages.
        return "id".equals(property) ? sort : sort.and(Sort.by(direction, "id"));
    }

    private Sort.Direction parseDirection(String sortDirection) {
        if (!StringUtils.hasText(sortDirection)) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.fromOptionalString(sortDirection.trim())
                .orElseThrow(() -> new InvalidRequestException(
                        "Sort direction must be 'asc' or 'desc', but was '" + sortDirection + "'"));
    }
}

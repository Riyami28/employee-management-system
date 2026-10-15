package com.kloudspot.ems.controller;

import com.kloudspot.ems.dto.EmployeeFilter;
import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.dto.ErrorResponse;
import com.kloudspot.ems.dto.PageResponse;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.service.EmployeeCsvExporter;
import com.kloudspot.ems.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees")
public class EmployeeRestController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final EmployeeService employeeService;
    private final EmployeeCsvExporter csvExporter;

    public EmployeeRestController(EmployeeService employeeService, EmployeeCsvExporter csvExporter) {
        this.employeeService = employeeService;
        this.csvExporter = csvExporter;
    }

    @PostMapping
    @Operation(summary = "Create an employee")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.createEmployee(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List employees with search, filters, paging and sorting")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Invalid parameter",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public PageResponse<EmployeeResponse> getEmployees(
            @Parameter(description = "Page number, starts at 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, 1 to 100") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Name, email or employee ID") @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmploymentStatus status,
            @Parameter(description = "employeeId, fullName, email, department, designation, dateOfJoining, salary or status")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "asc or desc") @RequestParam(defaultValue = "desc") String sortDirection) {
        EmployeeFilter filter = new EmployeeFilter(search, department, status);
        return employeeService.searchEmployees(filter, page, size, sortBy, sortDirection);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public EmployeeResponse getEmployee(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public EmployeeResponse updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "Not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export employees as CSV (same filters as the list)")
    @ApiResponse(responseCode = "200", description = "CSV file",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string")))
    public ResponseEntity<String> exportEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        EmployeeFilter filter = new EmployeeFilter(search, department, status);
        String csv = csvExporter.toCsv(employeeService.findAllEmployees(filter, sortBy, sortDirection));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("employees.csv").build().toString())
                .contentType(TEXT_CSV)
                .body(csv);
    }
}

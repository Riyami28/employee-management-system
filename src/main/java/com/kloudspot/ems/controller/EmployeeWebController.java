package com.kloudspot.ems.controller;

import com.kloudspot.ems.dto.EmployeeFilter;
import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.dto.EmployeeResponse;
import com.kloudspot.ems.dto.PageResponse;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.exception.DuplicateEmailException;
import com.kloudspot.ems.mapper.EmployeeMapper;
import com.kloudspot.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/employees")
public class EmployeeWebController {

    static final List<Integer> PAGE_SIZE_OPTIONS = List.of(5, 10, 20, 50);
    private static final int PAGE_LINK_RADIUS = 2;

    private final EmployeeService employeeService;

    public EmployeeWebController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String listEmployees(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String department,
                                @RequestParam(required = false) EmploymentStatus status,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "id") String sortBy,
                                @RequestParam(defaultValue = "desc") String sortDirection,
                                Model model) {
        EmployeeFilter filter = new EmployeeFilter(search, department, status);
        PageResponse<EmployeeResponse> employees =
                employeeService.searchEmployees(filter, page, size, sortBy, sortDirection);

        model.addAttribute("employees", employees);
        model.addAttribute("pageNumbers", pageNumbers(employees));
        model.addAttribute("summary", employeeService.getSummary());
        model.addAttribute("departments", employeeService.getDepartments());
        model.addAttribute("statuses", EmploymentStatus.values());
        model.addAttribute("pageSizeOptions", PAGE_SIZE_OPTIONS);
        model.addAttribute("search", search);
        model.addAttribute("department", department);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("filtersActive",
                StringUtils.hasText(search) || StringUtils.hasText(department) || status != null);
        return "employees/list";
    }

    @GetMapping("/{id}")
    public String viewEmployee(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.getEmployeeById(id));
        return "employees/details";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        EmployeeRequest employee = new EmployeeRequest();
        employee.setStatus(EmploymentStatus.ACTIVE);
        model.addAttribute("employee", employee);
        return showForm(model, null);
    }

    @PostMapping
    public String createEmployee(@Valid @ModelAttribute("employee") EmployeeRequest employee,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showForm(model, null);
        }
        try {
            EmployeeResponse created = employeeService.createEmployee(employee);
            redirectAttributes.addFlashAttribute("successMessage",
                    created.getFullName() + " (" + created.getEmployeeId() + ") was added successfully.");
            return "redirect:/employees";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return showForm(model, null);
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        EmployeeResponse existing = employeeService.getEmployeeById(id);
        model.addAttribute("employee", EmployeeMapper.toRequest(existing));
        return showForm(model, existing);
    }

    @PostMapping("/{id}")
    public String updateEmployee(@PathVariable Long id,
                                 @Valid @ModelAttribute("employee") EmployeeRequest employee,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showForm(model, employeeService.getEmployeeById(id));
        }
        try {
            EmployeeResponse updated = employeeService.updateEmployee(id, employee);
            redirectAttributes.addFlashAttribute("successMessage",
                    updated.getFullName() + " (" + updated.getEmployeeId() + ") was updated successfully.");
            return "redirect:/employees/" + id;
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return showForm(model, employeeService.getEmployeeById(id));
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("successMessage",
                employee.getFullName() + " (" + employee.getEmployeeId() + ") was deleted.");
        return "redirect:/employees";
    }

    // existing is null on the add page.
    private String showForm(Model model, EmployeeResponse existing) {
        model.addAttribute("existingEmployee", existing);
        model.addAttribute("departments", employeeService.getDepartments());
        model.addAttribute("statuses", EmploymentStatus.values());
        return "employees/form";
    }

    private List<Integer> pageNumbers(PageResponse<?> page) {
        if (page.getTotalPages() == 0) {
            return List.of();
        }
        int first = Math.max(0, page.getPage() - PAGE_LINK_RADIUS);
        int last = Math.min(page.getTotalPages() - 1, page.getPage() + PAGE_LINK_RADIUS);
        return IntStream.rangeClosed(first, last).boxed().toList();
    }
}

package com.kloudspot.ems.repository;

import com.kloudspot.ems.dto.EmployeeFilter;
import com.kloudspot.ems.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    // Adds a condition only for filters that have a value.
    public static Specification<Employee> matching(EmployeeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.search())) {
                String pattern = "%" + filter.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("employeeCode")), pattern)));
            }
            if (StringUtils.hasText(filter.department())) {
                predicates.add(cb.equal(root.get("department"), filter.department().trim()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

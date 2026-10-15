package com.kloudspot.ems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudspot.ems.dto.EmployeeRequest;
import com.kloudspot.ems.entity.EmploymentStatus;
import com.kloudspot.ems.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class EmployeeApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void cleanDatabase() {
        employeeRepository.deleteAll();
    }

    @Test
    void fullEmployeeLifecycle() throws Exception {
        String createdJson = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TestData.employeeRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(matchesPattern("EMP-\\d{4,}")))
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(createdJson);
        long id = created.get("id").asLong();
        assertThat(created.get("employeeId").asText()).isEqualTo("EMP-" + (1000 + id));

        EmployeeRequest duplicate = TestData.employeeRequest();
        duplicate.setEmail("ANANYA.SHARMA@EXAMPLE.COM");
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json(duplicate)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ananya Sharma"));

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json(
                        TestData.employeeRequest("Rahul Verma", "rahul@example.com", "Finance", EmploymentStatus.INACTIVE))))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/employees")
                        .param("search", "ananya")
                        .param("department", "Engineering")
                        .param("status", "ACTIVE")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("ananya.sharma@example.com"));
        mockMvc.perform(get("/api/employees").param("size", "1").param("page", "1").param("sortBy", "fullName")
                        .param("sortDirection", "asc"))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].fullName").value("Rahul Verma"));
        mockMvc.perform(get("/api/departments"))
                .andExpect(jsonPath("$.length()").value(2));

        EmployeeRequest update = TestData.employeeRequest();
        update.setDesignation("Senior Software Engineer");
        update.setStatus(EmploymentStatus.INACTIVE);
        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("Senior Software Engineer"))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.employeeId").value("EMP-" + (1000 + id)));

        update.setEmail("rahul@example.com");
        mockMvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(update)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/employees/export").param("department", "Finance"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Rahul Verma")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Ananya Sharma"))));

        mockMvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/employees/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void htmlPagesRender() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json(TestData.employeeRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ananya Sharma")));
        mockMvc.perform(get("/employees/new")).andExpect(status().isOk());
    }

    @Test
    void openApiDocumentationIsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Employee Management System API"))
                .andExpect(jsonPath("$.paths['/api/employees']").exists())
                .andExpect(jsonPath("$.paths['/api/employees/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/departments']").exists());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}

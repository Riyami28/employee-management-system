# Submission Checklist

## Requirements

- [x] Employee fields with validation (name, email, phone, department, designation, joining date, salary, status)
- [x] Unique employee ID (`EMP-1001`) generated automatically
- [x] Unique email, with a clear error for duplicates (409)
- [x] Add, view, edit and delete (with confirmation)
- [x] Search by name or email
- [x] Filter by department and status
- [x] Pagination with page size
- [x] Validation on the frontend and backend
- [x] Loading, empty and error states; success messages
- [x] Layered code: entity, dto, repository, service, controller, exception, config, mapper
- [x] REST API: POST, GET, GET by id, PUT, DELETE, departments
- [x] Correct status codes: 201, 200, 204, 400, 404, 409
- [x] Central error handling with `@RestControllerAdvice`
- [x] MySQL with automatic schema setup
- [x] Settings from environment variables; no secrets committed
- [x] Docker Compose for MySQL, with optional sample data
- [x] Unit tests with JUnit 5 and Mockito (91 tests passing)
- [x] Swagger UI
- [x] Postman collection
- [x] README with setup steps and screenshots
- [x] Optional: CSV export
- [ ] Optional: basic authentication (not included)

## Before submitting

- [x] Code pushed to GitHub
- [x] Author name in the README
- [ ] Clone the repo into a new folder and follow the README once
- [ ] Send the repository link to Kloudspot

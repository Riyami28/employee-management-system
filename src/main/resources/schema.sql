-- Runs on every startup. IF NOT EXISTS keeps existing data.

CREATE TABLE IF NOT EXISTS employees (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    employee_code   VARCHAR(20)   NULL,
    full_name       VARCHAR(100)  NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    phone_number    VARCHAR(20)   NOT NULL,
    department      VARCHAR(100)  NOT NULL,
    designation     VARCHAR(100)  NOT NULL,
    date_of_joining DATE          NOT NULL,
    salary          DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_employees_employee_code UNIQUE (employee_code),
    CONSTRAINT uk_employees_email UNIQUE (email),
    CONSTRAINT chk_employees_salary CHECK (salary >= 0),
    CONSTRAINT chk_employees_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    INDEX idx_employees_department (department),
    INDEX idx_employees_status (status),
    INDEX idx_employees_full_name (full_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

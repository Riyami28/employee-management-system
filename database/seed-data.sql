-- 24 sample employees (fictional). Safe to run again: existing emails are skipped.
-- Manual use: mysql -u ems_user -p employee_db < database/seed-data.sql

INSERT IGNORE INTO employees
    (full_name, email, phone_number, department, designation, date_of_joining, salary, status, created_at, updated_at)
VALUES
    ('Aarav Mehta',      'aarav.mehta@example.com',      '+919810000001', 'Engineering',      'Engineering Manager',        '2019-04-01', 3200000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Diya Iyer',        'diya.iyer@example.com',        '+919810000002', 'Engineering',      'Senior Software Engineer',   '2020-07-15', 2400000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Kabir Nair',       'kabir.nair@example.com',       '+919810000003', 'Engineering',      'Software Engineer',          '2022-01-10', 1400000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Meera Pillai',     'meera.pillai@example.com',     '+919810000004', 'Engineering',      'Software Engineer',          '2023-06-05', 1250000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Rohan Desai',      'rohan.desai@example.com',      '+919810000005', 'Engineering',      'DevOps Engineer',            '2021-09-20', 1800000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Sneha Kulkarni',   'sneha.kulkarni@example.com',   '+919810000006', 'Engineering',      'QA Engineer',                '2022-11-01', 1100000.00, 'INACTIVE', NOW(6), NOW(6)),
    ('Vihaan Reddy',     'vihaan.reddy@example.com',     '+919810000007', 'Engineering',      'Associate Software Engineer', '2025-07-14',  900000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Ishita Banerjee',  'ishita.banerjee@example.com',  '+919810000008', 'Product',          'Product Manager',            '2020-02-03', 2600000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Arjun Malhotra',   'arjun.malhotra@example.com',   '+919810000009', 'Product',          'Associate Product Manager',  '2023-08-21', 1500000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Tara Joshi',       'tara.joshi@example.com',       '+919810000010', 'Product',          'Business Analyst',           '2021-03-15', 1300000.00, 'INACTIVE', NOW(6), NOW(6)),
    ('Neha Kapoor',      'neha.kapoor@example.com',      '+919810000011', 'Design',           'Lead Product Designer',      '2019-10-07', 2200000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Aditya Rao',       'aditya.rao@example.com',       '+919810000012', 'Design',           'UI/UX Designer',             '2024-01-08', 1200000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Priya Menon',      'priya.menon@example.com',      '+919810000013', 'Human Resources',  'HR Manager',                 '2018-05-14', 2000000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Siddharth Bose',   'siddharth.bose@example.com',   '+919810000014', 'Human Resources',  'Talent Acquisition Specialist', '2022-04-18', 950000.00, 'ACTIVE', NOW(6), NOW(6)),
    ('Ananya Ghosh',     'ananya.ghosh@example.com',     '+919810000015', 'Human Resources',  'HR Executive',               '2024-09-02',  700000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Karan Verma',      'karan.verma@example.com',      '+919810000016', 'Finance',          'Finance Manager',            '2017-12-11', 2500000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Pooja Agarwal',    'pooja.agarwal@example.com',    '+919810000017', 'Finance',          'Accountant',                 '2021-06-28',  850000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Rahul Saxena',     'rahul.saxena@example.com',     '+919810000018', 'Finance',          'Financial Analyst',          '2020-11-16', 1150000.00, 'INACTIVE', NOW(6), NOW(6)),
    ('Nikhil Chawla',    'nikhil.chawla@example.com',    '+919810000019', 'Sales',            'Sales Director',             '2018-08-06', 3000000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Riya Sinha',       'riya.sinha@example.com',       '+919810000020', 'Sales',            'Account Executive',          '2023-02-13', 1000000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Varun Thakur',     'varun.thakur@example.com',     '+919810000021', 'Sales',            'Sales Development Representative', '2024-05-20', 650000.00, 'INACTIVE', NOW(6), NOW(6)),
    ('Kavya Hegde',      'kavya.hegde@example.com',      '+919810000022', 'Customer Success', 'Customer Success Manager',   '2021-01-25', 1350000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Manish Yadav',     'manish.yadav@example.com',     '+919810000023', 'Customer Success', 'Support Engineer',           '2022-07-04',  900000.00, 'ACTIVE',   NOW(6), NOW(6)),
    ('Lakshmi Krishnan', 'lakshmi.krishnan@example.com', '+919810000024', 'Customer Success', 'Technical Account Manager',  '2020-09-14', 1600000.00, 'INACTIVE', NOW(6), NOW(6));

-- Same ID rule as the app: EMP- + (1000 + id)
UPDATE employees
SET employee_code = CONCAT('EMP-', 1000 + id)
WHERE employee_code IS NULL;

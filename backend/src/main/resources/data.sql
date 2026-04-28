-- SEED DATA: ICT School - Classroom Attendance
-- Prerequisites:
--   - Docker must be running
--   - attendance-db container must be running
--   - schema must already be applied (schema.sql or app startup)
--
-- How to run:
--   docker exec -i attendance-db mysql -u db_user -pdb_password classroom_attendance < backend/src/main/resources/data.sql
--
-- To reset and re-run:
--   docker exec -it attendance-db mysql -u db_user -pdb_password classroom_attendance -e "
--     SET FOREIGN_KEY_CHECKS = 0;
--     TRUNCATE TABLE attendance;
--     TRUNCATE TABLE sessions;
--     TRUNCATE TABLE enrollments;
--     TRUNCATE TABLE classes;
--     TRUNCATE TABLE users;
--     SET FOREIGN_KEY_CHECKS = 1;"
--
-- Test Accounts (all passwords are: password123!)
--   ADMIN   : admin@ict.edu
--   TEACHER : john.smith@ict.edu
--   TEACHER : sarah.jones@ict.edu
--   TEACHER : michael.tan@ict.edu
--   STUDENT : alice.wong@student.ict.edu  (STU-2024-001)
--   STUDENT : bob.lim@student.ict.edu     (STU-2024-002)
-- ================================================================


-- ----------------------------------------------------------------
-- 1. USERS
-- ID assignment (auto-increment order):
--   1 = admin, 2 = john.smith, 3 = sarah.jones,
--   4 = michael.tan, 5 = alice.wong, 6 = bob.lim


-- 1. USERS
-- Plain password is "password123!" for all users, hashed using BCrypt with a cost factor of 10
INSERT INTO users (email, password_hash, first_name, last_name, user_type, student_code) VALUES

-- Admin (id=1)
('admin@ict.edu', '$2b$10$VYXVhyw/asIJhwbQ/GFMreXfMdVOF4zY7rQ3ysrsi.NBY59RP.Im6', 'System', 'Admin', 'ADMIN', NULL),

-- Teachers (id=2,3,4)
('john.smith@ict.edu',  '$2b$10$412sO.H/dVYCzOGaJz3vve1OoObVIUFOtXZcR.QaoyZ/lgNnMADyq', 'John',   'Smith', 'TEACHER', NULL),
('sarah.jones@ict.edu', '$2b$10$dQs4bsaszPSi0HNE1GFIAOu0x4WeHz2ZTa2bLrbqABG1dt85x8Hpe', 'Sarah',  'Jones', 'TEACHER', NULL),
('michael.tan@ict.edu', '$2b$10$.gluXKKb8PccYlIHE3hryuG9jYix08BIb.6xNe/y6OB6bFPn4147i', 'Michael','Tan',   'TEACHER', NULL),

-- Students (id=5,6)
('alice.wong@student.ict.edu', '$2b$10$sPouVZz6gY12IMH74EMc0.8drgW8LRJgG/uakfHolc0ESiBXYwsjW', 'Alice', 'Wong', 'STUDENT', 'STU-2024-001'),
('bob.lim@student.ict.edu',    '$2b$10$qgRykQ8x1dk41pEoHYZPp.hy5oQj01PhE4h9hhhYxq3PGTW5H3zGO', 'Bob',   'Lim',  'STUDENT', 'STU-2024-002');


-- 2. CLASSES
-- John(id=2) teaches ICT101, ICT201 | Sarah(id=3) teaches ICT301, ICT401
INSERT INTO classes (class_code, name, teacher_id, semester, academic_year, max_capacity) VALUES
                                                                                              ('ICT101-A', 'Introduction to Programming',    2, 'SEM1', '2024/2025', 30),
                                                                                              ('ICT201-A', 'Data Structures and Algorithms', 2, 'SEM1', '2024/2025', 30),
                                                                                              ('ICT301-A', 'Web Application Development',    3, 'SEM1', '2024/2025', 25),
                                                                                              ('ICT401-A', 'Database Management Systems',    3, 'SEM1', '2024/2025', 25);


-- 3. ENROLLMENTS
-- Alice=5, Bob=6 | ICT101=1, ICT201=2, ICT301=3, ICT401=4
INSERT INTO enrollments (student_id, class_id, status) VALUES
-- ICT101
(5, 1, 'ACTIVE'),
(6, 1, 'ACTIVE'),
-- ICT201
(5, 2, 'ACTIVE'),
(6, 2, 'ACTIVE'),
-- ICT301
(5, 3, 'ACTIVE'),
(6, 3, 'DROPPED'),
-- ICT401
(5, 4, 'ACTIVE'),
(6, 4, 'ACTIVE');


-- 4. SESSIONS
INSERT INTO sessions (class_id, session_date, start_time, end_time, topic, qr_token, status) VALUES
-- ICT101 (id=1,2,3)
(1, '2025-01-13', '08:00:00', '10:00:00', 'Introduction to Python Basics',       'QR-ICT101-001', 'COMPLETED'),
(1, '2025-01-20', '08:00:00', '10:00:00', 'Variables, Data Types and Operators', 'QR-ICT101-002', 'COMPLETED'),
(1, '2025-01-27', '08:00:00', '10:00:00', 'Control Flow: if/else and loops',     'QR-ICT101-003', 'COMPLETED'),
-- ICT201 (id=4,5,6)
(2, '2025-01-14', '10:00:00', '12:00:00', 'Arrays and Linked Lists',             'QR-ICT201-001', 'COMPLETED'),
(2, '2025-01-21', '10:00:00', '12:00:00', 'Stacks and Queues',                   'QR-ICT201-002', 'COMPLETED'),
(2, '2025-01-28', '10:00:00', '12:00:00', 'Binary Trees and Traversal',          'QR-ICT201-003', 'COMPLETED'),
-- ICT301 (id=7,8,9)
(3, '2025-01-15', '14:00:00', '16:00:00', 'HTML5 and CSS3 Fundamentals',         'QR-ICT301-001', 'COMPLETED'),
(3, '2025-01-22', '14:00:00', '16:00:00', 'JavaScript and DOM Manipulation',     'QR-ICT301-002', 'COMPLETED'),
(3, '2025-01-29', '14:00:00', '16:00:00', 'REST APIs with Spring Boot',          'QR-ICT301-003', 'SCHEDULED'),
-- ICT401 (id=10,11)
(4, '2025-01-16', '08:00:00', '10:00:00', 'Relational Model and SQL Basics',     'QR-ICT401-001', 'COMPLETED'),
(4, '2025-01-23', '08:00:00', '10:00:00', 'Joins, Indexes and Normalization',    'QR-ICT401-002', 'COMPLETED');


-- 5. ATTENDANCE
-- Only Alice(5) and Bob(6) | Bob is DROPPED from ICT301 so no attendance for sessions 7,8,9
-- Bob has no ICT301 attendance (DROPPED enrollment)
INSERT INTO attendance (student_id, session_id, status, marked_by) VALUES

-- ICT101 Session 1
(5, 1, 'PRESENT', 'QR'),
(6, 1, 'PRESENT', 'QR'),

-- ICT101 Session 2
(5, 2, 'PRESENT', 'QR'),
(6, 2, 'ABSENT',  'TEACHER'),

-- ICT101 Session 3
(5, 3, 'PRESENT', 'QR'),
(6, 3, 'EXCUSED', 'TEACHER'),

-- ICT201 Session 4
(5, 4, 'PRESENT', 'QR'),
(6, 4, 'PRESENT', 'QR'),

-- ICT201 Session 5
(5, 5, 'ABSENT',  'TEACHER'),
(6, 5, 'PRESENT', 'QR'),

-- ICT201 Session 6
(5, 6, 'PRESENT', 'QR'),
(6, 6, 'PRESENT', 'QR'),

-- ICT301 Sessions 7,8 — only Alice (Bob is DROPPED, session 9 is SCHEDULED so no attendance)
(5, 7, 'PRESENT', 'QR'),
(5, 8, 'EXCUSED', 'TEACHER'),

-- ICT401 Session 10
(5, 10, 'PRESENT', 'QR'),
(6, 10, 'PRESENT', 'QR'),

-- ICT401 Session 11
(5, 11, 'PRESENT', 'QR'),
(6, 11, 'ABSENT',  'TEACHER');
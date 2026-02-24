DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS users;

-- 1. USERS
CREATE TABLE users
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    email         VARCHAR(100)       NOT NULL UNIQUE,
    password_hash VARCHAR(255)       NOT NULL,
    first_name    VARCHAR(100)       NOT NULL,
    last_name     VARCHAR(100)       NOT NULL,
    user_type     VARCHAR(20)        NOT NULL,
    student_code  VARCHAR(20) UNIQUE NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_type
        CHECK (user_type IN ('ADMIN', 'TEACHER', 'STUDENT')),

    CONSTRAINT chk_student_code
        CHECK (
            (user_type = 'STUDENT' AND student_code IS NOT NULL) OR
            (user_type <> 'STUDENT' AND student_code IS NULL)
            )
);


-- 2. CLASSES
CREATE TABLE classes
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_code    VARCHAR(50)  NOT NULL UNIQUE,
    name          VARCHAR(200) NOT NULL,
    teacher_id    BIGINT       NOT NULL,
    semester      VARCHAR(20),
    academic_year VARCHAR(20),
    max_capacity  INT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (teacher_id) REFERENCES users (id)
        ON DELETE RESTRICT
);


-- 3. ENROLLMENTS (students join classes)
CREATE TABLE enrollments
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    class_id   BIGINT NOT NULL,
    status     VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE CASCADE,

    UNIQUE (student_id, class_id),

    CONSTRAINT chk_enrollment_status
        CHECK (status IN ('ACTIVE', 'DROPPED', 'COMPLETED'))
);


-- 4. SESSIONS (each class session)
CREATE TABLE sessions
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id     BIGINT NOT NULL,
    session_date DATE   NOT NULL,
    start_time   TIME   NOT NULL,
    end_time     TIME   NOT NULL,
    topic        VARCHAR(200),
    qr_token     VARCHAR(255) UNIQUE,
    status       VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE CASCADE,

    CONSTRAINT chk_session_time CHECK (end_time > start_time),

    CONSTRAINT chk_session_status
        CHECK (status IN ('SCHEDULED', 'ACTIVE', 'CANCELLED', 'COMPLETED')),

    UNIQUE (class_id, session_date, start_time)
);


-- 5. ATTENDANCE
CREATE TABLE attendance
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    status     VARCHAR(20) DEFAULT 'ABSENT',
    marked_by  VARCHAR(20) DEFAULT 'QR',
    remarks    VARCHAR(255) NULL,
    marked_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,

    UNIQUE (student_id, session_id),

    CONSTRAINT chk_attendance_status
        CHECK (status IN ('PRESENT', 'ABSENT', 'EXCUSED')),

    CONSTRAINT chk_marked_by
        CHECK (marked_by IN ('QR', 'TEACHER'))
);

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE attendance;
TRUNCATE TABLE sessions;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE classes;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (
    email,
    password_hash,
    first_name,
    last_name,
    user_type,
    student_code
) VALUES
-- Admin
('admin@school.com', 'hash_admin', 'System', 'Admin', 'ADMIN', NULL),

-- Teachers
('smith@school.com', 'hash_teacher', 'John', 'Smith', 'TEACHER', NULL),
('johnson@school.com', 'hash_teacher', 'Emily', 'Johnson', 'TEACHER', NULL),

-- Students
('alice@student.com', 'hash_student', 'Alice', 'Brown', 'STUDENT', 'S2024001'),
('bob@student.com', 'hash_student', 'Bob', 'Clark', 'STUDENT', 'S2024002'),
('charlie@student.com', 'hash_student', 'Charlie', 'Lee', 'STUDENT', 'S2023009');


INSERT INTO classes (
    class_code,
    name,
    teacher_id,
    semester,
    academic_year,
    max_capacity
) VALUES
      ('CS101-F24', 'Database Systems', 2, 'Fall', '2024', 40),
      ('CS205-F24', 'Software Engineering', 3, 'Fall', '2024', 35);
INSERT INTO enrollments (
    student_id,
    class_id,
    status
) VALUES

      (4, 1, 'ACTIVE'),
      (5, 1, 'ACTIVE'),
      (6, 1, 'ACTIVE'),


      (4, 2, 'ACTIVE'),
      (6, 2, 'ACTIVE');


INSERT INTO sessions (
    class_id,
    session_date,
    start_time,
    end_time,
    topic,
    qr_token,
    status
) VALUES
      (1, '2026-02-07', '09:00:00', '10:30:00', 'Introduction to Databases', 'QR_DB_001', 'COMPLETED'),
      (1, '2026-02-09', '09:00:00', '10:30:00', 'ER Modeling', 'QR_DB_002', 'COMPLETED'),
      (2, '2026-02-07', '11:00:00', '12:30:00', 'Agile & Scrum Basics', 'QR_SE_001', 'COMPLETED');


INSERT INTO attendance (
    student_id,
    session_id,
    status,
    marked_by
) VALUES

      (4, 1, 'PRESENT', 'QR'),
      (5, 1, 'PRESENT', 'QR'),
      (6, 1, 'ABSENT', 'TEACHER'),


      (4, 2, 'PRESENT', 'QR'),
      (5, 2, 'PRESENT', 'QR'),
      (6, 2, 'EXCUSED', 'TEACHER'),


      (4, 3, 'PRESENT', 'QR'),
      (6, 3, 'ABSENT', 'QR');


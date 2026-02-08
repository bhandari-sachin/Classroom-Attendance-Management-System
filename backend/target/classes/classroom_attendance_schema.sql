DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS class;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS student;

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
        CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED')),

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
    marked_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,

    UNIQUE (student_id, session_id),

    CONSTRAINT chk_attendance_status
        CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),

    CONSTRAINT chk_marked_by
        CHECK (marked_by IN ('QR', 'TEACHER', 'SYSTEM'))
);

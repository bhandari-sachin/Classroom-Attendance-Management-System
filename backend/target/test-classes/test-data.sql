-- Clear tables
DELETE FROM attendance;
DELETE FROM session;
DELETE FROM enrollment;
DELETE FROM class;
DELETE FROM student;
DELETE FROM user;

-- USERS
INSERT INTO user (id, email, password_hash, name, role)
VALUES (1, 'teacher@test.com', 'hash', 'Test Teacher', 'teacher');

-- STUDENTS
INSERT INTO student (id, student_code, name)
VALUES (1, 'S001', 'Alice Smith');

-- CLASS
INSERT INTO class (id, class_code, name, teacher_id)
VALUES (1, 'CS101', 'Intro CS', 1);

-- ENROLLMENT
INSERT INTO enrollment (student_id, class_id)
VALUES (1, 1);

-- SESSION
INSERT INTO session (id, class_id, session_date, start_time, end_time, qr_token)
VALUES (1, 1, CURDATE(), '09:00:00', '10:00:00', 'ABC123');

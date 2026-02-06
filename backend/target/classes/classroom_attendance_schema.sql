DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS class;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS student;

CREATE TABLE user (
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  name VARCHAR(100) NOT NULL,
  role ENUM('admin', 'teacher') NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE student (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_code VARCHAR(20) UNIQUE NOT NULL, -- "S2024001"
  name VARCHAR(100) NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE class (
  id INT PRIMARY KEY AUTO_INCREMENT,
  class_code VARCHAR(20) UNIQUE NOT NULL, -- "CS101-F24"
  name VARCHAR(100) NOT NULL,
  teacher_id INT NOT NULL, -- References user table
  room VARCHAR(20),
  schedule VARCHAR(100),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (teacher_id) REFERENCES user(id)
);

CREATE TABLE enrollment (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  class_id INT NOT NULL,
  enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES student(id),
  FOREIGN KEY (class_id) REFERENCES class(id),
  UNIQUE(student_id, class_id)
);

CREATE TABLE session (
  id INT PRIMARY KEY AUTO_INCREMENT,
  class_id INT NOT NULL,
  session_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  qr_token VARCHAR(100) UNIQUE NOT NULL,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (class_id) REFERENCES class(id)
);

CREATE TABLE attendance (
  id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  session_id INT NOT NULL,
  status ENUM('present', 'absent', 'late', 'excused') DEFAULT 'absent',
  marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  marked_by ENUM('qr', 'teacher', 'admin') DEFAULT 'qr',
  FOREIGN KEY (student_id) REFERENCES student(id),
  FOREIGN KEY (session_id) REFERENCES session(id),
  UNIQUE(student_id, session_id)
);
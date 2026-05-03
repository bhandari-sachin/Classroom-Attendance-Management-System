# Classroom Attendance Management System

---

## 1. Project Title & Overview

The **Classroom Attendance Management System** is a software application designed to simplify and automate attendance tracking in educational institutions. Traditional attendance methods are often time-consuming and error-prone; this system replaces them with an efficient and reliable platform.

It supports three primary users: **Admin, Teacher, and Student**, each with role-specific functionalities. Teachers manage sessions and attendance, students submit attendance via QR codes or manual input, and administrators oversee reports and system data.

The system is built using **Java, JavaFX, MySQL, and Maven**, following a modular layered architecture. Additional features such as **PDF report generation and UI localization** enhance usability and scalability.

The project was developed over **8 Agile sprints (2 weeks each)**.

---

## 2. Product Vision

### Vision Statement
To deliver a reliable, scalable, and user-friendly attendance management system that improves accuracy, accessibility, and reporting in educational environments.

### Goals
- Automate attendance tracking
- Reduce manual errors and paperwork
- Provide real-time attendance insights
- Support multiple user roles
- Enable report generation

### Key Features
- Role-based authentication (Admin, Teacher, Student)
- QR code attendance system
- Manual attendance marking
- PDF report generation
- Attendance statistics dashboard
- Multi-language UI support
- Secure JWT authentication

### Definition of Success
- All features function correctly across roles
- Accurate attendance tracking
- High test coverage and code quality
- Successful report generation
- Stable deployment with Docker

---

## 3. Project Plan & Sprint Structure

**Methodology:** Agile Scrum  
**Sprint Length:** 2 weeks  
**Total Sprints:** 8

### Sprint Timeline

| Sprint | Focus |
|--------|------|
| Sprint 1 | Planning & Vision |
| Sprint 2 | Requirements & Database |
| Sprint 3 | UI & CI |
| Sprint 4 | Docker |
| Sprint 5 | Localization |
| Sprint 6 | Database Localization |
| Sprint 7 | Quality Assurance |
| Sprint 8 | Finalization |

---

## Sprint Documentation

---

## 4. Sprint 1 – Project Planning & Vision

### Key Activities

- Project roadmap defined
- Product backlog created
- Risk and scope identified
- Vision validated

**Links:**
- [Sprint 1 Planning](reports/Sprint_1_Planning_Report.md)  
- [Sprint 1 Review](reports/Sprint_1_Review_Report.md)  

---

## 5. Sprint 2 – Requirements & Database

### Functional Requirements
- Authentication system
- Attendance tracking
- Report generation

### Diagrams
- ![Use Case](diagrams/UseCase02.png)  
- ![ER Diagram](diagrams/ERdiagramV2.png)  

### Database
- MySQL relational schema
- Designed relational schema for users, sessions, and attendance
- implemented database connection and CRUD operations

### Testing
- JUnit + JaCoCo

**Links:**
- [Sprint 2 Planning](reports/Sprint_2_Planning_Report.md)  
- [Sprint 2 Review](reports/Sprint_2_Review_Report.md)  

---

## 6. Sprint 3 – UI Implementation & CI

### UI
- JavaFX-based interface
- Dashboards for all roles

### CI Pipeline (Jenkins)
- Build: Maven
- Test: JUnit
- Coverage: JaCoCo

**Links:**
- [Sprint 3 Planning](reports/Sprint_3_Planning_Report.md)  
- [Sprint 3 Review](reports/Sprint_3_Review_Report.md)  

---

## 7. Sprint 4 – Docker Containerization

### Purpose
- Consistent environments
- Simplified deployment

### Services
- Frontend
- Backend
- MySQL database

### Setup
- Dockerfiles for each service
- docker-compose for orchestration

```bash
docker-compose up --build
```
### Usage
- Run full application with a single command
- Used for testing and development environments

**Links:**
- [Sprint 4 Planning](reports/Sprint_4_Planning_Report.md)  
- [Sprint 4 Review](reports/Sprint_4_Review_Report.md)  

---

## 8. Sprint 5 – UI Localization & Kubernetes

### UI Localization
- Multi-language support implemented
- Dynamic language switching
- Resource bundle approach

### Supported Languages
- English (default)
- Finnish
- Amharic
- Nepali
- Arabic

**Links:**
- [Sprint 5 Planning](reports/Sprint_5_Planning_Report.md)  
- [Sprint 5 Review](reports/Sprint_5_Review_Report.md)  

---

## 9. Sprint 6 – Database Localization

### Implementation
- Schema updates for localization
- Data validation improvements

**Links:**
- [Sprint 6 Planning](reports/Sprint_6_Planning_Report.md)  
- [Sprint 6 Review](reports/Sprint_6_Review_Report.md)  
- [Database Schema](diagrams/ERdiagramExtension.png)  

---

## 10. Sprint 7 – Quality Assurance

### Tools Used:
- SonarQube
- JUnit
- JaCoCo

### Testing
- Functional testing (features validation)
- Non-functional testing (performance, usability)
- Achieved ≥ 80% code coverage
- Identified and fixed bugs
- Improved code quality and maintainability

**Links:**
- [Sprint 7 Planning](reports/Sprint_7_Planning_Report.md)  
- [Sprint 7 Review](reports/Sprint_7_Review_Report.md)  

---

## 11. Sprint 8 – Documentation & Finalization

- Technical documentation
- User guides
- Final architecture

**Links:**
- [Sprint 8 Planning](reports/Sprint_8_Planning_Report.md)  
- [Sprint 8 Review](reports/Sprint_8_Review_Report.md)  

---

## 12. How to Run the Project

### Prerequisites
- Java 17+
- Maven
- Docker & Docker Compose

### Setup
```
git clone <your-repo-url>
cd Classroom-Attendance-System
```

### Run with Docker
```
docker-compose up --build
```

### Access
- Frontend: http://localhost:3000
- Backend: http://localhost:8081

---

## 13. Testing Instructions

### Run Tests
```mvn test```

### Coverage Report
```mvn jacoco:report```

### Open:
```target/site/jacoco/index.html```

---

## 14. Repository Structure
```
/frontend   → JavaFX UI
/backend    → Business logic
/Common     → Shared utilities
/reports    → Documentation
/diagrams   → UML and ER diagrams
/tests      → Test scripts
```

---

## 15. Authors

### Team Members
Olga Chitembo – Backend Developer  
Ahmad Sarfaraz – Frontend Developer  
Farah El Bajta – Frontend Developer  
Melkamu Yehualla – Test Engineer  
Sachin Bhandari – Backend Developer

### Course Info
Course: Software Engineering Project  
Semester: Spring 2026

---

## Main Links
- Documentation:  
https://github.com/bhandari-sachin/Classroom-Attendance-Management-System/tree/main/reports
- Diagrams:  
https://github.com/bhandari-sachin/Classroom-Attendance-Management-System/tree/main/diagrams
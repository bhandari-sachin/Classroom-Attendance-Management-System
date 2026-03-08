# Sprint Review

## Sprint Goal

Extend the functional prototype of the Classroom Attendance Management System, 
complete backend–frontend integration, implement authentication, integrate CI/CD using Jenkins, 
expand automated testing with JaCoCo, and create a working local Docker image.
---

## Completed User Stories / Tasks

* Implemented authentication and authorization module

* Integrated frontend and backend 

* Role-based dashboards (Student, Teacher, Admin)

* Extended attendance logic and validated workflows end-to-end

* Fixed logic bugs and optimized backend performance

---

## Demo Summary (Sprint Review)

# During the demo, we showed:

- Complete end-to-end attendance workflow:

- Login

- Role-based dashboard

- Mark attendance

- View attendance records

- Live Jenkins pipeline execution:

- Build

- Unit tests

- Coverage report generation

- JaCoCo HTML coverage report

- Running application inside a Docker container locally

## CI/CD Integration

* Configured Jenkins pipeline with stages:

- Code checkout

- Build using Apache Maven

- Unit testing using JUnit

- Code coverage generation using JaCoCo

- Enabled automatic pipeline execution on commit

- Verified successful and failing build scenarios

* Docker Image

- Created Dockerfile next spring.

- Built project Docker image locally

- Successfully ran application inside Docker container

---

## What Went Well

* Successful backend–frontend integration

* CI/CD pipeline fully operational

* Improved unit test coverage

* Docker image runs consistently on local machines

* Good collaboration and task distribution

---

## What Could Be Improved

* Earlier CI/CD setup could have reduced late integration pressure

* Some integration bugs required additional debugging time

* Coverage for certain edge cases can still be improved

* Docker optimization (image size reduction) can be enhanced

---

## Postponed / Incomplete Use Cases

* Advanced QR token validation logic

* Deployment to remote server/cloud environment

* Advanced security hardening

* Performance stress testing under heavy load

---

## Next Sprint Focus

* Implement secure QR token validation

* Deploy Docker image to remote environment

* Improve performance and scalability

* Increase unit and integration test coverage

* Strengthen security configuration

Prepare final system demonstration
---

## Time Spent During Sprint

| Team Member      | Role              | Tasks Worked On                                                                                                            | Estimated Time |
|------------------|-------------------|----------------------------------------------------------------------------------------------------------------------------|----------------|
| Sachin Bhandari  | Backend Developer | User model(student/Teacher/Admin), password hashing, login endpoint, signup endpoint, JWT generation, roll based middleware | 8 hours        |
| Olga Chitembo    | Backend           | extended backend functionality, connected admin, student and teacher dashboard to backend                                  | 15 hours       |
| Ahmad Sarfaraz   | Frontend          | Frontend - teacher dashboard                                                                                               | 9 hours        |
| Farah El Bajta   | Frontend          | Login page, signup page, Store JWT, Protected Routs, Auto redirect based role                                              | 12 hours       |
| Melkamu Yehualla | Support           | Backend and frontend unit tests, Integrate Jenkins for CI/CD, Docker Image                                                 | 15 hours       |


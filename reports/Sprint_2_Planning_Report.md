# Sprint 1 Planning Report

## Sprint Number & Dates
- **Sprint:** Sprint 2
- **Start Date:** 27 January
- **End Date:** 09 Febuary
- **Sprint Duration:** 2 weeks

---

## Sprint Goal
The goal of Sprint 2 is to establish the technical foundation of the QR-based attendance system by implementing the initial database structure, starting user interface development, and integrating essential development tools such as unit testing and code coverage.  
The focus of this sprint is on building a stable skeleton of the system rather than completing full features.

---

## Selected Product Backlog Items
- Design and implement a relational database schema for users, classes, and attendance
- Initialize frontend development based on the product vision and Figma designs
- Write unit tests for key backend and frontend components
- Integrate code coverage reporting using JaCoCo

---

## Planned Tasks / Breakdown

### Database
- Create database schema and tables
- Seed database with sample data
- Verify CRUD operations for attendance records

### User Interface
- Set up frontend project structure and routing
- Implement login page with mocked authentication
- Create basic dashboards for:
    - Students (view own attendance)
    - Teachers (view class attendance)
    - Administration (manage classes and users – placeholder)

### Testing
- Write backend unit tests for attendance logic
- Write frontend unit tests for component rendering and basic interactions

### Code Coverage
- Integrate JaCoCo into the Maven build
- Generate HTML coverage reports
- Review coverage for critical logic and publish the report to a public folder

---

## Team Capacity & Assumptions
- Team members are available on a part-time basis due to academic workload
- Sprint duration is fixed at two weeks in accordance with the course schedule
- Lectures and in-class assignments support the implementation of sprint tasks
- Advanced features such as full authentication, QR token validation, and real-time syncing are out of scope for this sprint

---

## Definition of Done
A task is considered **Done** when:
- It satisfies the requirements defined in the sprint plan
- Code is committed and pushed to the project repository
- The database schema is implemented and documented
- Initial UI screens are functional
- Unit tests are written and passing
- Relevant documentation is updated
- Code coverage is generated and reviewed

---
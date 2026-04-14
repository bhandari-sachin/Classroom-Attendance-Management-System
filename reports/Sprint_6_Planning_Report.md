# Sprint 6 Planning Report

## Sprint Number & Dates
Sprint: Sprint 6
Start Date: 01 April
End Date: 14 April
Sprint Duration: 2 weeks

---

## Sprint Goal

The goal of Sprint 6 is to finalize and release the Classroom Attendance Management System by ensuring system stability, improving code quality, completing final testing, and polishing the user interface.

This sprint focuses on bug fixing, performance optimization, finalizing export and localization features, and enforcing code quality using SonarQube.

---

## Selected Product Backlog Items

- Final system testing and bug fixing
- Code quality analysis using SonarQube
- Performance optimization
- Finalize PDF and CSV export features
- Complete multilingual support integration
- UI polishing and usability improvements

---

## Planned Tasks / Breakdown

### Backend & Database

### SonarQube Integration & Fixes

- Run SonarQube analysis on the codebase
- Identify code smells, bugs, and vulnerabilities
- Fix issues such as:
  - Code duplication
  - Unused variables and imports
  - Poor exception handling
  - Hardcoded values
- Improve maintainability and readability of code
- Ensure project meets quality gate requirements

#### Final Backend Refinements

- Optimize database queries for performance
- Refactor any remaining inefficient or duplicated code
- Ensure all endpoints are stable and secure
- Validate data consistency across all modules

---

### Feature Completion

#### Finalize Reporting & Export Features

- Ensure PDF and CSV exports work across all roles (student, teacher, admin)
- Validate formatting and correctness of exported reports
- Handle edge cases (empty reports, invalid filters)

#### Complete Multilingual Support

- Verify all labels and report fields use database-driven translations
- Ensure language switching works correctly across the system
- Fix missing or inconsistent translations

---

### Testing

#### Final System Testing

- Perform end-to-end testing of all features
- Validate attendance marking, reporting, and export workflows
- Ensure frontend and backend integration is fully functional

#### Bug Fixing

- Identify and resolve remaining bugs
- Fix UI inconsistencies and backend errors
- Improve error handling and user feedback messages

---

### User Interface

#### UI/UX Polishing

- Improve layout consistency across all pages
- Refine navigation and user experience
- Ensure responsive and clean design
- Fix alignment, spacing, and visual inconsistencies

---

## Team Capacity & Assumptions

- Team members are working part-time due to academic commitments
- Priority is on stability, quality, and completion
- SonarQube is used to ensure code quality meets project standards

---

## Definition of Done

A task is considered Done when:
- All core features are fully implemented and functional
- Code passes SonarQube quality gates
- Code is committed, reviewed, and pushed to the repository
- System passes end-to-end testing without critical bugs
- Export features (PDF/CSV) work correctly for all roles
- Multilingual support is fully integrated and consistent
- Documentation includes code quality and testing results
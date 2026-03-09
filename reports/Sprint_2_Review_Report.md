# Sprint Review

## Sprint Goal

Implement the core backend logic and database schema for a Classroom Attendance Management System, focusing on manual attendance using session-specific codes, attendance storage, and retrieval. Frontend screens were also completed.

---

## Completed User Stories / Tasks

* Designed relational database schema
* Implemented database connection and SQL repositories
* Implemented attendance logic:
    * Mark attendance by QR code
    * Store attendance status
    * Track how attendance was marked
* Fetch attendance records:
    * By student
    * By class
* Backend filtering logic to support searching attendance by student name or ID
* Frontend attendance pages and UI flows completed
* Local database successfully connected and tested

---

## Demo Summary (Sprint Review)

During the demo, we showed:

* Database schema and relationships
* Backend logic for attendance marking and retrieval
* Frontend attendance page layout and user flow

---

## What Went Well

* Clear separation between backend layers
* Attendance logic works end-to-end with the database
* Team collaboration was effective

---

## What Could Be Improved

* Earlier alignment between schema design and SQL queries would have reduced debugging time
* Testing setup took longer than expected due to foreign key constraints and test data issues
* Frontend and backend are not yet integrated

---

## Postponed / Incomplete Use Cases

* **Frontend–Backend Integration**: API endpoints exist logically but are not yet connected to the frontend
* **Attendance Report Export (PDF)**: Deferred to next sprint
* **Authentication & Authorization**: User login and role-based access not yet implemented

---

## Next Sprint Focus

* Linking the completed frontend with the backend using REST APIs
* Implementing full end-to-end attendance workflows
* Finalizing CI/CD integration using Jenkins
* Expanding unit tests and increasing code coverage with JaCoCo
* Preparing the system for a functional review and live demonstration
* Creating and testing a local Docker image for the application
* Refining backend logic and optimizing database queries where needed

---

## Time Spent During Sprint

| Team Member      | Role              | Tasks Worked On                                                                       | Estimated Time |
|------------------|-------------------|---------------------------------------------------------------------------------------|----------------|
| Sachin Bhandari  | Backend Developer | Database implementation, ER diagram                                                   | 8 hours        |
| Olga Chitembo    | Backend           | Scrum master, Backend - attendance marking, JaCoCo implementation and coverage report | 15 hours       |
| Ahmad Sarfaraz   | Frontend          | Frontend - teacher dashboard                                                          | 9 hours        |
| Farah El Bajta   | Frontend          | Frontend - admin and student dashboard                                                | 12 hours       |
| Melkamu Yehualla | Support           | Backend unit tests                                                                    | 15 hours       |


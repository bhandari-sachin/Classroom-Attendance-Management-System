## API Documentation

### Base URL
```http://localhost:8081/api```

### Authentication
All protected endpoints require a JWT token:

```Authorization: Bearer <JWT_TOKEN>```

## AUTHENTICATION

### Login
POST ```/auth/login```

#### Request
```
{
  "email": "alice@student.com",
  "password": "password"
}
```

#### Response
```
{
  "token": "jwt-token",
  "role": "STUDENT",
  "name": "Alice Brown"
}
```

### Signup
POST ```/auth/signup```
#### Request
```
{
  "firstName": "Alice",
  "lastName": "Brown",
  "email": "alice@student.com",
  "password": "password",
  "role": "STUDENT"
  "studentId": "S12345"
}
```
#### Response
```{
  "message": "User registered successfully"
}
```

## STUDENT ENDPOINTS
### Mark Attendance (QR / Manual Code)

POST ```/attendance/mark```

#### Request
```
{
  "code": "QR_DB_001"
}
```
#### Response
```
{
  "message": "Attendance marked successfully"
}
```

### Get Student Report

GET ```/student/attendance/summary```

#### Query Params
```
studentId=4
year=2026
```
#### Response
```
[
  {
    "className": "Database Systems",
    "present": 8,
    "absent": 2,
    "excused": 1,
    "rate": 72.7
  }
]
```
### Get Student's teachers

GET ```/student/teachers```

#### Response
```
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
    }
]
```

## TEACHER ENDPOINTS
### Get Teacher Classes

GET ```/teacher/classes```

#### Response
```
[
  {
    "id": 1,
    classCode": "CS101-F24",
    "name": "Database Systems"
  }
]
```
### Get Students

GET ```/teacher/students```

#### Response
```
[
  {
    "id": 4,
    "firstName": "Alice",
    "lastName": "Brown",
    "email": "alice@student.com"
  }
]
```
### Create Session (Generate QR Code)

POST ```/teacher/sessions```

#### Request
```
{
  "classId": 1
}
```
#### Response
```
{
  "sessionId": 99,
  "code": "QR_DB_003"
}
```
### Mark Attendance Manually

POST ```/teacher/attendance/mark```

#### Request
```
{
  "studentId": 4,
  "sessionId": 99,
  "status": "PRESENT"
}
```
#### Response
```
{
"message": "Attendance marked successfully"
}
```

### Get Teacher Class Report

GET ```/teacher/reports/session```

#### Query Params
```classId=1```
#### Response
```
[
  {
    "studentName": "Alice Brown",
    "present": 2,
    "absent": 0,
    "excused": 0,
    "total": 2,
    "rate": 100.0
  }
]
```
## ADMIN ENDPOINTS
### Get All Students Attendance Summary

GET ```/admin/attendance/report```

#### Response
```
[
  {
    "studentName": "Alice Brown",
    "present": 10,
    "absent": 2,
    "excused": 1,
    "total": 13,
    "rate": 76.9
  },
  {
    "studentName": "Bob Smith",
    "present": 8,
    "absent": 4,
    "excused": 0,
    "total": 12,
    "rate": 66.7
    }
]
```
## REPORT EXPORT ENDPOINTS
### Export Student Yearly Report (PDF)

GET ```/reports/export/student```

#### Params
```
format=pdf
lang=en
```
#### Response
```
PDF file download to {path}
```
### Export Teacher Class Report (PDF)

GET ```/reports/export/teacher```
#### Params
```
classId=1
format=pdf
lang=en
```
#### Response
```
PDF file download to {path}
```

### Export Admin Report (PDF)

GET ```/reports/export/admin```
#### params
```
format=pdf
lang=en
```
#### Response
```
PDF file download to {path}
```

## ERROR RESPONSES
Example
```
{
  "error": "Invalid QR code"
}
```

Common errors:

401 Unauthorized → Missing/invalid JWT
404 Not Found → Resource doesn’t exist
400 Bad Request → Invalid input

## Notes
#### status values:
```PRESENT | ABSENT | EXCUSED```
#### markedBy values:
```QR | TEACHER```
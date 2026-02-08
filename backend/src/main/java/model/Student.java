package model;

public class Student {

    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private Long studentNumber;

    public Student(Long studentId, String firstName, String lastName, String email, Long studentNumber) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.studentNumber = studentNumber;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public Long getStudentNumber() {
        return studentNumber;
    }

    public String getEmail() {
        return email;
    }

}


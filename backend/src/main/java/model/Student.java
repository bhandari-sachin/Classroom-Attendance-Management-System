package model;

public class Student {

    private Long studentId;
    private String name;
    private String email;

    public Student(Long studentId, String name, String email) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getFirstName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}


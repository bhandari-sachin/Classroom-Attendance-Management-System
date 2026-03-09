package dto;

public class ClassDto {

    private Long id;
    private String name;
    private String classCode;
    private String teacherEmail;
    private Integer studentCount;

    public ClassDto() {
    }

    public ClassDto(
            Long id,
            String name,
            String classCode,
            String teacherEmail,
            Integer studentCount
    ) {
        this.id = id;
        this.name = name;
        this.classCode = classCode;
        this.teacherEmail = teacherEmail;
        this.studentCount = studentCount;
    }

    // ===== Getters (important – used by frontend via reflection) =====

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public Integer getStudentCount() {
        return studentCount;
    }
}
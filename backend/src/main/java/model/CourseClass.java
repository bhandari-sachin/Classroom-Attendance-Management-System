package model;

public class CourseClass {
    private Long id;
    private String classCode;
    private String name;
    private Long teacherId;
    private String semester;
    private String academicYear;
    private Integer maxCapacity;
    private String teacherName;
    private String teacherEmail;

    public CourseClass(Long id,
                       String classCode,
                       String name,
                       Long teacherId,
                       String semester,
                       String academicYear,
                       Integer maxCapacity) {

        this.id = id;
        this.classCode = classCode;
        this.name = name;
        this.teacherId = teacherId;
        this.semester = semester;
        this.academicYear = academicYear;
        this.maxCapacity = maxCapacity;
    }

    public Long getId() {
        return id;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getName() {
        return name;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getSemester() {
        return semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    @Override
    public String toString() {
        return classCode + " - " + name;
    }
}

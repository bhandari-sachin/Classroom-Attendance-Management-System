package model;

public class Class {
    private Long id;
    private String classCode;
    private String name;
    private Long teacherId;
    private String semester;
    private String academicYear;
    private Integer maxCapacity;

    public Class(Long id,
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

    @Override
    public String toString() {
        return classCode + " - " + name;
    }
}

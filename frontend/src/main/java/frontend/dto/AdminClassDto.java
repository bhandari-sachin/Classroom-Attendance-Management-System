package frontend.dto;

public class AdminClassDto {
    public long id;
    public String classCode;
    public String name;
    public String teacherEmail;
    public String semester;
    public String academicYear;
    public int students; // studentsCount from backend mapped as "students"
}
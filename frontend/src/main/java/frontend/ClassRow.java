package frontend;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClassRow {
    private final StringProperty className = new SimpleStringProperty();
    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty teacher = new SimpleStringProperty();
    private final StringProperty schedule = new SimpleStringProperty();
    private final StringProperty students = new SimpleStringProperty();

    public ClassRow(String className, String code, String teacher, String schedule, String students) {
        this.className.set(className);
        this.code.set(code);
        this.teacher.set(teacher);
        this.schedule.set(schedule);
        this.students.set(students);
    }

    public String getClassName() { return className.get(); }
    public StringProperty classNameProperty() { return className; }
    public StringProperty codeProperty() { return code; }
    public StringProperty teacherProperty() { return teacher; }
    public StringProperty scheduleProperty() { return schedule; }
    public StringProperty studentsProperty() { return students; }
}

package frontend;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClassRow {
    private final StringProperty className = new SimpleStringProperty();
    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty teacher = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty schedule = new SimpleStringProperty();
    private final SimpleIntegerProperty students = new SimpleIntegerProperty();

    public ClassRow(String className, String code, String teacher, String email, String schedule, int students) {
        this.className.set(className);
        this.code.set(code);
        this.teacher.set(teacher);
        this.email.set(email);
        this.schedule.set(schedule);
        this.students.set(students);
    }

    public String getClassName() { return className.get(); }
    public StringProperty classNameProperty() { return className; }
    public StringProperty codeProperty() { return code; }
    public StringProperty teacherProperty() { return teacher; }
    public StringProperty emailProperty() { return email; }
    public StringProperty scheduleProperty() { return schedule; }
    public IntegerProperty studentsProperty() { return students; }
}

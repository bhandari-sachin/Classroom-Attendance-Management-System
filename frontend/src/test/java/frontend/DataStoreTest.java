package frontend;

import org.testng.annotations.Test;
import javafx.collections.ObservableList;

import static org.junit.jupiter.api.Assertions.*;

public class DataStoreTest {

    @Test
    void testStudentsListNotNull() {
        ObservableList<StudentRow> students = DataStore.getStudents();
        assertNotNull(students, "Students list should not be null");
    }

    @Test
    void testTeachersListNotNull() {
        ObservableList<TeacherRow> teachers = DataStore.getTeachers();
        assertNotNull(teachers, "Teachers list should not be null");
    }

    @Test
    void testInitialStudentsLoaded() {
        ObservableList<StudentRow> students = DataStore.getStudents();
        assertEquals(3, students.size(), "There should be 3 dummy students");
    }

    @Test
    void testInitialTeachersLoaded() {
        ObservableList<TeacherRow> teachers = DataStore.getTeachers();
        assertEquals(2, teachers.size(), "There should be 2 dummy teachers");
    }

    @Test
    void testAddStudent() {
        ObservableList<StudentRow> students = DataStore.getStudents();
        int initialSize = students.size();

        StudentRow newStudent = new StudentRow("Test Student", "test@school.com", "Present");
        students.add(newStudent);

        assertEquals(initialSize + 1, students.size());
        assertTrue(students.contains(newStudent));
    }

    @Test
    void testAddTeacher() {
        ObservableList<TeacherRow> teachers = DataStore.getTeachers();
        int initialSize = teachers.size();

        TeacherRow newTeacher = new TeacherRow("Test Teacher", "teacher@school.com");
        teachers.add(newTeacher);

        assertEquals(initialSize + 1, teachers.size());
        assertTrue(teachers.contains(newTeacher));
    }
}
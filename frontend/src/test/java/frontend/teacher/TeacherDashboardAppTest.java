package frontend.teacher;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TeacherDashboardAppTest {

    private static TeacherDashboardApp app;

    @BeforeAll
    static void setUpJavaFx() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        app = new TeacherDashboardApp();
    }

    @Test
    void toIntReturnsZeroForNull() {
        assertEquals(0, TeacherDashboardApp.toInt(null));
    }

    @Test
    void toIntReturnsIntValueForNumber() {
        assertEquals(12, TeacherDashboardApp.toInt(12));
        assertEquals(7, TeacherDashboardApp.toInt(7L));
        assertEquals(4, TeacherDashboardApp.toInt(4.9));
    }

    @Test
    void toIntParsesNumericString() {
        assertEquals(25, TeacherDashboardApp.toInt("25"));
    }

    @Test
    void toIntReturnsZeroForInvalidString() {
        assertEquals(0, TeacherDashboardApp.toInt("abc"));
    }

    @Test
    void valueOrReturnsFallbackWhenNull() {
        assertEquals("fallback", TeacherDashboardApp.valueOr(null, "fallback"));
    }

    @Test
    void valueOrReturnsStringValueWhenNotNull() {
        assertEquals("123", TeacherDashboardApp.valueOr(123, "fallback"));
        assertEquals("hello", TeacherDashboardApp.valueOr("hello", "fallback"));
    }

    @Test
    void resolveIconReturnsExpectedIcons() throws Exception {
        String classesLabel = message("teacher.dashboard.stats.classes");
        String studentsLabel = message("teacher.dashboard.stats.students");
        String presentLabel = message("teacher.dashboard.stats.present");
        String absentLabel = message("teacher.dashboard.stats.absent");

        assertEquals("📚", app.resolveIcon(classesLabel));
        assertEquals("👥", app.resolveIcon(studentsLabel));
        assertEquals("✅", app.resolveIcon(presentLabel));
        assertEquals("❌", app.resolveIcon(absentLabel));
        assertEquals("📊", app.resolveIcon("unknown-label"));
    }

    @Test
    void renderTeacherClassesShowsEmptyStateWhenListIsEmpty() throws Exception {
        VBox container = new VBox();

        invokePrivateVoid(
                "renderTeacherClasses",
                new Class<?>[]{VBox.class, List.class},
                container,
                List.of()
        );

        assertEquals(Pos.CENTER, container.getAlignment());
        assertEquals(3, container.getChildren().size());
    }

    @Test
    void renderTeacherClassesRendersClassRowsWhenDataExists() throws Exception {
        VBox container = new VBox();

        List<Map<String, Object>> classes = List.of(
                Map.of(
                        "classCode", "SE101",
                        "name", "Software Engineering",
                        "semester", "Spring",
                        "academicYear", "2026",
                        "studentsCount", 28
                )
        );

        invokePrivateVoid(
                "renderTeacherClasses",
                new Class<?>[]{VBox.class, List.class},
                container,
                classes
        );

        assertEquals(Pos.TOP_LEFT, container.getAlignment());
        assertEquals(1, container.getChildren().size());

        VBox firstRow = (VBox) container.getChildren().getFirst();
        Label title = (Label) firstRow.getChildren().get(0);
        Label meta = (Label) firstRow.getChildren().get(1);

        assertTrue(title.getText().contains("SE101"));
        assertTrue(title.getText().contains("Software Engineering"));
        assertTrue(meta.getText().contains("Spring"));
        assertTrue(meta.getText().contains("2026"));
        assertTrue(meta.getText().contains("28"));
    }

    @Test
    void showClassesErrorReplacesContentWithErrorLabel() throws Exception {
        VBox container = new VBox();
        container.getChildren().add(new Label("old"));

        invokePrivateVoid(
                "showClassesError",
                new Class<?>[]{VBox.class, String.class},
                container,
                "Backend unavailable"
        );

        assertEquals(1, container.getChildren().size());
        assertEquals(Pos.CENTER, container.getAlignment());

        Label errorLabel = (Label) container.getChildren().getFirst();
        assertTrue(errorLabel.getText().contains("Backend unavailable"));
    }


    private static void invokePrivateVoid(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherDashboardApp.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(app, args);
    }

    private static String message(String key) throws Exception {
        var field = TeacherDashboardApp.class.getDeclaredField("helper");
        field.setAccessible(true);
        Object helper = field.get(app);

        Method getMessage = helper.getClass().getMethod("getMessage", String.class);
        return (String) getMessage.invoke(helper, key);
    }
}
package frontend.teacher;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

    @Test
    void buildShouldReturnParent() {
        Scene scene = new Scene(new VBox());
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        AuthState state = Mockito.mock(AuthState.class);

        Mockito.when(state.getName()).thenReturn("Teacher User");

        Parent result = app.build(scene, router, jwtStore, state);

        assertNotNull(result);
    }

    @Test
    void statCardBuildsCardWithValueAndIcon() throws Exception {
        VBox card = invokePrivate(
                "statCard",
                new Class<?>[]{String.class, int.class},
                "Classes",
                12
        );

        assertNotNull(card);
        assertFalse(card.getChildren().isEmpty());
    }

    @Test
    void buildStatsGridPlacesFourCards() throws Exception {
        VBox c1 = new VBox();
        VBox c2 = new VBox();
        VBox c3 = new VBox();
        VBox c4 = new VBox();

        GridPane grid = invokePrivate(
                "buildStatsGrid",
                new Class<?>[]{VBox.class, VBox.class, VBox.class, VBox.class},
                c1, c2, c3, c4
        );

        assertNotNull(grid);
        assertEquals(4, grid.getChildren().size());
    }

    @Test
    void buildClassesContainerReturnsVBox() throws Exception {
        VBox box = invokePrivate("buildClassesContainer");

        assertNotNull(box);
    }

    @Test
    void buildGreetingLabelReturnsStyledLabel() throws Exception {
        Label label = invokePrivate(
                "buildGreetingLabel",
                new Class<?>[]{String.class},
                "Farah"
        );

        assertNotNull(label);
        assertFalse(label.getText().isBlank());
        assertTrue(label.getText().contains("Farah"));
    }

    @Test
    void buildDateLabelReturnsNonBlankLabel() throws Exception {
        Label label = invokePrivate("buildDateLabel");

        assertNotNull(label);
        assertFalse(label.getText().isBlank());
    }

    @Test
    void buildClassesTitleReturnsStyledLabel() throws Exception {
        Label label = invokePrivate("buildClassesTitle");

        assertNotNull(label);
        assertFalse(label.getText().isBlank());
        assertTrue(
                label.getStyleClass().contains("section-title")
                        || label.getStyleClass().contains("subtitle")
                        || label.getStyleClass().contains("title")
        );
    }

    @Test
    void setStatValueUpdatesNestedValueLabel() throws Exception {
        VBox card = invokePrivate(
                "statCard",
                new Class<?>[]{String.class, int.class},
                "Classes",
                3
        );

        invokePrivateVoid(
                "setStatValue",
                new Class<?>[]{VBox.class, int.class},
                card,
                99
        );

        assertTrue(containsLabelText(card, "99"));
    }

    private static String message(String key) throws Exception {
        var field = TeacherDashboardApp.class.getDeclaredField("helper");
        field.setAccessible(true);
        Object helper = field.get(app);

        Method getMessage = helper.getClass().getMethod("getMessage", String.class);
        return (String) getMessage.invoke(helper, key);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName) throws Exception {
        Method method = TeacherDashboardApp.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (T) method.invoke(app);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherDashboardApp.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(app, args);
    }

    private static void invokePrivateVoid(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = TeacherDashboardApp.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(app, args);
    }

    private static boolean containsLabelText(VBox root, String expected) {
        for (var node : root.getChildren()) {
            if (node instanceof Label label && expected.equals(label.getText())) {
                return true;
            }
            if (node instanceof VBox child && containsLabelText(child, expected)) {
                return true;
            }
            if (node instanceof HBox row) {
                for (var inner : row.getChildren()) {
                    if (inner instanceof Label label && expected.equals(label.getText())) {
                        return true;
                    }
                    if (inner instanceof VBox child && containsLabelText(child, expected)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
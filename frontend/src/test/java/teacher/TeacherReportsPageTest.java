package teacher;

import frontend.AppLayout;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.teacher.TeacherReportsPage;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TeacherReportsPageTest {

    private static AuthState mockState;
    private static JwtStore mockJwtStore;
    private static AppRouter mockRouter;

    @BeforeAll
    static void initJfx() throws Exception {
        // Initialize JavaFX toolkit
        Platform.startup(() -> {});
        mockState = Mockito.mock(AuthState.class);
        Mockito.when(mockState.getName()).thenReturn("Mr. Teacher");
        mockJwtStore = Mockito.mock(JwtStore.class);
        mockRouter = Mockito.mock(AppRouter.class);
    }

    @Test
    void testPageBuildComponentsExist() {
        TeacherReportsPage pageBuilder = new TeacherReportsPage();
        Parent root = pageBuilder.build(new Scene(new javafx.scene.layout.VBox()), mockRouter, mockJwtStore, mockState);

        assertNotNull(root, "Root node should not be null");

        ComboBox<?> classBox = findComboBox(root, "Select class");
        assertNotNull(classBox, "Class ComboBox should exist");

        ComboBox<?> sessionBox = findComboBox(root, "Select session");
        assertNotNull(sessionBox, "Session ComboBox should exist");

        Button loadBtn = findButton(root, "Load Report");
        assertNotNull(loadBtn, "Load Report button should exist");

        TableView<?> table = findTableView(root);
        assertNotNull(table, "Report TableView should exist");
    }

    // Recursive search helpers
    private ComboBox<?> findComboBox(javafx.scene.Parent parent, String promptText) {
        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof ComboBox<?> cb && promptText.equals(cb.getPromptText())) return cb;
            if (node instanceof javafx.scene.Parent p) {
                ComboBox<?> found = findComboBox(p, promptText);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Button findButton(javafx.scene.Parent parent, String text) {
        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button b && b.getText().equals(text)) return b;
            if (node instanceof javafx.scene.Parent p) {
                Button found = findButton(p, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private TableView<?> findTableView(javafx.scene.Parent parent) {
        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TableView<?> tv) return tv;
            if (node instanceof javafx.scene.Parent p) {
                TableView<?> found = findTableView(p);
                if (found != null) return found;
            }
        }
        return null;
    }
}
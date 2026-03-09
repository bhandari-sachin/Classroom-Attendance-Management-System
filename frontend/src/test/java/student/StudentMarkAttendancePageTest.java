package student;

import frontend.AppLayout;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.student.StudentMarkAttendancePage;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class StudentMarkAttendancePageTest {

    private static AuthState mockState;
    private static JwtStore mockJwtStore;
    private static AppRouter mockRouter;

    @BeforeAll
    static void initJfx() throws Exception {
        // Initialize JavaFX toolkit
        Platform.startup(() -> {});
        mockState = Mockito.mock(AuthState.class);
        Mockito.when(mockState.getName()).thenReturn("Jane Student");
        mockJwtStore = Mockito.mock(JwtStore.class);
        mockRouter = Mockito.mock(AppRouter.class);
    }

    @Test
    void testPageBuildComponentsExist() {
        StudentMarkAttendancePage pageBuilder = new StudentMarkAttendancePage();
        Parent root = pageBuilder.build(new Scene(new javafx.scene.layout.VBox()), mockRouter, mockJwtStore, mockState);

        assertNotNull(root, "Root node should not be null");

        Button backBtn = findButton(root, "← Back to Dashboard");
        assertNotNull(backBtn, "Back button should exist");

        TextField codeField = findTextField(root);
        assertNotNull(codeField, "Code TextField should exist");

        Button submitBtn = findButton(root, "Submit");
        assertNotNull(submitBtn, "Submit button should exist");
    }

    // Recursive search helpers
    private Button findButton(Parent parent, String text) {
        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button b && b.getText().equals(text)) return b;
            if (node instanceof Parent p) {
                Button found = findButton(p, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private TextField findTextField(Parent parent) {
        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof TextField tf) return tf;
            if (node instanceof Parent p) {
                TextField found = findTextField(p);
                if (found != null) return found;
            }
        }
        return null;
    }
}
package admin;

import frontend.admin.AdminAttendanceReportsPage;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class AdminAttendanceReportsPageTest {

    @BeforeAll
    static void initJavaFX() {
        // Starts JavaFX runtime for tests
        new JFXPanel();
    }

    @Test
    void build_shouldReturnParentNode() {

        AdminAttendanceReportsPage page = new AdminAttendanceReportsPage();

        Scene scene = new Scene(new VBox());

        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        AuthState state = Mockito.mock(AuthState.class);

        Mockito.when(state.getName()).thenReturn("Admin User");

        Parent result = page.build(scene, router, jwtStore, state);

        assertNotNull(result);
    }

    @Test
    void build_shouldWorkWhenNameIsNull() {

        AdminAttendanceReportsPage page = new AdminAttendanceReportsPage();

        Scene scene = new Scene(new VBox());

        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        AuthState state = Mockito.mock(AuthState.class);

        Mockito.when(state.getName()).thenReturn(null);

        Parent result = page.build(scene, router, jwtStore, state);

        assertNotNull(result);
    }

    @Test
    void build_shouldWorkWhenNameIsBlank() {

        AdminAttendanceReportsPage page = new AdminAttendanceReportsPage();

        Scene scene = new Scene(new VBox());

        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        AuthState state = Mockito.mock(AuthState.class);

        Mockito.when(state.getName()).thenReturn("");

        Parent result = page.build(scene, router, jwtStore, state);

        assertNotNull(result);
    }

}
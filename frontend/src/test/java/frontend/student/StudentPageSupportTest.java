package frontend.student;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StudentPageSupportTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void resolveStudentNameShouldReturnStateNameWhenPresent() {
        AuthState state = new AuthState("token", Role.STUDENT, "Oscar");
        HelperClass helper = new HelperClass();

        String result = StudentPageSupport.resolveStudentName(state, helper);

        assertEquals("Oscar", result);
    }

    @Test
    void resolveStudentNameShouldReturnPlaceholderWhenNameIsNull() {
        AuthState state = new AuthState("token", Role.STUDENT, null);
        HelperClass helper = new HelperClass();

        String result = StudentPageSupport.resolveStudentName(state, helper);

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void resolveStudentNameShouldReturnPlaceholderWhenNameIsBlank() {
        AuthState state = new AuthState("token", Role.STUDENT, "   ");
        HelperClass helper = new HelperClass();

        String result = StudentPageSupport.resolveStudentName(state, helper);

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void buildPageContainerShouldCreateStyledVBox() {
        VBox page = StudentPageSupport.buildPageContainer();

        assertNotNull(page);
        assertTrue(page.getStyleClass().contains("page"));
        assertEquals(16.0, page.getSpacing());
        assertEquals(26.0, page.getPadding().getTop());
        assertEquals(26.0, page.getPadding().getRight());
        assertEquals(26.0, page.getPadding().getBottom());
        assertEquals(26.0, page.getPadding().getLeft());
    }

    @Test
    void wrapWithSidebarShouldReturnParent() {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        HelperClass helper = new HelperClass();
        StackPane content = new StackPane();

        Parent result = StudentPageSupport.wrapWithSidebar(
                "Oscar",
                helper,
                content,
                "dashboard",
                router,
                store
        );

        assertNotNull(result);
        assertInstanceOf(BorderPane.class, result);
    }

    @Test
    void wrapWithSidebarShouldContainContentInCenter() {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        HelperClass helper = new HelperClass();
        StackPane content = new StackPane();

        Parent result = StudentPageSupport.wrapWithSidebar(
                "Oscar",
                helper,
                content,
                "dashboard",
                router,
                store
        );

        BorderPane root = (BorderPane) result;
        assertNotNull(root.getCenter());
    }

    private static class TestRouter extends AppRouter {
         String lastRoute;

        TestRouter() {
            super(new Scene(new StackPane()));
        }

        @Override
        public void go(String route) {
            this.lastRoute = route;
        }
    }

    private static class TestJwtStore extends JwtStore {
         boolean cleared;

        @Override
        public Optional<AuthState> load() {
            return Optional.empty();
        }

        @Override
        public void clear() {
            cleared = true;
        }
    }
}
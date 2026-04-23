package frontend.student;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
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

    @Test
    void wrapWithSidebarShouldNavigateToDashboard() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        HelperClass helper = new HelperClass();

        Parent root = StudentPageSupport.wrapWithSidebar(
                "Oscar",
                helper,
                new StackPane(),
                "dashboard",
                router,
                store
        );

        Label nav = findLabelByText(root, helper.getMessage("student.nav.dashboard"));
        assertNotNull(nav);

        runOnFxThreadAndWait(() -> clickNode(nav));

        assertEquals("student-dashboard", router.lastRoute);
    }

    @Test
    void wrapWithSidebarShouldNavigateToMarkAttendance() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        HelperClass helper = new HelperClass();

        Parent root = StudentPageSupport.wrapWithSidebar(
                "Oscar",
                helper,
                new StackPane(),
                "dashboard",
                router,
                store
        );

        Label nav = findLabelByText(root, helper.getMessage("student.nav.markAttendance"));
        assertNotNull(nav);

        runOnFxThreadAndWait(() -> clickNode(nav));

        assertEquals("student-mark", router.lastRoute);
    }

    @Test
    void wrapWithSidebarShouldNavigateToReports() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        HelperClass helper = new HelperClass();

        Parent root = StudentPageSupport.wrapWithSidebar(
                "Oscar",
                helper,
                new StackPane(),
                "dashboard",
                router,
                store
        );

        Label nav = findLabelByText(root, helper.getMessage("student.nav.myAttendance"));
        assertNotNull(nav);

        runOnFxThreadAndWait(() -> clickNode(nav));

        assertEquals("student-attendance", router.lastRoute);
    }

    @Test
    void wrapWithSidebarShouldNavigateToEmail() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        HelperClass helper = new HelperClass();

        Parent root = StudentPageSupport.wrapWithSidebar(
                "Oscar",
                helper,
                new StackPane(),
                "dashboard",
                router,
                store
        );

        Label nav = findLabelByText(root, helper.getMessage("student.nav.email"));
        assertNotNull(nav);

        runOnFxThreadAndWait(() -> clickNode(nav));

        assertEquals("student-email", router.lastRoute);
    }

    @Test
    void wrapWithSidebarShouldClearStoreAndNavigateToLoginOnLogout() throws Exception {
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore();
        HelperClass helper = new HelperClass();

        Parent root = StudentPageSupport.wrapWithSidebar(
                "Oscar",
                helper,
                new StackPane(),
                "dashboard",
                router,
                store
        );

        Label logout = findLabelByText(root, helper.getMessage("teacher.sidebar.logout"));
        assertNotNull(logout);

        runOnFxThreadAndWait(() -> clickNode(logout));

        assertTrue(store.cleared);
        assertEquals("login", router.lastRoute);
    }

    private static Label findLabelByText(Parent root, String text) {
        for (Node node : findAllNodes(root)) {
            if (node instanceof Label label && text.equals(label.getText())) {
                return label;
            }
        }
        return null;
    }

    private static List<Node> findAllNodes(Node root) {
        List<Node> nodes = new ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private static void collectNodes(Node node, List<Node> nodes) {
        nodes.add(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectNodes(child, nodes);
            }
        }
    }

    private static void clickNode(Node node) {
        node.fireEvent(new MouseEvent(
                MouseEvent.MOUSE_CLICKED,
                0, 0, 0, 0,
                MouseButton.PRIMARY,
                1,
                false, false, false, false,
                true, false, false, true, false, false,
                null
        ));
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] error = new Throwable[1];

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error[0] = t;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX task timed out");

        if (error[0] != null) {
            throw new RuntimeException(error[0]);
        }
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
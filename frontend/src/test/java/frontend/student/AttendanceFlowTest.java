package frontend.student;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.Optional;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

@Execution(ExecutionMode.SAME_THREAD)
class AttendanceFlowTest extends ApplicationTest {

    private Scene scene;
    private MockWebServer server;

    @BeforeEach
    void setupServer() throws IOException {
        server = new MockWebServer();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "sessionId": 123,
                            "code": "QR_DB_001"
                        }
                        """));

        server.start(8081);
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Override
    public void start(Stage stage) {

        scene = new Scene(new javafx.scene.layout.StackPane(), 800, 600);

        StudentMarkAttendancePage page = new StudentMarkAttendancePage();

        AuthState fakeState = new AuthState("test-token", Role.STUDENT, "Alice");

        stage.setScene(new Scene(
                page.build(null, new FakeRouter(scene), new FakeJwtStore(), fakeState)
        ));

        stage.show();
    }

    // STEP 1: Page loads successfully
    @Test
    void pageLoadsSuccessfully() {
        verifyThat("#codeField", Node::isVisible);
    }

    // STEP 2: Entering the code
    @Test
    void userCanEnterCode() {
        clickOn("#codeField");
        write("QR_DB_001");
        sleep(1500);

        verifyThat("#codeField", hasText("QR_DB_001"));
        sleep(1500);
    }

    // STEP 3: Submitting the code successfully
    @Test
    void submitButtonCanBeClicked() {
        clickOn("#codeField");
        write("QR_DB_001");
        sleep(1500);

        clickOn("#submitButton");

        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(".dialog-pane", Node::isVisible);
        sleep(1500);
    }

    // STEP 3: Submitting with empty code shows warning
    @Test
    void emptyCodeShowsWarning() {
        clickOn("#submitButton");

        verifyThat(".dialog-pane", Node::isVisible);
        sleep(1500);
    }

    static class FakeRouter extends AppRouter {
        public FakeRouter(Scene scene) {
            super(scene);
        }

        @Override
        public void go(String route) {
            System.out.println("Routing to: " + route);
        }
    }

    static class FakeJwtStore extends JwtStore {

        private AuthState state;

        @Override
        public void save(AuthState state) {
            this.state = state;
        }

        @Override
        public Optional<AuthState> load() {
            return Optional.ofNullable(state);
        }

        @Override
        public void clear() {
            state = null;
        }
    }
}
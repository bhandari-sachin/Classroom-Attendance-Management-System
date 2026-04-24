package frontend.student;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StudentEmailPageTest {

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
    void buildShouldReturnRootNode() {
        StudentEmailPage page = new StudentEmailPage();

        Scene scene = new Scene(new StackPane());
        AppRouter router = new AppRouter(scene);
        JwtStore jwtStore = new JwtStore();
        AuthState state = new AuthState("token", frontend.auth.Role.STUDENT, "Student Test");

        Parent root = page.build(scene, router, jwtStore, state);

        assertNotNull(root);
    }

    @Test
    void buildShouldWorkWhenSceneIsNull() {
        StudentEmailPage page = new StudentEmailPage();

        Scene scene = new Scene(new StackPane());
        AppRouter router = new AppRouter(scene);
        JwtStore jwtStore = new JwtStore();
        AuthState state = new AuthState("token", frontend.auth.Role.STUDENT, "Student Test");

        Parent root = page.build(null, router, jwtStore, state);

        assertNotNull(root);
    }

    @Test
    void handleSceneShouldAcceptNullSceneWithoutThrowing() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        Method method = StudentEmailPage.class.getDeclaredMethod("handleScene", Scene.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(page, (Scene) null));
    }

    @Test
    void handleSceneShouldAcceptNonNullSceneWithoutThrowing() throws Exception {
        StudentEmailPage page = new StudentEmailPage();

        Method method = StudentEmailPage.class.getDeclaredMethod("handleScene", Scene.class);
        method.setAccessible(true);

        Scene scene = new Scene(new StackPane());

        assertDoesNotThrow(() -> method.invoke(page, scene));
    }
}
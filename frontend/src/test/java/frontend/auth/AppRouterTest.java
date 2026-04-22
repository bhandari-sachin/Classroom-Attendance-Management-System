package frontend.auth;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class AppRouterTest {

    private Scene scene;
    private AppRouter router;

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            scene = new Scene(new Group());
            router = new AppRouter(scene);
            latch.countDown();
        });

        latch.await();
    }

    @Test
    void constructor_shouldThrowWhenSceneNull() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new AppRouter(null)
        );

        assertEquals("Scene must not be null", ex.getMessage());
    }

    @Test
    void getScene_shouldReturnSameScene() {
        assertEquals(scene, router.getScene());
    }

    @Test
    void register_shouldThrowWhenNameNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> router.register(null, Group::new)
        );

        assertEquals("Route name must not be null or blank", ex.getMessage());
    }

    @Test
    void register_shouldThrowWhenNameBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> router.register("   ", Group::new)
        );

        assertEquals("Route name must not be null or blank", ex.getMessage());
    }

    @Test
    void register_shouldThrowWhenFactoryNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> router.register("home", null)
        );

        assertEquals("View factory must not be null", ex.getMessage());
    }

    @Test
    void go_shouldSwitchSceneRoot() throws InterruptedException {
        Parent newRoot = new Group();
        router.register("home", () -> newRoot);

        runOnFxThreadAndWait(() -> router.go("home"));

        assertEquals(newRoot, scene.getRoot());
    }

    @Test
    void go_shouldCallFactory() throws InterruptedException {
        AtomicBoolean called = new AtomicBoolean(false);

        router.register("home", () -> {
            called.set(true);
            return new Group();
        });

        runOnFxThreadAndWait(() -> router.go("home"));

        assertTrue(called.get());
    }

    @Test
    void go_shouldThrowWhenRouteNotFound() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> router.go("unknown")
        );

        assertTrue(ex.getMessage().contains("Route not found: unknown"));
    }

    @Test
    void go_shouldThrowWhenFactoryReturnsNull() {
        router.register("broken", () -> null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> router.go("broken")
        );

        assertEquals("View factory returned null for route: broken", ex.getMessage());
    }

    private void runOnFxThreadAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        if (error.get() != null) {
            if (error.get() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(error.get());
        }
    }
}
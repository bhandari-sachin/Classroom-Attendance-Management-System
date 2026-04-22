package frontend.app;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class MainAppTest {

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
    void guardShouldRedirectToLoginAndReturnEmptyPaneWhenUserIsNotAuthenticated() throws Exception {
        MainApp app = new MainApp();
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore(null);
        Supplier<Parent> page = StackPane::new;

        Supplier<Parent> guarded = invokeGuard(app, router, store, Set.of(Role.ADMIN), page);
        Parent result = guarded.get();

        assertInstanceOf(StackPane.class, result);
        assertEquals("login", router.lastRoute);
    }

    @Test
    void guardShouldReturnEmptyPaneWhenRoleIsNotAllowed() throws Exception {
        MainApp app = new MainApp();
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore(new AuthState("token", Role.STUDENT, "Oscar"));
        Supplier<Parent> page = TestPage::new;

        Supplier<Parent> guarded = invokeGuard(app, router, store, Set.of(Role.ADMIN), page);
        Parent result = guarded.get();

        assertInstanceOf(StackPane.class, result);
        assertNull(router.lastRoute);
    }

    @Test
    void guardShouldReturnPageWhenRoleIsAllowed() throws Exception {
        MainApp app = new MainApp();
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore(new AuthState("token", Role.ADMIN, "Oscar"));
        Supplier<Parent> page = TestPage::new;

        Supplier<Parent> guarded = invokeGuard(app, router, store, Set.of(Role.ADMIN), page);
        Parent result = guarded.get();

        assertInstanceOf(TestPage.class, result);
        assertNull(router.lastRoute);
    }

    @Test
    void guardShouldAllowMultipleRolesWhenUserRoleIsIncluded() throws Exception {
        MainApp app = new MainApp();
        TestRouter router = new TestRouter();
        TestJwtStore store = new TestJwtStore(new AuthState("token", Role.TEACHER, "Oscar"));
        Supplier<Parent> page = TestPage::new;

        Supplier<Parent> guarded = invokeGuard(app, router, store, Set.of(Role.ADMIN, Role.TEACHER), page);
        Parent result = guarded.get();

        assertInstanceOf(TestPage.class, result);
        assertNull(router.lastRoute);
    }

    @Test
    void mainShouldNotThrow() {
        assertDoesNotThrow(() -> MainApp.main(new String[]{}));
    }

    @SuppressWarnings("unchecked")
    private Supplier<Parent> invokeGuard(
            MainApp app,
            AppRouter router,
            JwtStore store,
            Set<Role> allowedRoles,
            Supplier<Parent> page
    ) throws Exception {
        Method method = MainApp.class.getDeclaredMethod(
                "guard",
                AppRouter.class,
                JwtStore.class,
                Set.class,
                Supplier.class
        );
        method.setAccessible(true);
        return (Supplier<Parent>) method.invoke(app, router, store, allowedRoles, page);
    }

    private static class TestPage extends StackPane {
    }

    private static class TestJwtStore extends JwtStore {
        private final AuthState state;

        TestJwtStore(AuthState state) {
            this.state = state;
        }

        @Override
        public Optional<AuthState> load() {
            return Optional.ofNullable(state);
        }

        @Override
        public void clear() {
            // no-op
        }

        @Override
        public void save(AuthState authState) {
            // no-op
        }
    }

    private static class TestRouter extends AppRouter {
        private String lastRoute;

        TestRouter() {
            super(new Scene(new StackPane()));
        }

        @Override
        public void go(String route) {
            this.lastRoute = route;
        }
    }
}
package frontend.admin;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AdminDashboardAppTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("JavaFX toolkit failed to start");
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Admin User"})
    void buildShouldReturnWrappedDashboard(String adminName) {
        AdminDashboardApp app = new AdminDashboardApp();

        Scene scene = new Scene(new VBox());
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        AuthState state = Mockito.mock(AuthState.class);

        Mockito.when(state.getName()).thenReturn(adminName);

        Parent result = app.build(scene, router, jwtStore, state);

        assertNotNull(result);
    }
}
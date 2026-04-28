package frontend.teacher;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.ui.HelperClass;
import javafx.embed.swing.JFXPanel;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class TeacherPageSupportTest {

    @BeforeAll
    static void initJavaFx() {
        new JFXPanel();
    }

    @Test
    void resolveTeacherNameReturnsStateNameWhenPresent() {
        AuthState state = new AuthState("token-123", Role.TEACHER, "Farah");
        HelperClass helper = new HelperClass();

        String result = TeacherPageSupport.resolveTeacherName(state, helper);

        assertEquals("Farah", result);
    }

    @Test
    void resolveTeacherNameReturnsFallbackWhenNameIsNull() {
        AuthState state = new AuthState("token-123", Role.TEACHER, null);
        HelperClass helper = new HelperClass();

        String expected = helper.getMessage("teacher.fallback.name");
        String result = TeacherPageSupport.resolveTeacherName(state, helper);

        assertEquals(expected, result);
    }

    @Test
    void resolveTeacherNameReturnsFallbackWhenNameIsBlank() {
        AuthState state = new AuthState("token-123", Role.TEACHER, "   ");
        HelperClass helper = new HelperClass();

        String expected = helper.getMessage("teacher.fallback.name");
        String result = TeacherPageSupport.resolveTeacherName(state, helper);

        assertEquals(expected, result);
    }

    @Test
    void buildPageContainerCreatesExpectedVBox() {
        VBox page = TeacherPageSupport.buildPageContainer();

        assertEquals(14.0, page.getSpacing());
        assertEquals(22.0, page.getPadding().getTop());
        assertEquals(22.0, page.getPadding().getRight());
        assertEquals(22.0, page.getPadding().getBottom());
        assertEquals(22.0, page.getPadding().getLeft());
        assertTrue(page.getStyleClass().contains("page"));
    }

    @Test
    void buildWidePageContainerCreatesExpectedVBox() {
        VBox page = TeacherPageSupport.buildWidePageContainer();

        assertEquals(16.0, page.getSpacing());
        assertEquals(26.0, page.getPadding().getTop());
        assertEquals(26.0, page.getPadding().getRight());
        assertEquals(26.0, page.getPadding().getBottom());
        assertEquals(26.0, page.getPadding().getLeft());
        assertTrue(page.getStyleClass().contains("page"));
    }

    @Test
    void buildAttendancePageContainerCreatesExpectedVBox() {
        VBox page = TeacherPageSupport.buildAttendancePageContainer();

        assertEquals(16.0, page.getSpacing());
        assertEquals(22.0, page.getPadding().getTop());
        assertEquals(22.0, page.getPadding().getRight());
        assertEquals(22.0, page.getPadding().getBottom());
        assertEquals(22.0, page.getPadding().getLeft());
        assertTrue(page.getStyleClass().contains("page"));
    }

    @Test
    void buildExcusePageContainerCreatesExpectedVBox() {
        VBox page = TeacherPageSupport.buildExcusePageContainer();

        assertEquals(12.0, page.getSpacing());
        assertEquals(22.0, page.getPadding().getTop());
        assertEquals(22.0, page.getPadding().getRight());
        assertEquals(22.0, page.getPadding().getBottom());
        assertEquals(22.0, page.getPadding().getLeft());
        assertTrue(page.getStyleClass().contains("page"));
    }

    @ParameterizedTest
    @MethodSource("teacherNavigatorProvider")
    void teacherNavigatorShouldBehaveCorrectly(
            String methodName,
            String expectedRoute,
            boolean isRunnable
    ) throws Exception {

        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        Runnable takeAttendanceAction = Mockito.mock(Runnable.class);

        Object navigator = createTeacherNavigator(router, jwtStore, takeAttendanceAction);

        Method method = navigator.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(navigator);

        if (isRunnable) {
            verify(takeAttendanceAction).run();
        } else {
            verify(router).go(expectedRoute);
        }
    }

    static Stream<Arguments> teacherNavigatorProvider() {
        return Stream.of(
                Arguments.of("goDashboard", "teacher-dashboard", false),
                Arguments.of("goReports", "teacher-reports", false),
                Arguments.of("goEmail", "teacher-email", false)
        );
    }

    @Test
    void teacherNavigatorGoTakeAttendanceShouldRunTakeAttendanceAction() throws Exception {
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        Runnable takeAttendanceAction = Mockito.mock(Runnable.class);

        Object navigator = createTeacherNavigator(router, jwtStore, takeAttendanceAction);

        Method method = navigator.getClass().getDeclaredMethod("goTakeAttendance");
        method.setAccessible(true);
        method.invoke(navigator);

        verify(takeAttendanceAction).run();
    }

    @Test
    void teacherNavigatorLogoutShouldClearJwtAndGoLogin() throws Exception {
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        Runnable takeAttendanceAction = Mockito.mock(Runnable.class);

        Object navigator = createTeacherNavigator(router, jwtStore, takeAttendanceAction);

        Method method = navigator.getClass().getDeclaredMethod("logout");
        method.setAccessible(true);
        method.invoke(navigator);

        verify(jwtStore).clear();
        verify(router).go("login");
    }

    private static Object createTeacherNavigator(
            AppRouter router,
            JwtStore jwtStore,
            Runnable takeAttendanceAction
    ) throws Exception {
        Class<?> navigatorClass = Class.forName("frontend.teacher.TeacherPageSupport$TeacherNavigator");
        Constructor<?> constructor = navigatorClass.getDeclaredConstructor(
                AppRouter.class,
                JwtStore.class,
                Runnable.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(router, jwtStore, takeAttendanceAction);
    }
}
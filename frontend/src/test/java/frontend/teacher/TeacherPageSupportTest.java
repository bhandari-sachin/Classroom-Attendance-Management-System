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
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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

    @Test
    void teacherNavigatorGoDashboardShouldNavigate() throws Exception {
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        Runnable takeAttendanceAction = Mockito.mock(Runnable.class);

        Object navigator = createTeacherNavigator(router, jwtStore, takeAttendanceAction);

        Method method = navigator.getClass().getDeclaredMethod("goDashboard");
        method.setAccessible(true);
        method.invoke(navigator);

        verify(router).go("teacher-dashboard");
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
    void teacherNavigatorGoReportsShouldNavigate() throws Exception {
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        Runnable takeAttendanceAction = Mockito.mock(Runnable.class);

        Object navigator = createTeacherNavigator(router, jwtStore, takeAttendanceAction);

        Method method = navigator.getClass().getDeclaredMethod("goReports");
        method.setAccessible(true);
        method.invoke(navigator);

        verify(router).go("teacher-reports");
    }

    @Test
    void teacherNavigatorGoEmailShouldNavigate() throws Exception {
        AppRouter router = Mockito.mock(AppRouter.class);
        JwtStore jwtStore = Mockito.mock(JwtStore.class);
        Runnable takeAttendanceAction = Mockito.mock(Runnable.class);

        Object navigator = createTeacherNavigator(router, jwtStore, takeAttendanceAction);

        Method method = navigator.getClass().getDeclaredMethod("goEmail");
        method.setAccessible(true);
        method.invoke(navigator);

        verify(router).go("teacher-email");
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
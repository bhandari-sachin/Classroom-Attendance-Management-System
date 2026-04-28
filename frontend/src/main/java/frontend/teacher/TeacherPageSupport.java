package frontend.teacher;

import frontend.app.AppLayout;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public final class TeacherPageSupport {

    private TeacherPageSupport() {
        // Utility class
    }

    public static String resolveTeacherName(AuthState state, HelperClass helper) {
        return (state.name() == null || state.name().isBlank())
                ? helper.getMessage("teacher.fallback.name")
                : state.name();
    }

    public static VBox buildPageContainer() {
        VBox page = new VBox(14);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        return page;
    }

    public static VBox buildWidePageContainer() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");
        return page;
    }

    public static VBox buildAttendancePageContainer() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        return page;
    }

    public static VBox buildExcusePageContainer() {
        VBox page = new VBox(12);
        page.setPadding(new Insets(22));
        page.getStyleClass().add("page");
        return page;
    }

    public static Parent wrapWithSidebar(
            String teacherName,
            HelperClass helper,
            Node content,
            String activeKey,
            AppRouter router,
            JwtStore jwtStore
    ) {
        return wrapWithSidebar(
                teacherName,
                helper,
                content,
                activeKey,
                router,
                jwtStore,
                () -> router.go("teacher-take")
        );
    }

    public static Parent wrapWithSidebar(
            String teacherName,
            HelperClass helper,
            Node content,
            String activeKey,
            AppRouter router,
            JwtStore jwtStore,
            Runnable takeAttendanceAction
    ) {
        AppLayout.SidebarConfig config = new AppLayout.SidebarConfig(
                teacherName,
                helper.getMessage("teacher.sidebar.title"),
                helper.getMessage("teacher.sidebar.menu.dashboard"),
                helper.getMessage("teacher.sidebar.menu.take_attendance"),
                helper.getMessage("teacher.sidebar.menu.reports"),
                helper.getMessage("teacher.sidebar.menu.email")
        );

        return AppLayout.wrapWithSidebar(
                config,
                content,
                activeKey,
                new TeacherNavigator(router, jwtStore, takeAttendanceAction)
        );
    }

    private static final class TeacherNavigator implements AppLayout.Navigator {

        private final AppRouter router;
        private final JwtStore jwtStore;
        private final Runnable takeAttendanceAction;

        private TeacherNavigator(
                AppRouter router,
                JwtStore jwtStore,
                Runnable takeAttendanceAction
        ) {
            this.router = router;
            this.jwtStore = jwtStore;
            this.takeAttendanceAction = takeAttendanceAction;
        }

        @Override
        public void goDashboard() {
            router.go("teacher-dashboard");
        }

        @Override
        public void goTakeAttendance() {
            takeAttendanceAction.run();
        }

        @Override
        public void goReports() {
            router.go("teacher-reports");
        }

        @Override
        public void goEmail() {
            router.go("teacher-email");
        }

        @Override
        public void logout() {
            jwtStore.clear();
            router.go("login");
        }
    }
}
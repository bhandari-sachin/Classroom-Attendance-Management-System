package frontend.student;

import frontend.app.AppLayout;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public final class StudentPageSupport {

    private StudentPageSupport() {
        // Utility class
    }

    public static String resolveStudentName(AuthState state, HelperClass helper) {
        return (state.name() == null || state.name().isBlank())
                ? helper.getMessage("student.name.placeholder")
                : state.name();
    }

    public static VBox buildPageContainer() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(26));
        page.getStyleClass().add("page");
        return page;
    }

    public static Parent wrapWithSidebar(
            String studentName,
            HelperClass helper,
            Node content,
            String activeKey,
            AppRouter router,
            JwtStore jwtStore
    ) {
        return AppLayout.wrapWithSidebar(
                studentName,
                helper.getMessage("student.panel.title"),
                helper.getMessage("student.nav.dashboard"),
                helper.getMessage("student.nav.markAttendance"),
                helper.getMessage("student.nav.myAttendance"),
                helper.getMessage("student.nav.email"),
                content,
                activeKey,
                new AppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        router.go("student-dashboard");
                    }

                    @Override
                    public void goTakeAttendance() {
                        router.go("student-mark");
                    }

                    @Override
                    public void goReports() {
                        router.go("student-attendance");
                    }

                    @Override
                    public void goEmail() {
                        router.go("student-email");
                    }

                    @Override
                    public void logout() {
                        jwtStore.clear();
                        router.go("login");
                    }
                }
        );
    }
}
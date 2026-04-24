package frontend.admin;

import frontend.app.AppLayout;
import javafx.scene.Node;
import javafx.scene.Parent;

public final class AdminAppLayout {

    private AdminAppLayout() {
        // Utility class
    }

    public interface Navigator {
        void goDashboard();
        void goTakeAttendance();
        void goReports();
        void goEmail();
        void logout();
    }

    public record SidebarConfig(
            String name,
            String roleLabel,
            String dashboardLabel,
            String secondLabel,
            String thirdLabel,
            String fourthLabel
    ) {
    }

    public static Parent wrapWithSidebar(
            SidebarConfig config,
            Node content,
            String activeKey,
            Navigator nav
    ) {
        return AppLayout.wrapWithSidebar(
                config.name(),
                config.roleLabel(),
                config.dashboardLabel(),
                config.secondLabel(),
                config.thirdLabel(),
                config.fourthLabel(),
                content,
                activeKey,
                new AppLayout.Navigator() {
                    @Override
                    public void goDashboard() {
                        nav.goDashboard();
                    }

                    @Override
                    public void goTakeAttendance() {
                        nav.goTakeAttendance();
                    }

                    @Override
                    public void goReports() {
                        nav.goReports();
                    }

                    @Override
                    public void goEmail() {
                        nav.goEmail();
                    }

                    @Override
                    public void logout() {
                        nav.logout();
                    }
                }
        );
    }
}
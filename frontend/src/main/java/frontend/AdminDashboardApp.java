package frontend;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class AdminDashboardApp {

    public Parent build(Scene scene, String adminName) {

        Parent scroll = AdminPages.dashboardPage(scene, adminName);

        return AdminAppLayout.wrapWithSidebar(
                adminName,
                "Admin Panel",
                "Dashboard",
                "Manage Classes",
                "Manage Users",
                "Attendance Reports",
                scroll,
                "dashboard",
                new AdminAppLayout.Navigator() {
                    @Override public void goDashboard() { scene.setRoot(new AdminDashboardApp().build(scene, adminName)); }
                    @Override public void goTakeAttendance() { scene.setRoot(new AdminManageClassesPage().build(scene, adminName)); }
                    @Override public void goReports() { scene.setRoot(new AdminManageUsersPage().build(scene, adminName)); }
                    @Override public void goEmail() { scene.setRoot(new AdminAttendanceReportsPage().build(scene, adminName)); }
                    @Override public void logout() { System.out.println("TODO: Admin Logout"); }
                }
        );
    }
}

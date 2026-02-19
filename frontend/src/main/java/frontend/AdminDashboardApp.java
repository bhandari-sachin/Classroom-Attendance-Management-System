package frontend;

import config.AttendanceSQL;
import config.ClassSQL;
import config.SessionSQL;
import config.UserSQL;
import javafx.scene.Parent;
import javafx.scene.Scene;
import service.AttendanceService;
import service.ClassService;
import service.UserService;

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
                    @Override public void goTakeAttendance() { scene.setRoot(new AdminManageClassesPage(new ClassService(new ClassSQL())).build(scene, adminName)); }
                    @Override public void goReports() { scene.setRoot(new AdminManageUsersPage(new UserService(new UserSQL())).build(scene, adminName)); }
                    @Override public void goEmail() { scene.setRoot(new AdminAttendanceReportsPage(new AttendanceService(new AttendanceSQL(), new SessionSQL()),
                            new ClassService(new ClassSQL())).build(scene, adminName)); }
                    @Override public void logout() { System.out.println("TODO: Admin Logout"); }
                }
        );
    }
}

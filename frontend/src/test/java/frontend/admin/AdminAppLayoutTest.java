package frontend.admin;

import frontend.app.AppLayout;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminAppLayoutTest {

    @Test
    void shouldDelegateWrapWithSidebarToAppLayoutAndReturnItsResult() {
        String name = "Test";
        String roleLabel = "Admin";
        String dashboardLabel = "Dashboard";
        String secondLabel = "Attendance";
        String thirdLabel = "Reports";
        String fourthLabel = "Email";
        String activeKey = "dashboard";

        VBox content = new VBox();
        Parent expectedParent = new VBox();

        AdminAppLayout.Navigator nav = mock(AdminAppLayout.Navigator.class);

        try (MockedStatic<AppLayout> mockedStatic = mockStatic(AppLayout.class)) {
            mockedStatic.when(() ->
                    AppLayout.wrapWithSidebar(
                            eq(name),
                            eq(roleLabel),
                            eq(dashboardLabel),
                            eq(secondLabel),
                            eq(thirdLabel),
                            eq(fourthLabel),
                            eq(content),
                            eq(activeKey),
                            any(AppLayout.Navigator.class)
                    )
            ).thenReturn(expectedParent);

            Parent result = AdminAppLayout.wrapWithSidebar(
                    name,
                    roleLabel,
                    dashboardLabel,
                    secondLabel,
                    thirdLabel,
                    fourthLabel,
                    content,
                    activeKey,
                    nav
            );

            assertSame(expectedParent, result);

            mockedStatic.verify(() ->
                    AppLayout.wrapWithSidebar(
                            eq(name),
                            eq(roleLabel),
                            eq(dashboardLabel),
                            eq(secondLabel),
                            eq(thirdLabel),
                            eq(fourthLabel),
                            eq(content),
                            eq(activeKey),
                            any(AppLayout.Navigator.class)
                    )
            );
        }
    }

    @Test
    void shouldForwardGoDashboardFromWrappedNavigator() {
        AdminAppLayout.Navigator nav = mock(AdminAppLayout.Navigator.class);
        VBox content = new VBox();
        Parent expectedParent = new VBox();

        try (MockedStatic<AppLayout> mockedStatic = mockStatic(AppLayout.class)) {
            mockedStatic.when(() ->
                    AppLayout.wrapWithSidebar(
                            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                            any(), anyString(), any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrappedNavigator = invocation.getArgument(8);
                wrappedNavigator.goDashboard();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    "Test",
                    "Admin",
                    "Dashboard",
                    "Attendance",
                    "Reports",
                    "Email",
                    content,
                    "dashboard",
                    nav
            );

            assertSame(expectedParent, result);
            verify(nav).goDashboard();
        }
    }

    @Test
    void shouldForwardGoTakeAttendanceFromWrappedNavigator() {
        AdminAppLayout.Navigator nav = mock(AdminAppLayout.Navigator.class);
        VBox content = new VBox();
        Parent expectedParent = new VBox();

        try (MockedStatic<AppLayout> mockedStatic = mockStatic(AppLayout.class)) {
            mockedStatic.when(() ->
                    AppLayout.wrapWithSidebar(
                            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                            any(), anyString(), any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrappedNavigator = invocation.getArgument(8);
                wrappedNavigator.goTakeAttendance();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    "Test",
                    "Admin",
                    "Dashboard",
                    "Attendance",
                    "Reports",
                    "Email",
                    content,
                    "attendance",
                    nav
            );

            assertSame(expectedParent, result);
            verify(nav).goTakeAttendance();
        }
    }

    @Test
    void shouldForwardGoReportsFromWrappedNavigator() {
        AdminAppLayout.Navigator nav = mock(AdminAppLayout.Navigator.class);
        VBox content = new VBox();
        Parent expectedParent = new VBox();

        try (MockedStatic<AppLayout> mockedStatic = mockStatic(AppLayout.class)) {
            mockedStatic.when(() ->
                    AppLayout.wrapWithSidebar(
                            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                            any(), anyString(), any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrappedNavigator = invocation.getArgument(8);
                wrappedNavigator.goReports();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    "Test",
                    "Admin",
                    "Dashboard",
                    "Attendance",
                    "Reports",
                    "Email",
                    content,
                    "reports",
                    nav
            );

            assertSame(expectedParent, result);
            verify(nav).goReports();
        }
    }

    @Test
    void shouldForwardGoEmailFromWrappedNavigator() {
        AdminAppLayout.Navigator nav = mock(AdminAppLayout.Navigator.class);
        VBox content = new VBox();
        Parent expectedParent = new VBox();

        try (MockedStatic<AppLayout> mockedStatic = mockStatic(AppLayout.class)) {
            mockedStatic.when(() ->
                    AppLayout.wrapWithSidebar(
                            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                            any(), anyString(), any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrappedNavigator = invocation.getArgument(8);
                wrappedNavigator.goEmail();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    "Test",
                    "Admin",
                    "Dashboard",
                    "Attendance",
                    "Reports",
                    "Email",
                    content,
                    "email",
                    nav
            );

            assertSame(expectedParent, result);
            verify(nav).goEmail();
        }
    }

    @Test
    void shouldForwardLogoutFromWrappedNavigator() {
        AdminAppLayout.Navigator nav = mock(AdminAppLayout.Navigator.class);
        VBox content = new VBox();
        Parent expectedParent = new VBox();

        try (MockedStatic<AppLayout> mockedStatic = mockStatic(AppLayout.class)) {
            mockedStatic.when(() ->
                    AppLayout.wrapWithSidebar(
                            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                            any(), anyString(), any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrappedNavigator = invocation.getArgument(8);
                wrappedNavigator.logout();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    "Test",
                    "Admin",
                    "Dashboard",
                    "Attendance",
                    "Reports",
                    "Email",
                    content,
                    "dashboard",
                    nav
            );

            assertSame(expectedParent, result);
            verify(nav).logout();
        }
    }
}
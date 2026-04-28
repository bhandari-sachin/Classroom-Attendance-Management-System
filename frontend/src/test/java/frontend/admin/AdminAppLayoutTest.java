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

    private AppLayout.SidebarConfig createConfig() {
        return new AppLayout.SidebarConfig(
                "Test",
                "Admin",
                "Dashboard",
                "Attendance",
                "Reports",
                "Email"
        );
    }

    @Test
    void shouldDelegateWrapWithSidebarToAppLayoutAndReturnItsResult() {
        AppLayout.SidebarConfig config = createConfig();
        String activeKey = "dashboard";

        VBox content = new VBox();
        Parent expectedParent = new VBox();

        AdminAppLayout.Navigator nav = mock(AdminAppLayout.Navigator.class);

        try (MockedStatic<AppLayout> mockedStatic = mockStatic(AppLayout.class)) {
            mockedStatic.when(() ->
                    AppLayout.wrapWithSidebar(
                            any(AppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AppLayout.Navigator.class)
                    )
            ).thenReturn(expectedParent);

            Parent result = AdminAppLayout.wrapWithSidebar(
                    config,
                    content,
                    activeKey,
                    nav
            );

            assertSame(expectedParent, result);
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
                            any(AppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrapped = invocation.getArgument(3);
                wrapped.goDashboard();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    createConfig(),
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
                            any(AppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrapped = invocation.getArgument(3);
                wrapped.goTakeAttendance();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    createConfig(),
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
                            any(AppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrapped = invocation.getArgument(3);
                wrapped.goReports();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    createConfig(),
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
                            any(AppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrapped = invocation.getArgument(3);
                wrapped.goEmail();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    createConfig(),
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
                            any(AppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AppLayout.Navigator wrapped = invocation.getArgument(3);
                wrapped.logout();
                return expectedParent;
            });

            Parent result = AdminAppLayout.wrapWithSidebar(
                    createConfig(),
                    content,
                    "dashboard",
                    nav
            );

            assertSame(expectedParent, result);
            verify(nav).logout();
        }
    }
}
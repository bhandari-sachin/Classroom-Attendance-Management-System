package frontend.admin;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminPageSupportTest {

    @Test
    void shouldReturnAdminNameWhenStateNameExists() {
        AuthState state = mock(AuthState.class);
        HelperClass helper = mock(HelperClass.class);

        when(state.name()).thenReturn("Oscar");

        String result = AdminPageSupport.resolveAdminName(state, helper);

        assertEquals("Oscar", result);
        verify(helper, never()).getMessage(anyString());
    }

    @Test
    void shouldReturnFallbackNameWhenStateNameIsNull() {
        AuthState state = mock(AuthState.class);
        HelperClass helper = mock(HelperClass.class);

        when(state.name()).thenReturn(null);
        when(helper.getMessage("teacher.fallback.name")).thenReturn("Fallback Name");

        String result = AdminPageSupport.resolveAdminName(state, helper);

        assertEquals("Fallback Name", result);
        verify(helper).getMessage("teacher.fallback.name");
    }

    @Test
    void shouldReturnFallbackNameWhenStateNameIsBlank() {
        AuthState state = mock(AuthState.class);
        HelperClass helper = mock(HelperClass.class);

        when(state.name()).thenReturn("   ");
        when(helper.getMessage("teacher.fallback.name")).thenReturn("Fallback Name");

        String result = AdminPageSupport.resolveAdminName(state, helper);

        assertEquals("Fallback Name", result);
        verify(helper).getMessage("teacher.fallback.name");
    }

    @Test
    void shouldBuildContentContainerCorrectly() {
        VBox result = AdminPageSupport.buildContentContainer();

        assertEquals(14, result.getSpacing());
        assertTrue(result.getStyleClass().contains("content"));
        assertEquals(18, result.getPadding().getTop());
        assertEquals(18, result.getPadding().getRight());
        assertEquals(18, result.getPadding().getBottom());
        assertEquals(18, result.getPadding().getLeft());
    }

    @Test
    void shouldDelegateWrapWithSidebarAndReturnItsResult() {
        HelperClass helper = mock(HelperClass.class);
        AppRouter router = mock(AppRouter.class);
        JwtStore jwtStore = mock(JwtStore.class);
        VBox content = new VBox();
        Parent expected = new VBox();

        when(helper.getMessage("admin.sidebar.title")).thenReturn("Admin");
        when(helper.getMessage("admin.dashboard.title")).thenReturn("Dashboard");
        when(helper.getMessage("admin.classes.title")).thenReturn("Classes");
        when(helper.getMessage("admin.users.title")).thenReturn("Users");
        when(helper.getMessage("admin.reports.title")).thenReturn("Reports");

        try (MockedStatic<AdminAppLayout> mockedStatic = mockStatic(AdminAppLayout.class)) {
            mockedStatic.when(() ->
                    AdminAppLayout.wrapWithSidebar(
                            any(AdminAppLayout.SidebarConfig.class),
                            eq(content),
                            eq("second"),
                            any(AdminAppLayout.Navigator.class)
                    )
            ).thenReturn(expected);

            Parent result = AdminPageSupport.wrapWithSidebar(
                    "Oscar",
                    helper,
                    content,
                    "second",
                    router,
                    jwtStore
            );

            assertSame(expected, result);

            mockedStatic.verify(() ->
                    AdminAppLayout.wrapWithSidebar(
                            argThat(config ->
                                    "Oscar".equals(config.name())
                                            && "Admin".equals(config.roleLabel())
                                            && "Dashboard".equals(config.dashboardLabel())
                                            && "Classes".equals(config.secondLabel())
                                            && "Users".equals(config.thirdLabel())
                                            && "Reports".equals(config.fourthLabel())
                            ),
                            eq(content),
                            eq("second"),
                            any(AdminAppLayout.Navigator.class)
                    )
            );
        }
    }

    @Test
    void shouldNavigateToDashboard() {
        HelperClass helper = mock(HelperClass.class);
        AppRouter router = mock(AppRouter.class);
        JwtStore jwtStore = mock(JwtStore.class);
        VBox content = new VBox();
        Parent expected = new VBox();

        when(helper.getMessage(anyString())).thenReturn("x");

        try (MockedStatic<AdminAppLayout> mockedStatic = mockStatic(AdminAppLayout.class)) {
            mockedStatic.when(() ->
                    AdminAppLayout.wrapWithSidebar(
                            any(AdminAppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AdminAppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AdminAppLayout.Navigator nav = invocation.getArgument(3);
                nav.goDashboard();
                return expected;
            });

            AdminPageSupport.wrapWithSidebar("Oscar", helper, content, "first", router, jwtStore);

            verify(router).go("admin-dashboard");
        }
    }

    @Test
    void shouldNavigateToClasses() {
        HelperClass helper = mock(HelperClass.class);
        AppRouter router = mock(AppRouter.class);
        JwtStore jwtStore = mock(JwtStore.class);
        VBox content = new VBox();
        Parent expected = new VBox();

        when(helper.getMessage(anyString())).thenReturn("x");

        try (MockedStatic<AdminAppLayout> mockedStatic = mockStatic(AdminAppLayout.class)) {
            mockedStatic.when(() ->
                    AdminAppLayout.wrapWithSidebar(
                            any(AdminAppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AdminAppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AdminAppLayout.Navigator nav = invocation.getArgument(3);
                nav.goTakeAttendance();
                return expected;
            });

            AdminPageSupport.wrapWithSidebar("Oscar", helper, content, "second", router, jwtStore);

            verify(router).go("admin-classes");
        }
    }

    @Test
    void shouldNavigateToUsers() {
        HelperClass helper = mock(HelperClass.class);
        AppRouter router = mock(AppRouter.class);
        JwtStore jwtStore = mock(JwtStore.class);
        VBox content = new VBox();
        Parent expected = new VBox();

        when(helper.getMessage(anyString())).thenReturn("x");

        try (MockedStatic<AdminAppLayout> mockedStatic = mockStatic(AdminAppLayout.class)) {
            mockedStatic.when(() ->
                    AdminAppLayout.wrapWithSidebar(
                            any(AdminAppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AdminAppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AdminAppLayout.Navigator nav = invocation.getArgument(3);
                nav.goReports();
                return expected;
            });

            AdminPageSupport.wrapWithSidebar("Oscar", helper, content, "third", router, jwtStore);

            verify(router).go("admin-users");
        }
    }

    @Test
    void shouldNavigateToReports() {
        HelperClass helper = mock(HelperClass.class);
        AppRouter router = mock(AppRouter.class);
        JwtStore jwtStore = mock(JwtStore.class);
        VBox content = new VBox();
        Parent expected = new VBox();

        when(helper.getMessage(anyString())).thenReturn("x");

        try (MockedStatic<AdminAppLayout> mockedStatic = mockStatic(AdminAppLayout.class)) {
            mockedStatic.when(() ->
                    AdminAppLayout.wrapWithSidebar(
                            any(AdminAppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AdminAppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AdminAppLayout.Navigator nav = invocation.getArgument(3);
                nav.goEmail();
                return expected;
            });

            AdminPageSupport.wrapWithSidebar("Oscar", helper, content, "fourth", router, jwtStore);

            verify(router).go("admin-reports");
        }
    }

    @Test
    void shouldClearJwtStoreAndNavigateToLoginOnLogout() {
        HelperClass helper = mock(HelperClass.class);
        AppRouter router = mock(AppRouter.class);
        JwtStore jwtStore = mock(JwtStore.class);
        VBox content = new VBox();
        Parent expected = new VBox();

        when(helper.getMessage(anyString())).thenReturn("x");

        try (MockedStatic<AdminAppLayout> mockedStatic = mockStatic(AdminAppLayout.class)) {
            mockedStatic.when(() ->
                    AdminAppLayout.wrapWithSidebar(
                            any(AdminAppLayout.SidebarConfig.class),
                            any(),
                            anyString(),
                            any(AdminAppLayout.Navigator.class)
                    )
            ).thenAnswer(invocation -> {
                AdminAppLayout.Navigator nav = invocation.getArgument(3);
                nav.logout();
                return expected;
            });

            AdminPageSupport.wrapWithSidebar("Oscar", helper, content, "first", router, jwtStore);

            verify(jwtStore).clear();
            verify(router).go("login");
        }
    }
}
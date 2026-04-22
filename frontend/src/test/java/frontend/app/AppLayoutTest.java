package frontend.app;

import frontend.ui.UiPreferences;
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AppLayoutTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit failed to start");
    }

    @Test
    void constructorShouldBePrivate() throws Exception {
        var constructor = AppLayout.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    void isRtlShouldReturnTrueForArabic() {
        UiPreferences.setLanguage("ar");
        assertTrue(AppLayout.isRtl());
    }

    @Test
    void isRtlShouldReturnFalseForEnglish() {
        UiPreferences.setLanguage("en");
        assertFalse(AppLayout.isRtl());
    }

    @Test
    void isRtlShouldReturnTrueForArabicLocaleVariant() {
        UiPreferences.setLanguage("ar-MA");
        assertTrue(AppLayout.isRtl());
    }

    @Test
    void toOrientationShouldReturnRightToLeftWhenRtlIsTrue() {
        assertEquals(NodeOrientation.RIGHT_TO_LEFT, AppLayout.toOrientation(true));
    }

    @Test
    void toOrientationShouldReturnLeftToRightWhenRtlIsFalse() {
        assertEquals(NodeOrientation.LEFT_TO_RIGHT, AppLayout.toOrientation(false));
    }

    @ParameterizedTest
    @CsvSource({
            "dashboard,dashboard",
            "second,takeAttendance",
            "third,reports",
            "fourth,email",
            "unknown,dashboard"
    })
    void refreshCurrentPageShouldNavigateCorrectly(String activeKey, String expectedAction) {
        TestNavigator nav = new TestNavigator();

        AppLayout.refreshCurrentPage(nav, activeKey);

        assertEquals(expectedAction, nav.lastAction);
    }

    @Test
    void wrapWithSidebarShouldPlaceSidebarOnLeftForLtr() throws Exception {
        UiPreferences.setLanguage("en");
        TestNavigator nav = new TestNavigator();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        new VBox(),
                        "dashboard",
                        nav
                )
        );

        assertInstanceOf(BorderPane.class, parent);
        BorderPane root = (BorderPane) parent;

        assertNotNull(root.getLeft());
        assertNull(root.getRight());
        assertNotNull(root.getCenter());
    }

    @Test
    void wrapWithSidebarShouldPlaceSidebarOnRightForRtl() throws Exception {
        UiPreferences.setLanguage("ar");
        TestNavigator nav = new TestNavigator();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        new VBox(),
                        "dashboard",
                        nav
                )
        );

        assertInstanceOf(BorderPane.class, parent);
        BorderPane root = (BorderPane) parent;

        assertNull(root.getLeft());
        assertNotNull(root.getRight());
        assertNotNull(root.getCenter());
    }

    @Test
    void wrapWithSidebarShouldApplyActiveStyleToMatchingNavItem() throws Exception {
        UiPreferences.setLanguage("en");
        TestNavigator nav = new TestNavigator();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        new VBox(),
                        "third",
                        nav
                )
        );

        List<Label> labels = findAllLabels(parent);
        Label reportsLabel = labels.stream()
                .filter(label -> "Reports".equals(label.getText()))
                .findFirst()
                .orElseThrow();

        assertTrue(reportsLabel.getStyleClass().contains("nav-item"));
        assertTrue(reportsLabel.getStyleClass().contains("nav-item-active"));
    }

    @Test
    void clickingDashboardNavLabelShouldCallNavigator() throws Exception {
        UiPreferences.setLanguage("en");
        TestNavigator nav = new TestNavigator();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        new VBox(),
                        "dashboard",
                        nav
                )
        );

        Label dashboardLabel = findAllLabels(parent).stream()
                .filter(label -> "Dashboard".equals(label.getText()))
                .findFirst()
                .orElseThrow();

        runOnFxThread(() -> {
            dashboardLabel.getOnMouseClicked().handle(null);
            return null;
        });

        assertEquals("dashboard", nav.lastAction);
    }

    @Test
    void clickingAttendanceNavLabelShouldCallNavigator() throws Exception {
        UiPreferences.setLanguage("en");
        TestNavigator nav = new TestNavigator();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        new VBox(),
                        "dashboard",
                        nav
                )
        );

        Label attendanceLabel = findAllLabels(parent).stream()
                .filter(label -> "Attendance".equals(label.getText()))
                .findFirst()
                .orElseThrow();

        runOnFxThread(() -> {
            attendanceLabel.getOnMouseClicked().handle(null);
            return null;
        });

        assertEquals("takeAttendance", nav.lastAction);
    }

    @Test
    void clickingReportsNavLabelShouldCallNavigator() throws Exception {
        UiPreferences.setLanguage("en");
        TestNavigator nav = new TestNavigator();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        new VBox(),
                        "dashboard",
                        nav
                )
        );

        Label reportsLabel = findAllLabels(parent).stream()
                .filter(label -> "Reports".equals(label.getText()))
                .findFirst()
                .orElseThrow();

        runOnFxThread(() -> {
            reportsLabel.getOnMouseClicked().handle(null);
            return null;
        });

        assertEquals("reports", nav.lastAction);
    }

    @Test
    void clickingEmailNavLabelShouldCallNavigator() throws Exception {
        UiPreferences.setLanguage("en");
        TestNavigator nav = new TestNavigator();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        new VBox(),
                        "dashboard",
                        nav
                )
        );

        Label emailLabel = findAllLabels(parent).stream()
                .filter(label -> "Email".equals(label.getText()))
                .findFirst()
                .orElseThrow();

        runOnFxThread(() -> {
            emailLabel.getOnMouseClicked().handle(null);
            return null;
        });

        assertEquals("email", nav.lastAction);
    }

    @Test
    void wrapWithSidebarShouldGrowRegionContent() throws Exception {
        UiPreferences.setLanguage("en");
        TestNavigator nav = new TestNavigator();
        VBox content = new VBox();

        Parent parent = runOnFxThread(() ->
                AppLayout.wrapWithSidebar(
                        "Oscar",
                        "Teacher",
                        "Dashboard",
                        "Attendance",
                        "Reports",
                        "Email",
                        content,
                        "dashboard",
                        nav
                )
        );

        BorderPane root = (BorderPane) parent;
        assertInstanceOf(VBox.class, root.getCenter());

        VBox center = (VBox) root.getCenter();
        assertTrue(center.getChildren().contains(content));
        assertEquals(Double.MAX_VALUE, content.getMaxWidth());
        assertEquals(Double.MAX_VALUE, content.getMaxHeight());
        assertEquals(Priority.ALWAYS, VBox.getVgrow(content));
    }

    private static List<Label> findAllLabels(Node node) {
        List<Label> labels = new ArrayList<>();
        collectLabels(node, labels);
        return labels;
    }

    private static void collectLabels(Node node, List<Label> labels) {
        if (node instanceof Label label) {
            labels.add(label);
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectLabels(child, labels);
            }
        }
    }

    private static <T> T runOnFxThread(FxSupplier<T> supplier) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                result.set(supplier.get());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX operation timed out");

        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }

        return result.get();
    }

    @FunctionalInterface
    private interface FxSupplier<T> {
        T get() throws Exception;
    }

    private static class TestNavigator implements AppLayout.Navigator {
        private String lastAction;

        @Override
        public void goDashboard() {
            lastAction = "dashboard";
        }

        @Override
        public void goTakeAttendance() {
            lastAction = "takeAttendance";
        }

        @Override
        public void goReports() {
            lastAction = "reports";
        }

        @Override
        public void goEmail() {
            lastAction = "email";
        }

        @Override
        public void logout() {
            lastAction = "logout";
        }
    }
}
package frontend.admin;

import frontend.auth.AppRouter;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdminPagesTest {

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

    @ParameterizedTest
    @CsvSource({
            "buildTitle,title",
            "buildSubtitle,subtitle",
            "buildQuickActionsTitle,section-title",
            "buildRecentClassesTitle,section-title"
    })
    void shouldBuildStyledLabel(String methodName, String expectedStyleClass) throws Exception {
        HelperClass helper = new HelperClass();

        Label label = invokePrivateStatic(
                methodName,
                Label.class,
                new Class<?>[]{HelperClass.class},
                helper
        );

        assertNotNull(label);
        assertNotNull(label.getText());
        assertFalse(label.getText().isBlank());
        assertTrue(label.getStyleClass().contains(expectedStyleClass));
    }

    @Test
    void toIntShouldReturnZeroWhenValueIsNull() {
        assertEquals(0, AdminPages.toInt(null));
    }

    @Test
    void toIntShouldReturnIntegerValueWhenNumberIsGiven() {
        assertEquals(12, AdminPages.toInt(12));
        assertEquals(5, AdminPages.toInt(5L));
        assertEquals(7, AdminPages.toInt(7.9));
    }

    @Test
    void toIntShouldParseNumericString() {
        assertEquals(42, AdminPages.toInt("42"));
    }

    @Test
    void toIntShouldReturnZeroForInvalidString() {
        assertEquals(0, AdminPages.toInt("abc"));
        assertEquals(0, AdminPages.toInt(""));
    }

    @Test
    void toDoubleShouldReturnZeroWhenValueIsNull() {
        assertEquals(0.0, AdminPages.toDouble(null));
    }

    @Test
    void toDoubleShouldReturnDoubleValueWhenNumberIsGiven() {
        assertEquals(12.0, AdminPages.toDouble(12));
        assertEquals(5.5, AdminPages.toDouble(5.5));
    }

    @Test
    void toDoubleShouldParseNumericString() {
        assertEquals(42.5, AdminPages.toDouble("42.5"));
    }

    @Test
    void toDoubleShouldReturnZeroForInvalidString() {
        assertEquals(0.0, AdminPages.toDouble("abc"));
        assertEquals(0.0, AdminPages.toDouble(""));
    }

    @Test
    void valueOrShouldReturnFallbackWhenValueIsNull() {
        assertEquals("fallback", AdminPages.valueOr(null, "fallback"));
    }

    @Test
    void valueOrShouldReturnStringValueWhenValueIsPresent() {
        assertEquals("hello", AdminPages.valueOr("hello", "fallback"));
        assertEquals("123", AdminPages.valueOr(123, "fallback"));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorWhenThrowableIsNull() {
        assertEquals("Unknown error", AdminPages.safeErrorMessage(null));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorWhenMessageIsNull() {
        assertEquals("Unknown error", AdminPages.safeErrorMessage(new RuntimeException((String) null)));
    }

    @Test
    void safeErrorMessageShouldReturnUnknownErrorWhenMessageIsBlank() {
        assertEquals("Unknown error", AdminPages.safeErrorMessage(new RuntimeException("   ")));
    }

    @Test
    void safeErrorMessageShouldReturnThrowableMessageWhenPresent() {
        assertEquals("Something failed", AdminPages.safeErrorMessage(new RuntimeException("Something failed")));
    }


    @Test
    void buildSubtitleShouldReturnStyledLabel() throws Exception {
        HelperClass helper = new HelperClass();

        Label subtitle = invokePrivateStatic("buildSubtitle", Label.class, new Class<?>[]{HelperClass.class}, helper);

        assertNotNull(subtitle);
        assertNotNull(subtitle.getText());
        assertFalse(subtitle.getText().isBlank());
        assertTrue(subtitle.getStyleClass().contains("subtitle"));
    }


    @Test
    void buildStatsGridShouldPlaceCardsInTwoByTwoLayout() throws Exception {
        Pane totalClassesCard = new VBox();
        Pane studentsCard = new VBox();
        Pane teachersCard = new VBox();
        Pane rateCard = new VBox();

        GridPane grid = invokePrivateStatic(
                "buildStatsGrid",
                GridPane.class,
                new Class<?>[]{Pane.class, Pane.class, Pane.class, Pane.class},
                totalClassesCard, studentsCard, teachersCard, rateCard
        );

        assertNotNull(grid);
        assertTrue(grid.getStyleClass().contains("grid"));
        assertEquals(14.0, grid.getHgap());
        assertEquals(14.0, grid.getVgap());
        assertEquals(2, grid.getColumnConstraints().size());
        assertEquals(Priority.ALWAYS, grid.getColumnConstraints().get(0).getHgrow());
        assertEquals(Priority.ALWAYS, grid.getColumnConstraints().get(1).getHgrow());

        assertTrue(grid.getChildren().contains(totalClassesCard));
        assertTrue(grid.getChildren().contains(studentsCard));
        assertTrue(grid.getChildren().contains(teachersCard));
        assertTrue(grid.getChildren().contains(rateCard));

        assertEquals(0, GridPane.getColumnIndex(totalClassesCard) == null ? 0 : GridPane.getColumnIndex(totalClassesCard));
        assertEquals(0, GridPane.getRowIndex(totalClassesCard) == null ? 0 : GridPane.getRowIndex(totalClassesCard));

        assertEquals(1, GridPane.getColumnIndex(studentsCard));
        assertEquals(0, GridPane.getRowIndex(studentsCard) == null ? 0 : GridPane.getRowIndex(studentsCard));

        assertEquals(0, GridPane.getColumnIndex(teachersCard) == null ? 0 : GridPane.getColumnIndex(teachersCard));
        assertEquals(1, GridPane.getRowIndex(teachersCard));

        assertEquals(1, GridPane.getColumnIndex(rateCard));
        assertEquals(1, GridPane.getRowIndex(rateCard));
    }

    @Test
    void buildRecentGridShouldContainLoadingLabelInitially() throws Exception {
        HelperClass helper = new HelperClass();

        GridPane grid = invokePrivateStatic("buildRecentGrid", GridPane.class, new Class<?>[]{HelperClass.class}, helper);

        assertNotNull(grid);
        assertEquals(14.0, grid.getHgap());
        assertEquals(14.0, grid.getVgap());
        assertEquals(2, grid.getColumnConstraints().size());
        assertEquals(1, grid.getChildren().size());

        Node firstNode = grid.getChildren().getFirst();
        assertInstanceOf(Label.class, firstNode);

        Label loadingLabel = (Label) firstNode;
        assertNotNull(loadingLabel.getText());
        assertFalse(loadingLabel.getText().isBlank());
        assertTrue(loadingLabel.getStyleClass().contains("subtitle"));
    }

    @Test
    void buildQuickActionsShouldCreateThreeClickableCards() throws Exception {
        HelperClass helper = new HelperClass();
        AppRouter router = mock(AppRouter.class);

        HBox quickActions = invokePrivateStatic(
                "buildQuickActions",
                HBox.class,
                new Class<?>[]{HelperClass.class, AppRouter.class},
                helper, router
        );

        assertNotNull(quickActions);
        assertEquals(14.0, quickActions.getSpacing());
        assertTrue(quickActions.getStyleClass().contains("quick-actions"));
        assertEquals(3, quickActions.getChildren().size());

        Pane manageClasses = (Pane) quickActions.getChildren().get(0);
        Pane manageUsers = (Pane) quickActions.getChildren().get(1);
        Pane reports = (Pane) quickActions.getChildren().get(2);

        assertNotNull(manageClasses.getOnMouseClicked());
        assertNotNull(manageUsers.getOnMouseClicked());
        assertNotNull(reports.getOnMouseClicked());

        fireClick(manageClasses);
        verify(router).go("admin-classes");

        fireClick(manageUsers);
        verify(router).go("admin-users");

        fireClick(reports);
        verify(router).go("admin-reports");
    }

    @Test
    void setStatCardValueShouldUpdateNestedLabelWhenStructureMatches() throws Exception {
        Label titleLabel = new Label("Title");
        Label valueLabel = new Label("Loading");

        VBox leftBox = new VBox(titleLabel, valueLabel);
        Label iconLabel = new Label("📘");
        HBox topBox = new HBox(leftBox, iconLabel);
        VBox card = new VBox(topBox);

        invokePrivateStaticVoid(
                "setStatCardValue",
                new Class<?>[]{Pane.class, String.class},
                card, "25"
        );

        assertEquals("25", valueLabel.getText());
    }

    @Test
    void setStatCardValueShouldDoNothingWhenPaneStructureDoesNotMatch() {
        VBox card = new VBox(new Label("Simple"));

        assertDoesNotThrow(() ->
                invokePrivateStaticVoid(
                        "setStatCardValue",
                        new Class<?>[]{Pane.class, String.class},
                        card, "25"
                )
        );
    }

    @Test
    void renderRecentClassesShouldShowEmptyMessageWhenClassesListIsNull() throws Exception {
        HelperClass helper = new HelperClass();
        GridPane recentGrid = new GridPane();
        recentGrid.add(new Label("old"), 0, 0);

        invokePrivateStaticVoid(
                "renderRecentClasses",
                new Class<?>[]{HelperClass.class, GridPane.class, List.class},
                helper, recentGrid, null
        );

        assertEquals(1, recentGrid.getChildren().size());
        assertInstanceOf(Label.class, recentGrid.getChildren().getFirst());

        Label label = (Label) recentGrid.getChildren().getFirst();
        assertNotNull(label.getText());
        assertFalse(label.getText().isBlank());
        assertTrue(label.getStyleClass().contains("subtitle"));
    }

    @Test
    void renderRecentClassesShouldShowEmptyMessageWhenClassesListIsEmpty() throws Exception {
        HelperClass helper = new HelperClass();
        GridPane recentGrid = new GridPane();

        invokePrivateStaticVoid(
                "renderRecentClasses",
                new Class<?>[]{HelperClass.class, GridPane.class, List.class},
                helper, recentGrid, List.of()
        );

        assertEquals(1, recentGrid.getChildren().size());
        assertInstanceOf(Label.class, recentGrid.getChildren().getFirst());
    }

    @Test
    void renderRecentClassesShouldRenderAtMostFourCards() throws Exception {
        HelperClass helper = new HelperClass();
        GridPane recentGrid = new GridPane();

        List<Map<String, Object>> classes = List.of(
                Map.of("name", "Math", "classCode", "MTH101", "teacherEmail", "t1@example.com", "semester", "Spring", "academicYear", "2025"),
                Map.of("name", "Physics", "classCode", "PHY202", "teacherEmail", "t2@example.com", "semester", "Autumn", "academicYear", "2025"),
                Map.of("name", "Chemistry", "classCode", "CHE303", "teacherEmail", "t3@example.com", "semester", "Spring", "academicYear", "2026"),
                Map.of("name", "Biology", "classCode", "BIO404", "teacherEmail", "t4@example.com", "semester", "Autumn", "academicYear", "2026"),
                Map.of("name", "History", "classCode", "HIS505", "teacherEmail", "t5@example.com", "semester", "Winter", "academicYear", "2027")
        );

        invokePrivateStaticVoid(
                "renderRecentClasses",
                new Class<?>[]{HelperClass.class, GridPane.class, List.class},
                helper, recentGrid, classes
        );

        assertEquals(4, recentGrid.getChildren().size());
    }

    @Test
    void renderRecentClassesShouldUseFallbackValuesWhenFieldsAreMissing() throws Exception {
        HelperClass helper = new HelperClass();
        GridPane recentGrid = new GridPane();

        List<Map<String, Object>> classes = List.of(
                Map.of()
        );

        invokePrivateStaticVoid(
                "renderRecentClasses",
                new Class<?>[]{HelperClass.class, GridPane.class, List.class},
                helper, recentGrid, classes
        );

        assertEquals(1, recentGrid.getChildren().size());
        assertInstanceOf(Pane.class, recentGrid.getChildren().getFirst());
    }

    @Test
    void showDashboardLoadErrorShouldSetFailedValuesAndRenderErrorLabel() throws Exception {
        HelperClass helper = new HelperClass();

        VBox totalClassesCard = createStatCard();
        VBox studentsCard = createStatCard();
        VBox teachersCard = createStatCard();
        VBox rateCard = createStatCard();

        GridPane recentGrid = new GridPane();
        recentGrid.add(new Label("old"), 0, 0);

        invokePrivateStaticVoid(
                "showDashboardLoadError",
                new Class<?>[]{HelperClass.class, Pane.class, Pane.class, Pane.class, Pane.class, GridPane.class, String.class},
                helper, totalClassesCard, studentsCard, teachersCard, rateCard, recentGrid, "Backend offline"
        );

        assertEquals(helper.getMessage("common.status.failed"), extractStatCardValue(totalClassesCard));
        assertEquals(helper.getMessage("common.status.failed"), extractStatCardValue(studentsCard));
        assertEquals(helper.getMessage("common.status.failed"), extractStatCardValue(teachersCard));
        assertEquals(helper.getMessage("common.status.failed"), extractStatCardValue(rateCard));

        assertEquals(1, recentGrid.getChildren().size());
        assertInstanceOf(Label.class, recentGrid.getChildren().getFirst());

        Label errorLabel = (Label) recentGrid.getChildren().getFirst();
        assertTrue(errorLabel.getText().contains("Backend offline"));
        assertTrue(errorLabel.getStyleClass().contains("subtitle"));
    }

    private static VBox createStatCard() {
        Label titleLabel = new Label("Title");
        Label valueLabel = new Label("Loading");
        VBox leftBox = new VBox(titleLabel, valueLabel);
        Label iconLabel = new Label("📘");
        HBox topBox = new HBox(leftBox, iconLabel);
        return new VBox(topBox);
    }

    private static String extractStatCardValue(VBox card) {
        HBox topBox = (HBox) card.getChildren().getFirst();
        VBox leftBox = (VBox) topBox.getChildren().getFirst();
        Label valueLabel = (Label) leftBox.getChildren().get(1);
        return valueLabel.getText();
    }

    private static void fireClick(Pane pane) {
        MouseEvent event = new MouseEvent(
                MouseEvent.MOUSE_CLICKED,
                0, 0, 0, 0,
                MouseButton.PRIMARY,
                1,
                false, false, false, false,
                true, false, false,
                true, false, false, null
        );
        pane.getOnMouseClicked().handle(event);
    }

    private static void invokePrivateStaticVoid(
            String methodName,
            Class<?>[] parameterTypes,
            Object... args
    ) throws Exception {
        Method method = AdminPages.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        method.invoke(null, args);
    }

    private static <T> T invokePrivateStatic(
            String methodName,
            Class<T> returnType,
            Class<?>[] parameterTypes,
            Object... args
    ) throws Exception {
        Method method = AdminPages.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object result = method.invoke(null, args);
        return returnType.cast(result);
    }

}
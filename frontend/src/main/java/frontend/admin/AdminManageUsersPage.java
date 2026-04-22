package frontend.admin;

import frontend.ui.UserRow;
import frontend.api.AdminApi;
import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.dto.AdminUserDto;
import frontend.dto.AdminUsersResponseDto;
import frontend.ui.HelperClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminManageUsersPage {

    private static final Logger LOGGER =
            Logger.getLogger(AdminManageUsersPage.class.getName());

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        String adminName = AdminPageSupport.resolveAdminName(state, helper);

        VBox content = AdminPageSupport.buildContentContainer();

        Label title = buildTitle();
        Label subtitle = buildSubtitle();
        HBox summaryRow = buildSummaryRow();

        TextField searchField = buildSearchField();
        ComboBox<String> typeFilter = buildTypeFilter();
        HBox filtersRow = buildFiltersRow(searchField, typeFilter);

        Label loadError = buildLoadErrorLabel();

        TableView<UserRow> table = AdminUI.buildUsersTable();
        table.getItems().clear();

        ObservableList<UserRow> rows = FXCollections.observableArrayList();
        FilteredList<UserRow> filteredRows = new FilteredList<>(rows, row -> true);
        table.setItems(filteredRows);

        String studentLabel = helper.getMessage("admin.users.filter.student");
        String teacherLabel = helper.getMessage("admin.users.filter.teacher");
        String adminLabel = helper.getMessage("admin.users.filter.admin");

        Runnable applyFilter = () -> applyFilter(
                filteredRows,
                searchField,
                typeFilter,
                studentLabel,
                teacherLabel,
                adminLabel
        );

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter.run());
        typeFilter.setOnAction(event -> applyFilter.run());

        AdminApi adminApi = new AdminApi("http://localhost:8081", jwtStore);

        Runnable reload = () -> loadUsers(
                adminApi,
                summaryRow,
                rows,
                loadError,
                applyFilter
        );

        reload.run();

        content.getChildren().addAll(
                title,
                subtitle,
                summaryRow,
                filtersRow,
                loadError,
                table
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return AdminPageSupport.wrapWithSidebar(
                adminName,
                helper,
                scroll,
                "third",
                router,
                jwtStore
        );
    }

    private Label buildTitle() {
        Label title = new Label(helper.getMessage("admin.users.title"));
        title.getStyleClass().add("title");
        return title;
    }

    private Label buildSubtitle() {
        Label subtitle = new Label(helper.getMessage("admin.users.subtitle"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    private HBox buildSummaryRow() {
        HBox summary = new HBox(12);
        summary.getStyleClass().add("summary-row");
        return summary;
    }

    private TextField buildSearchField() {
        TextField search = new TextField();
        search.setPromptText(helper.getMessage("admin.users.search.placeholder"));
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);
        return search;
    }

    private ComboBox<String> buildTypeFilter() {
        String allTypesLabel = helper.getMessage("admin.users.filter.allTypes");
        String studentLabel = helper.getMessage("admin.users.filter.student");
        String teacherLabel = helper.getMessage("admin.users.filter.teacher");
        String adminLabel = helper.getMessage("admin.users.filter.admin");

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll(
                allTypesLabel,
                studentLabel,
                teacherLabel,
                adminLabel
        );
        type.setValue(allTypesLabel);
        type.getStyleClass().add("filter-combo");
        return type;
    }

    private HBox buildFiltersRow(TextField searchField, ComboBox<String> typeFilter) {
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getChildren().addAll(searchField, typeFilter);
        return filters;
    }

    private Label buildLoadErrorLabel() {
        Label loadError = new Label();
        loadError.getStyleClass().add("subtitle");
        loadError.setManaged(false);
        loadError.setVisible(false);
        return loadError;
    }

    void applyFilter(
            FilteredList<UserRow> filteredRows,
            TextField searchField,
            ComboBox<String> typeFilter,
            String studentLabel,
            String teacherLabel,
            String adminLabel
    ) {
        String query = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String selectedType = typeFilter.getValue();

        filteredRows.setPredicate(row -> {
            if (row == null) {
                return false;
            }

            boolean matchesText = query.isBlank()
                    || safe(row.getUser()).contains(query)
                    || safe(row.getType()).contains(query)
                    || safe(row.getEnrolled()).contains(query);

            boolean matchesType = true;

            if (studentLabel.equalsIgnoreCase(selectedType)) {
                matchesType = studentLabel.equalsIgnoreCase(row.getType());
            } else if (teacherLabel.equalsIgnoreCase(selectedType)) {
                matchesType = teacherLabel.equalsIgnoreCase(row.getType());
            } else if (adminLabel.equalsIgnoreCase(selectedType)) {
                matchesType = adminLabel.equalsIgnoreCase(row.getType());
            }

            return matchesText && matchesType;
        });
    }

    private void loadUsers(
            AdminApi adminApi,
            HBox summaryRow,
            ObservableList<UserRow> rows,
            Label loadError,
            Runnable applyFilter
    ) {
        hideLabel(loadError);

        new Thread(() -> {
            try {
                AdminUsersResponseDto data = adminApi.getAdminUsers();

                Platform.runLater(() -> {
                    updateSummaryCards(summaryRow, data);
                    rows.setAll(mapUserRows(data));
                    applyFilter.run();
                });
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load admin users.", ex);
                Platform.runLater(() -> showLabel(
                        loadError,
                        helper.getMessage("admin.users.loadError") + " "
                                + (ex.getMessage() == null ? "Unknown error" : ex.getMessage())
                ));
            }
        }).start();
    }

    private void updateSummaryCards(HBox summaryRow, AdminUsersResponseDto data) {
        summaryRow.getChildren().setAll(
                AdminUI.smallSummaryCard(
                        helper.getMessage("admin.users.summary.students"),
                        String.valueOf(data.getStudents()),
                        "🎓",
                        "accent-green"
                ),
                AdminUI.smallSummaryCard(
                        helper.getMessage("admin.users.summary.teachers"),
                        String.valueOf(data.getTeachers()),
                        "👥",
                        "accent-purple"
                ),
                AdminUI.smallSummaryCard(
                        helper.getMessage("admin.users.summary.admins"),
                        String.valueOf(data.getAdmins()),
                        "🛡",
                        "accent-orange"
                )
        );
    }

    ObservableList<UserRow> mapUserRows(AdminUsersResponseDto data) {
        ObservableList<UserRow> mappedRows = FXCollections.observableArrayList();

        if (data.getUsers() == null) {
            return mappedRows;
        }

        for (AdminUserDto user : data.getUsers()) {
            String userCell = nullToEmpty(user.getName()) + "\n" + nullToEmpty(user.getEmail());
            String localizedRole = localizeRole(user.getRole());
            String enrolledText = localizeEnrolled(user.getEnrolled());

            mappedRows.add(new UserRow(userCell, localizedRole, enrolledText));
        }

        return mappedRows;
    }

    String localizeRole(String role) {
        if (role == null) {
            return "";
        }

        return switch (role.trim().toUpperCase()) {
            case "STUDENT" -> helper.getMessage("admin.users.filter.student");
            case "TEACHER" -> helper.getMessage("admin.users.filter.teacher");
            case "ADMIN" -> helper.getMessage("admin.users.filter.admin");
            default -> role;
        };
    }

    String localizeEnrolled(String enrolled) {
        if (enrolled == null || enrolled.isBlank()) {
            return helper.getMessage("common.status.noData");
        }
        return enrolled;
    }

    private void showLabel(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    static String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
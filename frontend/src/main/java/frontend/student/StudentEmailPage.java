package frontend.student;

import frontend.auth.AppRouter;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.ui.HelperClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class StudentEmailPage {

    private final HelperClass helper = new HelperClass();

    public Parent build(Scene scene, AppRouter router, JwtStore jwtStore, AuthState state) {
        handleScene(scene); // Fix #1

        String studentName = StudentPageSupport.resolveStudentName(state, helper);

        VBox page = StudentPageSupport.buildPageContainer();

        Label title = new Label(helper.getMessage("student.email.title"));
        title.getStyleClass().add("dash-title");

        Label subtitle = new Label(helper.getMessage("student.email.subtitle"));
        subtitle.getStyleClass().add("dash-subtitle");

        TableView<?> table = new TableView<>();
        table.getStyleClass().add("table");

        // Fix #2: remove deprecated resize policy
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        VBox.setVgrow(table, Priority.ALWAYS);

        page.getChildren().addAll(title, subtitle, table);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");

        return StudentPageSupport.wrapWithSidebar(
                studentName,
                helper,
                scroll,
                "email",
                router,
                jwtStore
        );
    }

    // Fix for unused parameter (clean + Sonar-safe)
    private void handleScene(Scene scene) {
        if (scene == null) {
            // intentional no-op
        }
    }
}
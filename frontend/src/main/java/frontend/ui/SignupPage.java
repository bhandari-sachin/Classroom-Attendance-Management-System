package frontend.ui;

import frontend.auth.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class SignupPage extends StackPane {

    private final HelperClass helper = new HelperClass();

    public SignupPage(AppRouter router, AuthService authService, JwtStore jwtStore) {

        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(420);
        card.setPadding(new Insets(22));

        Label title = new Label(helper.getMessage("signup.title"));
        title.getStyleClass().add("title");

        Label sub = new Label(helper.getMessage("signup.subtitle"));
        sub.getStyleClass().add("subtitle");

        TextField firstName = new TextField();
        firstName.setPromptText(helper.getMessage("signup.firstname.placeholder"));

        TextField lastName = new TextField();
        lastName.setPromptText(helper.getMessage("signup.lastname.placeholder"));

        TextField email = new TextField();
        email.setPromptText(helper.getMessage("signup.email.placeholder"));

        PasswordField password = new PasswordField();
        password.setPromptText(helper.getMessage("signup.password.placeholder"));

        ComboBox<Role> role = new ComboBox<>();
        role.getItems().addAll(Role.STUDENT, Role.TEACHER);
        role.setValue(Role.STUDENT);
        role.setMaxWidth(Double.MAX_VALUE);

        role.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == Role.STUDENT
                            ? helper.getMessage("signup.role.student")
                            : helper.getMessage("signup.role.teacher"));
                }
            }
        });

        role.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item == Role.STUDENT
                            ? helper.getMessage("signup.role.student")
                            : helper.getMessage("signup.role.teacher"));
                }
            }
        });

        Label studentCodeLabel = new Label(helper.getMessage("signup.studentcode.label"));
        studentCodeLabel.getStyleClass().add("field-label");

        TextField studentCode = new TextField();
        studentCode.setPromptText(helper.getMessage("signup.studentcode.placeholder"));

        studentCodeLabel.visibleProperty().bind(role.valueProperty().isEqualTo(Role.STUDENT));
        studentCode.visibleProperty().bind(role.valueProperty().isEqualTo(Role.STUDENT));
        studentCodeLabel.managedProperty().bind(studentCodeLabel.visibleProperty());
        studentCode.managedProperty().bind(studentCode.visibleProperty());

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setVisible(false);
        error.setManaged(false);

        Label info = new Label();
        info.getStyleClass().add("subtitle");
        info.setVisible(false);
        info.setManaged(false);

        Button signupBtn = new Button(helper.getMessage("signup.button.submit"));
        signupBtn.getStyleClass().add("primary-btn");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        Button goLogin = new Button(helper.getMessage("signup.button.login"));
        goLogin.getStyleClass().add("link-button");
        goLogin.setOnAction(e -> router.go("login"));

        signupBtn.setOnAction(e -> {
            hideMsg(error);
            hideMsg(info);

            String fn = firstName.getText().trim();
            String ln = lastName.getText().trim();
            String em = email.getText().trim();
            String pw = password.getText();
            Role r = role.getValue();
            String sc = studentCode.getText().trim();

            if (fn.isBlank() || ln.isBlank() || em.isBlank() || pw.isBlank()) {
                showMsg(error, helper.getMessage("signup.error.required_fields"));
                return;
            }
            if (!em.contains("@")) {
                showMsg(error, helper.getMessage("signup.error.invalid_email"));
                return;
            }
            if (pw.length() < 6) {
                showMsg(error, helper.getMessage("signup.error.password_short"));
                return;
            }
            if (r == Role.STUDENT && sc.isBlank()) {
                showMsg(error, helper.getMessage("signup.error.student_code_required"));
                return;
            }

            signupBtn.setDisable(true);

            new Thread(() -> {
                try {
                    authService.signup(fn, ln, em, pw, r, (r == Role.STUDENT ? sc : null));

                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        showMsg(info, helper.getMessage("signup.success.created"));
                        router.go("login");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        showMsg(error, helper.getMessage("signup.error.failed") + " " + ex.getMessage());
                    });
                }
            }).start();
        });

        card.getChildren().addAll(
                title, sub,
                field(helper.getMessage("signup.firstname.label"), firstName),
                field(helper.getMessage("signup.lastname.label"), lastName),
                field(helper.getMessage("signup.email.label"), email),
                field(helper.getMessage("signup.password.label"), password),
                field(helper.getMessage("signup.role.label"), role),
                studentCodeLabel, studentCode,
                error,
                info,
                signupBtn,
                goLogin
        );

        StackPane.setAlignment(card, Pos.CENTER);
        getChildren().add(card);

        Optional<AuthState> existing = jwtStore.load();
        existing.ifPresent(state -> router.go(RoleRedirect.routeFor(state.getRole())));
    }

    private static VBox field(String label, Control input) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        return new VBox(6, l, input);
    }

    private static void showMsg(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private static void hideMsg(Label lbl) {
        lbl.setVisible(false);
        lbl.setManaged(false);
        lbl.setText("");
    }
}
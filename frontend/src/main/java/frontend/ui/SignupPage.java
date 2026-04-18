package frontend.ui;

import frontend.auth.*;
import frontend.i18n.FrontendI18n;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class SignupPage extends StackPane {

    public SignupPage(AppRouter router, AuthService authService, JwtStore jwtStore) {

        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(420);
        card.setPadding(new javafx.geometry.Insets(22));

        Label title = new Label(t("signup.title", "Create Account"));
        title.getStyleClass().add("title");

        Label sub = new Label(t("signup.subtitle", "Sign up to continue"));
        sub.getStyleClass().add("subtitle");

        TextField firstName = new TextField();
        firstName.setPromptText(t("signup.firstname.placeholder", "First name"));

        TextField lastName = new TextField();
        lastName.setPromptText(t("signup.lastname.placeholder", "Last name"));

        TextField email = new TextField();
        email.setPromptText(t("signup.email.placeholder", "Email"));

        PasswordField password = new PasswordField();
        password.setPromptText(t("signup.password.placeholder", "Password"));

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
                            ? t("signup.role.student", "Student")
                            : t("signup.role.teacher", "Teacher"));
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
                            ? t("signup.role.student", "Student")
                            : t("signup.role.teacher", "Teacher"));
                }
            }
        });

        Label studentCodeLabel = new Label(t("signup.studentcode.label", "Student Code"));
        studentCodeLabel.getStyleClass().add("field-label");

        TextField studentCode = new TextField();
        studentCode.setPromptText(t("signup.studentcode.placeholder", "Enter student code"));

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

        Button signupBtn = new Button(t("signup.button.submit", "Sign Up"));
        signupBtn.getStyleClass().add("primary-btn");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        Button goLogin = new Button(t("signup.button.login", "Back to Login"));
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
                showMsg(error, t("signup.error.required_fields", "Please fill in all required fields."));
                return;
            }
            if (!em.contains("@")) {
                showMsg(error, t("signup.error.invalid_email", "Please enter a valid email address."));
                return;
            }
            if (pw.length() < 6) {
                showMsg(error, t("signup.error.password_short", "Password must be at least 6 characters."));
                return;
            }
            if (r == Role.STUDENT && sc.isBlank()) {
                showMsg(error, t("signup.error.student_code_required", "Student code is required."));
                return;
            }

            signupBtn.setDisable(true);

            new Thread(() -> {
                try {
                    authService.signup(fn, ln, em, pw, r, (r == Role.STUDENT ? sc : null));

                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        showMsg(info, t("signup.success.created", "Account created successfully."));
                        router.go("login");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        showMsg(error, t("signup.error.failed", "Signup failed:") + " " + ex.getMessage());
                    });
                }
            }).start();
        });

        card.getChildren().addAll(
                title, sub,
                field(t("signup.firstname.label", "First Name"), firstName),
                field(t("signup.lastname.label", "Last Name"), lastName),
                field(t("signup.email.label", "Email"), email),
                field(t("signup.password.label", "Password"), password),
                field(t("signup.role.label", "Role"), role),
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

    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}
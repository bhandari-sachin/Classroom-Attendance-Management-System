package frontend.ui;

import frontend.auth.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class SignupPage extends StackPane {

    public SignupPage(AppRouter router, AuthService authService, JwtStore jwtStore) {

        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(420);
        card.setPadding(new Insets(22));

        Label title = new Label("Create account");
        title.getStyleClass().add("title");

        Label sub = new Label("Sign up to start using the system");
        sub.getStyleClass().add("subtitle");

        TextField firstName = new TextField();
        firstName.setPromptText("First name");

        TextField lastName = new TextField();
        lastName.setPromptText("Last name");

        TextField email = new TextField();
        email.setPromptText("Email");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        ComboBox<Role> role = new ComboBox<>();
        role.getItems().addAll(Role.STUDENT, Role.TEACHER);
        role.setValue(Role.STUDENT);
        role.setMaxWidth(Double.MAX_VALUE);

        Label studentCodeLabel = new Label("Student code");
        studentCodeLabel.getStyleClass().add("field-label");

        TextField studentCode = new TextField();
        studentCode.setPromptText("e.g. 123456");

        // Only show studentCode for STUDENT
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

        Button signupBtn = new Button("Sign up");
        signupBtn.getStyleClass().add("primary-btn");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        Button goLogin = new Button("I already have an account");
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
                showMsg(error, "Please fill in all required fields.");
                return;
            }
            if (!em.contains("@")) {
                showMsg(error, "Please enter a valid email.");
                return;
            }
            if (pw.length() < 6) {
                showMsg(error, "Password must be at least 6 characters.");
                return;
            }
            if (r == Role.STUDENT && sc.isBlank()) {
                showMsg(error, "Student code is required for students.");
                return;
            }

            signupBtn.setDisable(true);

            new Thread(() -> {
                try {
                    authService.signup(fn, ln, em, pw, r, (r == Role.STUDENT ? sc : null));

                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        showMsg(info, "Account created. Please login.");
                        router.go("login");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        showMsg(error, "Signup failed: " + ex.getMessage());
                    });
                }
            }).start();
        });

        card.getChildren().addAll(
                title, sub,
                field("First name", firstName),
                field("Last name", lastName),
                field("Email", email),
                field("Password", password),
                field("Role", role),
                studentCodeLabel, studentCode,
                error,
                info,
                signupBtn,
                goLogin
        );

        StackPane.setAlignment(card, Pos.CENTER);
        getChildren().add(card);

        // Auto redirect if already logged in
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
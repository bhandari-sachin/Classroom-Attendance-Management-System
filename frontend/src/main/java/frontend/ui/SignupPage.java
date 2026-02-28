package frontend.ui;

import frontend.auth.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

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

        // ===== Fields =====
        TextField name = new TextField();
        name.setPromptText("Full name");

        TextField email = new TextField();
        email.setPromptText("Email");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        ComboBox<Role> role = new ComboBox<>();
        role.getItems().addAll(Role.STUDENT, Role.TEACHER, Role.ADMIN);
        role.setValue(Role.STUDENT);
        role.setMaxWidth(Double.MAX_VALUE);

        Label idLabel = new Label("Student number");
        idLabel.getStyleClass().add("field-label");

        TextField idNumber = new TextField();
        idNumber.setPromptText("e.g. 123456");

        // Change label depending on role
        role.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == Role.STUDENT) {
                idLabel.setText("Student number");
                idNumber.setPromptText("e.g. 123456");
            } else {
                idLabel.setText("Staff number");
                idNumber.setPromptText("e.g. ST-90812");
            }
        });

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setVisible(false);
        error.setManaged(false);

        Button signupBtn = new Button("Sign up");
        signupBtn.getStyleClass().add("primary-btn");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        Button goLogin = new Button("I already have an account");
        goLogin.getStyleClass().add("link-button");
        goLogin.setOnAction(e -> router.go("login"));

        // ===== Action =====
        signupBtn.setOnAction(e -> {
            error.setVisible(false);
            error.setManaged(false);

            String n = name.getText().trim();
            String em = email.getText().trim();
            String pw = password.getText();
            Role r = role.getValue();
            String id = idNumber.getText().trim();

            if (n.isBlank() || em.isBlank() || pw.isBlank() || id.isBlank()) {
                showError(error, "Please fill in all fields.");
                return;
            }
            if (!em.contains("@")) {
                showError(error, "Please enter a valid email.");
                return;
            }
            if (pw.length() < 6) {
                showError(error, "Password must be at least 6 characters.");
                return;
            }

            signupBtn.setDisable(true);

            // Run HTTP call off the JavaFX thread
            new Thread(() -> {
                try {
                    AuthState state = authService.signup(n, em, pw, r, id);

                    javafx.application.Platform.runLater(() -> {
                        jwtStore.save(state);
                        router.go(RoleRedirect.routeFor(state.getRole()));
                    });

                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        showError(error, "Signup failed: " + ex.getMessage());
                        signupBtn.setDisable(false);
                    });
                }
            }).start();
        });

        // Layout
        VBox.setMargin(title, new Insets(0, 0, 4, 0));

        card.getChildren().addAll(
                title, sub,
                field("Full name", name),
                field("Email", email),
                field("Password", password),
                field("Role", role),
                idLabel, idNumber,
                error,
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
        VBox box = new VBox(6, l, input);
        return box;
    }

    private static void showError(Label error, String msg) {
        error.setText(msg);
        error.setVisible(true);
        error.setManaged(true);
    }
}
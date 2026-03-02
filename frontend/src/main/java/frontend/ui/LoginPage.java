package frontend.ui;

import frontend.auth.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class LoginPage extends StackPane {

    public LoginPage(AppRouter router, AuthService authService, JwtStore jwtStore) {

        setPadding(new Insets(24));

        VBox card = new VBox(12);
        card.setMaxWidth(420);
        card.setPadding(new Insets(22));
        card.getStyleClass().add("card");

        Label title = new Label("Welcome back");
        title.getStyleClass().add("title");

        Label sub = new Label("Log in to continue");
        sub.getStyleClass().add("subtitle");

        TextField email = new TextField();
        email.setPromptText("Email");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setManaged(false);
        error.setVisible(false);

        Button loginBtn = new Button("Log in");
        loginBtn.getStyleClass().add("primary-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        //  REAL LOGIN (BACKEND)
        loginBtn.setOnAction(e -> {
            error.setVisible(false);
            error.setManaged(false);

            String em = email.getText().trim();
            String pw = password.getText();

            if (em.isBlank() || pw.isBlank()) {
                showError(error, "Please enter email and password.");
                return;
            }

            loginBtn.setDisable(true);

            new Thread(() -> {
                try {
                    AuthState state = authService.login(em, pw);

                    Platform.runLater(() -> {
                        jwtStore.save(state);
                        router.go(RoleRedirect.routeFor(state.getRole()));
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showError(error, "Login failed: " + cleanMessage(ex.getMessage()));
                        loginBtn.setDisable(false);
                    });
                }
            }).start();
        });

        // ===== DEV QUICK LOGIN (NO BACKEND) =====
        Label devLabel = new Label("Dev quick login");
        devLabel.getStyleClass().add("subtitle");

        Button devAdmin = new Button("Login as Admin");
        Button devTeacher = new Button("Login as Teacher");
        Button devStudent = new Button("Login as Student");

        devAdmin.setMaxWidth(Double.MAX_VALUE);
        devTeacher.setMaxWidth(Double.MAX_VALUE);
        devStudent.setMaxWidth(Double.MAX_VALUE);

        // reuse your existing button style
        devAdmin.getStyleClass().add("primary-btn");
        devTeacher.getStyleClass().add("primary-btn");
        devStudent.getStyleClass().add("primary-btn");

        devAdmin.setOnAction(e -> {
            jwtStore.save(new AuthState("mock-token-admin", Role.ADMIN, "Admin User", 1L));
            router.go(RoleRedirect.routeFor(Role.ADMIN));
        });

        devTeacher.setOnAction(e -> {
            jwtStore.save(new AuthState("mock-token-teacher", Role.TEACHER, "Teacher User", 2L));
            router.go(RoleRedirect.routeFor(Role.TEACHER));
        });

        devStudent.setOnAction(e -> {
            jwtStore.save(new AuthState("mock-token-student", Role.STUDENT, "Student User", 3L));
            router.go(RoleRedirect.routeFor(Role.STUDENT));
        });

        VBox devBox = new VBox(8, devLabel, devAdmin, devTeacher, devStudent);
        devBox.setPadding(new Insets(10, 0, 0, 0));

        Button goSignup = new Button("I don't have an account");
        goSignup.getStyleClass().add("link-button");
        goSignup.setOnAction(e -> router.go("signup"));

        card.getChildren().addAll(
                title,
                sub,
                email,
                password,
                error,
                loginBtn,
                goSignup,
                devBox
        );

        StackPane.setAlignment(card, Pos.CENTER);
        getChildren().add(card);

        // auto redirect if already logged in
        Optional<AuthState> existing = jwtStore.load();
        existing.ifPresent(state -> router.go(RoleRedirect.routeFor(state.getRole())));
    }

    private static void showError(Label error, String msg) {
        error.setText(msg);
        error.setVisible(true);
        error.setManaged(true);
    }

    private static String cleanMessage(String msg) {
        if (msg == null || msg.isBlank()) return "Unknown error";
        return msg.length() > 200 ? msg.substring(0, 200) + "..." : msg;
    }
}
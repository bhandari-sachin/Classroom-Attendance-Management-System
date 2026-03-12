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
                goSignup
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
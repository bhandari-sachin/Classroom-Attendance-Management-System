package frontend.ui;

import frontend.auth.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class LoginPage extends StackPane {

    private final HelperClass helper = new HelperClass();

    public LoginPage(AppRouter router, AuthService authService, JwtStore jwtStore) {

        setPadding(new Insets(24));

        VBox card = new VBox(12);
        card.setMaxWidth(420);
        card.setPadding(new Insets(22));
        card.getStyleClass().add("card");

        Label title = new Label(helper.getMessage("auth.login.title"));
        title.getStyleClass().add("title");

        Label sub = new Label(helper.getMessage("auth.login.subtitle"));
        sub.getStyleClass().add("subtitle");

        TextField email = new TextField();
        email.setPromptText(helper.getMessage("login.email.placeholder"));

        PasswordField password = new PasswordField();
        password.setPromptText(helper.getMessage("login.password.placeholder"));

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setManaged(false);
        error.setVisible(false);

        Button loginBtn = new Button(helper.getMessage("login.button.submit"));
        loginBtn.getStyleClass().add("primary-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            error.setVisible(false);
            error.setManaged(false);

            String em = email.getText().trim();
            String pw = password.getText();

            if (em.isBlank() || pw.isBlank()) {
                showError(error, helper.getMessage("login.error.empty_fields"));
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
                        showError(
                                error,
                                helper.getMessage("login.error.failed") + " " + cleanMessage(ex.getMessage())
                        );
                        loginBtn.setDisable(false);
                    });
                }
            }).start();
        });

        Button goSignup = new Button(helper.getMessage("login.button.signup"));
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

        Optional<AuthState> existing = jwtStore.load();
        existing.ifPresent(state -> router.go(RoleRedirect.routeFor(state.getRole())));
    }

    private static void showError(Label error, String msg) {
        error.setText(msg);
        error.setVisible(true);
        error.setManaged(true);
    }

    private String cleanMessage(String msg) {
        if (msg == null || msg.isBlank()) return helper.getMessage("login.error.unknown");
        return msg.length() > 200 ? msg.substring(0, 200) + "..." : msg;
    }
}
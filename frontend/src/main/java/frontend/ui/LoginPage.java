package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.RoleRedirect;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Login page for user authentication.
 *
 * <p>This page allows the user to:
 * enter email and password,
 * authenticate against the backend,
 * persist the JWT state,
 * and redirect to the correct dashboard based on role.</p>
 */
public class LoginPage extends BaseAuthPage {

    public LoginPage(AppRouter router, AuthService authService, JwtStore jwtStore) {
        validateAutoLogin(router, jwtStore);

        Label titleLabel = createTitleLabel("auth.login.title", "Login");
        Label subtitleLabel = createSubtitleLabel("auth.login.subtitle", "Sign in to your account");

        TextField emailField = createEmailField();
        PasswordField passwordField = createPasswordField();
        Label errorLabel = createMessageLabel("error");

        Button loginButton = createLoginButton(router, authService, jwtStore, emailField, passwordField, errorLabel);
        Button signupButton = createSignupButton(router);

        VBox card = createCard(
                titleLabel,
                subtitleLabel,
                emailField,
                passwordField,
                errorLabel,
                loginButton,
                signupButton
        );

        showCenteredCard(card);
    }

    private TextField createEmailField() {
        TextField emailField = new TextField();
        emailField.setPromptText(t("login.email.placeholder", "Email"));
        return emailField;
    }

    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(t("login.password.placeholder", "Password"));
        return passwordField;
    }

    private Button createLoginButton(AppRouter router,
                                     AuthService authService,
                                     JwtStore jwtStore,
                                     TextField emailField,
                                     PasswordField passwordField,
                                     Label errorLabel) {

        Button loginButton = createPrimaryButton("login.button.submit", "Login");

        loginButton.setOnAction(event -> attemptLogin(
                router,
                authService,
                jwtStore,
                emailField,
                passwordField,
                errorLabel,
                loginButton
        ));

        return loginButton;
    }

    private Button createSignupButton(AppRouter router) {
        Button signupButton = createLinkButton("login.button.signup", "Create account");
        signupButton.setOnAction(event -> router.go("signup"));
        return signupButton;
    }

    private void attemptLogin(AppRouter router,
                              AuthService authService,
                              JwtStore jwtStore,
                              TextField emailField,
                              PasswordField passwordField,
                              Label errorLabel,
                              Button loginButton) {

        hideMessage(errorLabel);

        String email = safeTrim(emailField.getText());
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showMessage(errorLabel, t("login.error.empty_fields", "Please fill all fields"));
            return;
        }

        loginButton.setDisable(true);

        Thread loginThread = new Thread(() -> {
            try {
                AuthState state = authService.login(email, password);

                Platform.runLater(() -> {
                    jwtStore.save(state);
                    router.go(RoleRedirect.routeFor(state.role()));
                });

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    showMessage(errorLabel, t("login.error.failed", "Login interrupted."));
                    loginButton.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showMessage(
                            errorLabel,
                            t("login.error.failed", "Login failed:") + " " + cleanMessage(ex.getMessage())
                    );
                    loginButton.setDisable(false);
                });
            }
        });

        loginThread.setName("login-thread");
        loginThread.setDaemon(true);
        loginThread.start();
    }
}
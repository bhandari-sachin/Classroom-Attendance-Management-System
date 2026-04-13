package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.RoleRedirect;
import frontend.i18n.FrontendI18n;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Login page for user authentication.
 *
 * <p>This page allows the user to:
 * enter email and password,
 * authenticate against the backend,
 * persist the JWT state,
 * and redirect to the correct dashboard based on role.</p>
 */
public class LoginPage extends StackPane {

    private static final double CARD_MAX_WIDTH = 420;
    private static final int ROOT_PADDING = 24;
    private static final int CARD_PADDING = 22;
    private static final int CARD_SPACING = 12;

    public LoginPage(AppRouter router, AuthService authService, JwtStore jwtStore) {
        validateAutoLogin(router, jwtStore);

        setPadding(new Insets(ROOT_PADDING));

        Label titleLabel = createTitleLabel();
        Label subtitleLabel = createSubtitleLabel();

        TextField emailField = createEmailField();
        PasswordField passwordField = createPasswordField();

        Label errorLabel = createErrorLabel();

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

        StackPane.setAlignment(card, Pos.CENTER);
        getChildren().add(card);
    }

    /**
     * Redirects the user immediately if an authentication state already exists.
     */
    private void validateAutoLogin(AppRouter router, JwtStore jwtStore) {
        Optional<AuthState> existingState = jwtStore.load();
        existingState.ifPresent(state -> router.go(RoleRedirect.routeFor(state.getRole())));
    }

    /**
     * Creates the page title label.
     */
    private Label createTitleLabel() {
        Label title = new Label(t("auth.login.title", "Login"));
        title.getStyleClass().add("title");
        return title;
    }

    /**
     * Creates the subtitle label.
     */
    private Label createSubtitleLabel() {
        Label subtitle = new Label(t("auth.login.subtitle", "Sign in to your account"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    /**
     * Creates the email input field.
     */
    private TextField createEmailField() {
        TextField emailField = new TextField();
        emailField.setPromptText(t("login.email.placeholder", "Email"));
        return emailField;
    }

    /**
     * Creates the password input field.
     */
    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(t("login.password.placeholder", "Password"));
        return passwordField;
    }

    /**
     * Creates the error label used to display validation or login errors.
     */
    private Label createErrorLabel() {
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        return errorLabel;
    }

    /**
     * Creates the login button and binds the login action.
     */
    private Button createLoginButton(AppRouter router,
                                     AuthService authService,
                                     JwtStore jwtStore,
                                     TextField emailField,
                                     PasswordField passwordField,
                                     Label errorLabel) {

        Button loginButton = new Button(t("login.button.submit", "Login"));
        loginButton.getStyleClass().add("primary-btn");
        loginButton.setMaxWidth(Double.MAX_VALUE);

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

    /**
     * Creates the signup navigation button.
     */
    private Button createSignupButton(AppRouter router) {
        Button signupButton = new Button(t("login.button.signup", "Create account"));
        signupButton.getStyleClass().add("link-button");
        signupButton.setOnAction(event -> router.go("signup"));
        return signupButton;
    }

    /**
     * Creates the main card container.
     */
    private VBox createCard(javafx.scene.Node... children) {
        VBox card = new VBox(CARD_SPACING);
        card.setMaxWidth(CARD_MAX_WIDTH);
        card.setPadding(new Insets(CARD_PADDING));
        card.getStyleClass().add("card");
        card.getChildren().addAll(children);
        return card;
    }

    /**
     * Validates input and starts the login process.
     */
    private void attemptLogin(AppRouter router,
                              AuthService authService,
                              JwtStore jwtStore,
                              TextField emailField,
                              PasswordField passwordField,
                              Label errorLabel,
                              Button loginButton) {

        hideError(errorLabel);

        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showError(errorLabel, t("login.error.empty_fields", "Please fill all fields"));
            return;
        }

        loginButton.setDisable(true);

        Thread loginThread = new Thread(() -> {
            try {
                AuthState state = authService.login(email, password);

                Platform.runLater(() -> {
                    jwtStore.save(state);
                    router.go(RoleRedirect.routeFor(state.getRole()));
                });

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    showError(
                            errorLabel,
                            t("login.error.failed", "Login interrupted.")
                    );
                    loginButton.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showError(
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

    /**
     * Shows an error message.
     */
    private static void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Hides the error message.
     */
    private static void hideError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Cleans long or empty exception messages before displaying them in UI.
     */
    private static String cleanMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown error";
        }
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }

    /**
     * Returns a translated value, or a fallback if the key is missing.
     */
    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }
}
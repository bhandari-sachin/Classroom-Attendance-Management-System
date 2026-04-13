package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.auth.RoleRedirect;
import frontend.i18n.FrontendI18n;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Signup page for creating a new user account.
 *
 * <p>This page allows the user to:
 * enter personal details,
 * select a role,
 * optionally provide a student code,
 * create an account,
 * and navigate back to login.</p>
 */
public class SignupPage extends StackPane {

    private static final double CARD_MAX_WIDTH = 420;
    private static final int CARD_PADDING = 22;
    private static final int CARD_SPACING = 10;
    private static final int FIELD_SPACING = 6;

    public SignupPage(AppRouter router, AuthService authService, JwtStore jwtStore) {
        validateAutoLogin(router, jwtStore);

        Label titleLabel = createTitleLabel();
        Label subtitleLabel = createSubtitleLabel();

        TextField firstNameField = createTextField("signup.firstname.placeholder", "First name");
        TextField lastNameField = createTextField("signup.lastname.placeholder", "Last name");
        TextField emailField = createTextField("signup.email.placeholder", "Email");
        PasswordField passwordField = createPasswordField();

        ComboBox<Role> roleComboBox = createRoleComboBox();

        Label studentCodeLabel = createFieldLabel(t("signup.studentcode.label", "Student Code"));
        TextField studentCodeField = createTextField("signup.studentcode.placeholder", "Enter student code");
        bindStudentCodeVisibility(roleComboBox, studentCodeLabel, studentCodeField);

        Label errorLabel = createMessageLabel("error");
        Label infoLabel = createMessageLabel("subtitle");

        Button signupButton = createSignupButton(
                router,
                authService,
                firstNameField,
                lastNameField,
                emailField,
                passwordField,
                roleComboBox,
                studentCodeField,
                errorLabel,
                infoLabel
        );

        Button loginButton = createLoginButton(router);

        VBox card = createCard(
                titleLabel,
                subtitleLabel,
                field(t("signup.firstname.label", "First Name"), firstNameField),
                field(t("signup.lastname.label", "Last Name"), lastNameField),
                field(t("signup.email.label", "Email"), emailField),
                field(t("signup.password.label", "Password"), passwordField),
                field(t("signup.role.label", "Role"), roleComboBox),
                studentCodeLabel,
                studentCodeField,
                errorLabel,
                infoLabel,
                signupButton,
                loginButton
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
        Label title = new Label(t("signup.title", "Create Account"));
        title.getStyleClass().add("title");
        return title;
    }

    /**
     * Creates the page subtitle label.
     */
    private Label createSubtitleLabel() {
        Label subtitle = new Label(t("signup.subtitle", "Sign up to continue"));
        subtitle.getStyleClass().add("subtitle");
        return subtitle;
    }

    /**
     * Creates a standard text field with translated placeholder text.
     */
    private TextField createTextField(String placeholderKey, String fallbackPlaceholder) {
        TextField textField = new TextField();
        textField.setPromptText(t(placeholderKey, fallbackPlaceholder));
        return textField;
    }

    /**
     * Creates the password field.
     */
    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(t("signup.password.placeholder", "Password"));
        return passwordField;
    }

    /**
     * Creates a styled field label.
     */
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    /**
     * Creates a styled message label.
     */
    private Label createMessageLabel(String styleClass) {
        Label label = new Label();
        label.getStyleClass().add(styleClass);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    /**
     * Creates the role selection combo box.
     */
    private ComboBox<Role> createRoleComboBox() {
        ComboBox<Role> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Role.STUDENT, Role.TEACHER);
        roleComboBox.setValue(Role.STUDENT);
        roleComboBox.setMaxWidth(Double.MAX_VALUE);

        roleComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : roleLabel(item));
            }
        });

        roleComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : roleLabel(item));
            }
        });

        return roleComboBox;
    }

    /**
     * Binds student code visibility so it only appears for students.
     */
    private void bindStudentCodeVisibility(ComboBox<Role> roleComboBox, Label studentCodeLabel, TextField studentCodeField) {
        studentCodeLabel.visibleProperty().bind(roleComboBox.valueProperty().isEqualTo(Role.STUDENT));
        studentCodeField.visibleProperty().bind(roleComboBox.valueProperty().isEqualTo(Role.STUDENT));

        studentCodeLabel.managedProperty().bind(studentCodeLabel.visibleProperty());
        studentCodeField.managedProperty().bind(studentCodeField.visibleProperty());
    }

    /**
     * Creates the signup button and binds the signup action.
     */
    private Button createSignupButton(AppRouter router,
                                      AuthService authService,
                                      TextField firstNameField,
                                      TextField lastNameField,
                                      TextField emailField,
                                      PasswordField passwordField,
                                      ComboBox<Role> roleComboBox,
                                      TextField studentCodeField,
                                      Label errorLabel,
                                      Label infoLabel) {

        Button signupButton = new Button(t("signup.button.submit", "Sign Up"));
        signupButton.getStyleClass().add("primary-btn");
        signupButton.setMaxWidth(Double.MAX_VALUE);

        signupButton.setOnAction(event -> attemptSignup(
                router,
                authService,
                firstNameField,
                lastNameField,
                emailField,
                passwordField,
                roleComboBox,
                studentCodeField,
                errorLabel,
                infoLabel,
                signupButton
        ));

        return signupButton;
    }

    /**
     * Creates the login navigation button.
     */
    private Button createLoginButton(AppRouter router) {
        Button loginButton = new Button(t("signup.button.login", "Back to Login"));
        loginButton.getStyleClass().add("link-button");
        loginButton.setOnAction(event -> router.go("login"));
        return loginButton;
    }

    /**
     * Creates the main card container.
     */
    private VBox createCard(javafx.scene.Node... children) {
        VBox card = new VBox(CARD_SPACING);
        card.getStyleClass().add("card");
        card.setMaxWidth(CARD_MAX_WIDTH);
        card.setPadding(new Insets(CARD_PADDING));
        card.getChildren().addAll(children);
        return card;
    }

    /**
     * Handles signup validation and backend request.
     */
    private void attemptSignup(AppRouter router,
                               AuthService authService,
                               TextField firstNameField,
                               TextField lastNameField,
                               TextField emailField,
                               PasswordField passwordField,
                               ComboBox<Role> roleComboBox,
                               TextField studentCodeField,
                               Label errorLabel,
                               Label infoLabel,
                               Button signupButton) {

        hideMessage(errorLabel);
        hideMessage(infoLabel);

        String firstName = safeTrim(firstNameField.getText());
        String lastName = safeTrim(lastNameField.getText());
        String email = safeTrim(emailField.getText());
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        Role role = roleComboBox.getValue();
        String studentCode = safeTrim(studentCodeField.getText());

        String validationError = validateSignupInput(firstName, lastName, email, password, role, studentCode);
        if (validationError != null) {
            showMessage(errorLabel, validationError);
            return;
        }

        signupButton.setDisable(true);

        Thread signupThread = new Thread(() -> {
            try {
                authService.signup(
                        firstName,
                        lastName,
                        email,
                        password,
                        role,
                        role == Role.STUDENT ? studentCode : null
                );

                Platform.runLater(() -> {
                    signupButton.setDisable(false);
                    showMessage(infoLabel, t("signup.success.created", "Account created successfully."));
                    router.go("login");
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    signupButton.setDisable(false);
                    showMessage(
                            errorLabel,
                            t("signup.error.failed", "Signup failed:") + " " + cleanMessage(ex.getMessage())
                    );
                });
            }
        });

        signupThread.setName("signup-thread");
        signupThread.setDaemon(true);
        signupThread.start();
    }

    /**
     * Validates signup form input and returns translated error message if invalid.
     */
    private String validateSignupInput(String firstName,
                                       String lastName,
                                       String email,
                                       String password,
                                       Role role,
                                       String studentCode) {

        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            return t("signup.error.required_fields", "Please fill in all required fields.");
        }

        if (!email.contains("@")) {
            return t("signup.error.invalid_email", "Please enter a valid email address.");
        }

        if (password.length() < 6) {
            return t("signup.error.password_short", "Password must be at least 6 characters.");
        }

        if (role == Role.STUDENT && studentCode.isBlank()) {
            return t("signup.error.student_code_required", "Student code is required.");
        }

        return null;
    }

    /**
     * Creates a labeled field block.
     */
    private static VBox field(String labelText, Control input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(FIELD_SPACING, label, input);
    }

    /**
     * Returns the translated label for a role.
     */
    private String roleLabel(Role role) {
        return switch (role) {
            case STUDENT -> t("signup.role.student", "Student");
            case TEACHER -> t("signup.role.teacher", "Teacher");
            case ADMIN -> t("signup.role.admin", "Admin");
        };
    }

    /**
     * Shows a message label.
     */
    private static void showMessage(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    /**
     * Hides a message label.
     */
    private static void hideMessage(Label label) {
        label.setVisible(false);
        label.setManaged(false);
        label.setText("");
    }

    /**
     * Safely trims text input.
     */
    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
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
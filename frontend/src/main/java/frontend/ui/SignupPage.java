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

import java.io.IOException;
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
    private static final int MAX_ERROR_MESSAGE_LENGTH = 200;

    public SignupPage(AppRouter router, AuthService authService, JwtStore jwtStore) {
        validateAutoLogin(router, jwtStore);

        Label titleLabel = createTitleLabel();
        Label subtitleLabel = createSubtitleLabel();

        SignupFormFields fields = createSignupFormFields();
        SignupMessages messages = createSignupMessages();

        bindStudentCodeVisibility(fields.roleComboBox, fields.studentCodeLabel, fields.studentCodeField);

        Button signupButton = createSignupButton(router, authService, fields, messages);
        Button loginButton = createLoginButton(router);

        VBox card = createCard(
                titleLabel,
                subtitleLabel,
                field(t("signup.firstname.label", "First Name"), fields.firstNameField),
                field(t("signup.lastname.label", "Last Name"), fields.lastNameField),
                field(t("signup.email.label", "Email"), fields.emailField),
                field(t("signup.password.label", "Password"), fields.passwordField),
                field(t("signup.role.label", "Role"), fields.roleComboBox),
                fields.studentCodeLabel,
                fields.studentCodeField,
                messages.errorLabel,
                messages.infoLabel,
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
     * Creates all signup form fields.
     */
    private SignupFormFields createSignupFormFields() {
        TextField firstNameField = createTextField("signup.firstname.placeholder", "First name");
        TextField lastNameField = createTextField("signup.lastname.placeholder", "Last name");
        TextField emailField = createTextField("signup.email.placeholder", "Email");
        PasswordField passwordField = createPasswordField();
        ComboBox<Role> roleComboBox = createRoleComboBox();
        Label studentCodeLabel = createFieldLabel(t("signup.studentcode.label", "Student Code"));
        TextField studentCodeField = createTextField("signup.studentcode.placeholder", "Enter student code");

        return new SignupFormFields(
                firstNameField,
                lastNameField,
                emailField,
                passwordField,
                roleComboBox,
                studentCodeLabel,
                studentCodeField
        );
    }

    /**
     * Creates all message labels.
     */
    private SignupMessages createSignupMessages() {
        return new SignupMessages(
                createMessageLabel("error"),
                createMessageLabel("subtitle")
        );
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
                                      SignupFormFields fields,
                                      SignupMessages messages) {

        Button signupButton = new Button(t("signup.button.submit", "Sign Up"));
        signupButton.getStyleClass().add("primary-btn");
        signupButton.setMaxWidth(Double.MAX_VALUE);

        signupButton.setOnAction(event -> attemptSignup(
                router,
                authService,
                fields,
                messages,
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
                               SignupFormFields fields,
                               SignupMessages messages,
                               Button signupButton) {

        hideMessage(messages.errorLabel);
        hideMessage(messages.infoLabel);

        SignupData signupData = extractSignupData(fields);

        String validationError = validateSignupInput(
                signupData.firstName(),
                signupData.lastName(),
                signupData.email(),
                signupData.password(),
                signupData.role(),
                signupData.studentCode()
        );

        if (validationError != null) {
            showMessage(messages.errorLabel, validationError);
            return;
        }

        signupButton.setDisable(true);

        Thread signupThread = new Thread(() -> performSignup(
                router,
                authService,
                signupData,
                messages,
                signupButton
        ));

        signupThread.setName("signup-thread");
        signupThread.setDaemon(true);
        signupThread.start();
    }

    /**
     * Performs the signup request in a background thread.
     */
    private void performSignup(AppRouter router,
                               AuthService authService,
                               SignupData signupData,
                               SignupMessages messages,
                               Button signupButton) {
        try {
            authService.signup(
                    signupData.firstName(),
                    signupData.lastName(),
                    signupData.email(),
                    signupData.password(),
                    signupData.role(),
                    signupData.role() == Role.STUDENT ? signupData.studentCode() : null
            );

            Platform.runLater(() -> {
                signupButton.setDisable(false);
                showMessage(messages.infoLabel, t("signup.success.created", "Account created successfully."));
                router.go("login");
            });

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> {
                signupButton.setDisable(false);
                showMessage(
                        messages.errorLabel,
                        t("signup.error.failed", "Signup failed:") + " " + t("signup.error.interrupted", "Operation was interrupted.")
                );
            });
        } catch (IOException | RuntimeException ex) {
            Platform.runLater(() -> {
                signupButton.setDisable(false);
                showMessage(
                        messages.errorLabel,
                        t("signup.error.failed", "Signup failed:") + " " + cleanMessage(ex.getMessage())
                );
            });
        }
    }

    /**
     * Extracts and normalizes signup input values.
     */
    private SignupData extractSignupData(SignupFormFields fields) {
        String firstName = safeTrim(fields.firstNameField.getText());
        String lastName = safeTrim(fields.lastNameField.getText());
        String email = safeTrim(fields.emailField.getText());
        String password = fields.passwordField.getText() == null ? "" : fields.passwordField.getText();
        Role role = fields.roleComboBox.getValue();
        String studentCode = safeTrim(fields.studentCodeField.getText());

        return new SignupData(firstName, lastName, email, password, role, studentCode);
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
        return message.length() > MAX_ERROR_MESSAGE_LENGTH
                ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "..."
                : message;
    }

    /**
     * Returns a translated value, or a fallback if the key is missing.
     */
    private static String t(String key, String fallback) {
        String value = FrontendI18n.t(key);
        return key.equals(value) ? fallback : value;
    }

    /**
     * Holder for all signup form controls.
     */
    private static final class SignupFormFields {
        private final TextField firstNameField;
        private final TextField lastNameField;
        private final TextField emailField;
        private final PasswordField passwordField;
        private final ComboBox<Role> roleComboBox;
        private final Label studentCodeLabel;
        private final TextField studentCodeField;

        private SignupFormFields(TextField firstNameField,
                                 TextField lastNameField,
                                 TextField emailField,
                                 PasswordField passwordField,
                                 ComboBox<Role> roleComboBox,
                                 Label studentCodeLabel,
                                 TextField studentCodeField) {
            this.firstNameField = firstNameField;
            this.lastNameField = lastNameField;
            this.emailField = emailField;
            this.passwordField = passwordField;
            this.roleComboBox = roleComboBox;
            this.studentCodeLabel = studentCodeLabel;
            this.studentCodeField = studentCodeField;
        }
    }

    /**
     * Holder for page message labels.
     */
    private static final class SignupMessages {
        private final Label errorLabel;
        private final Label infoLabel;

        private SignupMessages(Label errorLabel, Label infoLabel) {
            this.errorLabel = errorLabel;
            this.infoLabel = infoLabel;
        }
    }

    /**
     * Signup input data.
     */
    private record SignupData(String firstName,
                              String lastName,
                              String email,
                              String password,
                              Role role,
                              String studentCode) {
    }
}
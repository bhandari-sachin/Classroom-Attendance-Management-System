package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Signup page for creating a new user account.
 *
 * <p>This page allows the user to:
 * enter personal details,
 * select a role,
 * optionally provide a student code,
 * create an account,
 * and navigate back to log in.</p>
 */
public class SignupPage extends BaseAuthPage {

    private static final int FIELD_SPACING = 6;

    public SignupPage(AppRouter router, AuthService authService, JwtStore jwtStore) {
        validateAutoLogin(router, jwtStore);

        Label titleLabel = createTitleLabel("signup.title", "Create Account");
        Label subtitleLabel = createSubtitleLabel("signup.subtitle", "Sign up to continue");

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

        showCenteredCard(card);
    }

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

    private SignupMessages createSignupMessages() {
        return new SignupMessages(
                createMessageLabel("error"),
                createMessageLabel("subtitle")
        );
    }

    private TextField createTextField(String placeholderKey, String fallbackPlaceholder) {
        TextField textField = new TextField();
        textField.setPromptText(t(placeholderKey, fallbackPlaceholder));
        return textField;
    }

    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(t("signup.password.placeholder", "Password"));
        return passwordField;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

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

    private void bindStudentCodeVisibility(ComboBox<Role> roleComboBox, Label studentCodeLabel, TextField studentCodeField) {
        studentCodeLabel.visibleProperty().bind(roleComboBox.valueProperty().isEqualTo(Role.STUDENT));
        studentCodeField.visibleProperty().bind(roleComboBox.valueProperty().isEqualTo(Role.STUDENT));

        studentCodeLabel.managedProperty().bind(studentCodeLabel.visibleProperty());
        studentCodeField.managedProperty().bind(studentCodeField.visibleProperty());
    }

    private Button createSignupButton(AppRouter router,
                                      AuthService authService,
                                      SignupFormFields fields,
                                      SignupMessages messages) {

        Button signupButton = createPrimaryButton("signup.button.submit", "Sign Up");

        signupButton.setOnAction(event -> attemptSignup(
                router,
                authService,
                fields,
                messages,
                signupButton
        ));

        return signupButton;
    }

    private Button createLoginButton(AppRouter router) {
        Button loginButton = createLinkButton("signup.button.login", "Back to Login");
        loginButton.setOnAction(event -> router.go("login"));
        return loginButton;
    }

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

    private SignupData extractSignupData(SignupFormFields fields) {
        String firstName = safeTrim(fields.firstNameField.getText());
        String lastName = safeTrim(fields.lastNameField.getText());
        String email = safeTrim(fields.emailField.getText());
        String password = fields.passwordField.getText() == null ? "" : fields.passwordField.getText();
        Role role = fields.roleComboBox.getValue();
        String studentCode = safeTrim(fields.studentCodeField.getText());

        return new SignupData(firstName, lastName, email, password, role, studentCode);
    }

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

    private static VBox field(String labelText, Control input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        VBox box = new VBox(FIELD_SPACING, label, input);
        box.setPadding(new Insets(0));
        return box;
    }

    private String roleLabel(Role role) {
        return switch (role) {
            case STUDENT -> t("signup.role.student", "Student");
            case TEACHER -> t("signup.role.teacher", "Teacher");
            case ADMIN -> t("signup.role.admin", "Admin");
        };
    }

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

    private static final class SignupMessages {
        private final Label errorLabel;
        private final Label infoLabel;

        private SignupMessages(Label errorLabel, Label infoLabel) {
            this.errorLabel = errorLabel;
            this.infoLabel = infoLabel;
        }
    }

    private record SignupData(String firstName,
                              String lastName,
                              String email,
                              String password,
                              Role role,
                              String studentCode) {
    }
}
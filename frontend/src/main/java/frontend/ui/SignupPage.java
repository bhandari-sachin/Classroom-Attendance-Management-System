package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.Role;
import frontend.auth.RoleRedirect;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.I18n;

import java.util.Optional;

public class SignupPage extends StackPane {

    public SignupPage(AppRouter router, AuthService authService, JwtStore jwtStore) {

        setPadding(new Insets(24));
        setNodeOrientation(I18n.isRtl() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        VBox root = new VBox(16);
        root.setFillWidth(true);
        root.setMaxWidth(520);

        // ===== top row with language switch =====
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> languageSwitch = new ComboBox<>();
        languageSwitch.getItems().addAll("English", "العربية");
        languageSwitch.setValue(I18n.isArabic() ? "العربية" : "English");
        languageSwitch.setPrefWidth(130);
        languageSwitch.getStyleClass().add("lang-switch");

        languageSwitch.setOnAction(e -> {
            String selected = languageSwitch.getValue();

            if ("العربية".equals(selected)) {
                I18n.setArabic();
            } else {
                I18n.setEnglish();
            }

            router.refresh();
        });

        topRow.getChildren().addAll(spacer, languageSwitch);

        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(420);
        card.setPadding(new Insets(22));
        card.setNodeOrientation(I18n.isRtl() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label title = new Label(I18n.t("signup.title"));
        title.getStyleClass().add("title");

        Label sub = new Label(I18n.t("signup.subtitle"));
        sub.getStyleClass().add("subtitle");

        TextField firstName = new TextField();
        firstName.setPromptText(I18n.t("signup.firstname.placeholder"));

        TextField lastName = new TextField();
        lastName.setPromptText(I18n.t("signup.lastname.placeholder"));

        TextField email = new TextField();
        email.setPromptText(I18n.t("signup.email.placeholder"));

        PasswordField password = new PasswordField();
        password.setPromptText(I18n.t("signup.password.placeholder"));

        ComboBox<Role> role = new ComboBox<>();
        role.getItems().addAll(Role.STUDENT, Role.TEACHER);
        role.setValue(Role.STUDENT);
        role.setMaxWidth(Double.MAX_VALUE);

        role.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(localizeRole(item));
                }
            }
        });

        role.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(localizeRole(item));
                }
            }
        });

        Label studentCodeLabel = new Label(I18n.t("signup.studentcode.label"));
        studentCodeLabel.getStyleClass().add("field-label");

        TextField studentCode = new TextField();
        studentCode.setPromptText(I18n.t("signup.studentcode.placeholder"));

        // show only for students
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

        Button signupBtn = new Button(I18n.t("signup.button.submit"));
        signupBtn.getStyleClass().add("primary-btn");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        Button goLogin = new Button(I18n.t("signup.button.login"));
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
                showMsg(error, I18n.t("signup.error.required_fields"));
                return;
            }
            if (!em.contains("@")) {
                showMsg(error, I18n.t("signup.error.invalid_email"));
                return;
            }
            if (pw.length() < 6) {
                showMsg(error, I18n.t("signup.error.password_short"));
                return;
            }
            if (r == Role.STUDENT && sc.isBlank()) {
                showMsg(error, I18n.t("signup.error.student_code_required"));
                return;
            }

            signupBtn.setDisable(true);

            new Thread(() -> {
                try {
                    authService.signup(fn, ln, em, pw, r, (r == Role.STUDENT ? sc : null));

                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        showMsg(info, I18n.t("signup.success.created"));
                        router.go("login");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        signupBtn.setDisable(false);
                        String msg = ex.getMessage() == null || ex.getMessage().isBlank()
                                ? I18n.t("signup.error.failed")
                                : I18n.t("signup.error.failed") + ": " + ex.getMessage();
                        showMsg(error, msg);
                    });
                }
            }).start();
        });

        card.getChildren().addAll(
                title,
                sub,
                field(I18n.t("signup.firstname.label"), firstName),
                field(I18n.t("signup.lastname.label"), lastName),
                field(I18n.t("signup.email.label"), email),
                field(I18n.t("signup.password.label"), password),
                field(I18n.t("signup.role.label"), role),
                studentCodeLabel,
                studentCode,
                error,
                info,
                signupBtn,
                goLogin
        );

        root.getChildren().addAll(topRow, card);

        StackPane.setAlignment(root, Pos.CENTER);
        getChildren().add(root);

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

    private static String localizeRole(Role role) {
        if (role == null) return "";
        return switch (role) {
            case STUDENT -> I18n.t("signup.role.student");
            case TEACHER -> I18n.t("signup.role.teacher");
            default -> role.name();
        };
    }
}
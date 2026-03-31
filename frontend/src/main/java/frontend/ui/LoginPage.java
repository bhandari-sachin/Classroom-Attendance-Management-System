package frontend.ui;

import frontend.auth.AppRouter;
import frontend.auth.AuthService;
import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.auth.RoleRedirect;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

public class LoginPage extends StackPane {

    public LoginPage(AppRouter router, AuthService authService, JwtStore jwtStore) {

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

        VBox card = new VBox(12);
        card.setMaxWidth(420);
        card.setPadding(new Insets(22));
        card.getStyleClass().add("card");
        card.setNodeOrientation(I18n.isRtl() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);

        Label title = new Label(I18n.t("auth.login.title"));
        title.getStyleClass().add("title");

        Label sub = new Label(I18n.t("auth.login.subtitle"));
        sub.getStyleClass().add("subtitle");

        TextField email = new TextField();
        email.setPromptText(I18n.t("login.email.placeholder"));

        PasswordField password = new PasswordField();
        password.setPromptText(I18n.t("login.password.placeholder"));

        Label error = new Label();
        error.getStyleClass().add("error");
        error.setManaged(false);
        error.setVisible(false);

        Button loginBtn = new Button(I18n.t("login.button.submit"));
        loginBtn.getStyleClass().add("primary-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            error.setVisible(false);
            error.setManaged(false);

            String em = email.getText().trim();
            String pw = password.getText();

            if (em.isBlank() || pw.isBlank()) {
                showError(error, I18n.t("login.error.empty_fields"));
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
                        showError(error, I18n.t("login.error.failed") + ": " + cleanMessage(ex.getMessage()));
                        loginBtn.setDisable(false);
                    });
                }
            }).start();
        });

        Button goSignup = new Button(I18n.t("login.button.signup"));
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

        root.getChildren().addAll(topRow, card);

        StackPane.setAlignment(root, Pos.CENTER);
        getChildren().add(root);

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
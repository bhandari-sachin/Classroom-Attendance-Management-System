package frontend.ui;

import frontend.auth.*;
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
            jwtStore.save(new AuthState("mock-token-admin", Role.ADMIN, "Admin User"));
            router.go(RoleRedirect.routeFor(Role.ADMIN));
        });

        devTeacher.setOnAction(e -> {
            jwtStore.save(new AuthState("mock-token-teacher", Role.TEACHER, "Teacher User"));
            router.go(RoleRedirect.routeFor(Role.TEACHER));
        });

        devStudent.setOnAction(e -> {
            jwtStore.save(new AuthState("mock-token-student", Role.STUDENT, "Student User"));
            router.go(RoleRedirect.routeFor(Role.STUDENT));
        });

        VBox devBox = new VBox(8, devLabel, devAdmin, devTeacher, devStudent);
        devBox.setPadding(new Insets(10, 0, 0, 0));
        Button goSignup = new Button("I don't have an account");
        goSignup.getStyleClass().add("link-button");

        goSignup.setOnAction(e -> router.go("signup"));
        // -- //

        card.getChildren().addAll(title, sub, email, password, error, loginBtn, goSignup, devBox);

        StackPane.setAlignment(card, Pos.CENTER);
        getChildren().add(card);

        // auto redirect if already logged in
        Optional<AuthState> existing = jwtStore.load();
        existing.ifPresent(state -> router.go(RoleRedirect.routeFor(state.getRole())));
    }
}
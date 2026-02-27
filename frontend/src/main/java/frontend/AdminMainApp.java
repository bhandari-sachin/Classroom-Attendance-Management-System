package frontend;

import config.UserSQL;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import service.UserService;

import java.util.List;

public class AdminMainApp extends Application {

    @Override
    public void start(Stage stage) {

        UserService userService = new UserService(new UserSQL());
        Long userId = 1L;
        String adminName = "Name";
        try {
            List<User> all = userService.getAllUsers();
            if (all != null) {
                for (User u : all) {
                    if (u.getId() != null && u.getId().equals(userId)) {
                        String first = u.getFirstName() == null ? "" : u.getFirstName();
                        String last = u.getLastName() == null ? "" : u.getLastName();
                        String combined = (first + " " + last).trim();
                        if (!combined.isEmpty()) adminName = combined;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize admin info: " + e.getMessage());
        }

        // Create the scene first, then pass it into build(...)
        Scene scene = new Scene(new javafx.scene.layout.StackPane(), 1100, 700);
        scene.setRoot(new AdminDashboardApp().build(scene, adminName));

        // Load CSS if present
        var css = getClass().getResource("/admin-dashboard.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Frontend");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

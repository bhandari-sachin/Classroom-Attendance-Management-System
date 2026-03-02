package frontend;

import config.UserSQL;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import service.UserService;

import java.util.List;

public class StudentMainApp extends Application {

    @Override
    public void start(Stage stage) {
        // get from login in future
        UserService userService = new UserService(new UserSQL());
        Long studentId = 4L;
        String studentName = "Name";
        try {
            List<User> all = userService.getAllUsers();
            if (all != null) {
                for (User u : all) {
                    if (u.getId() != null && u.getId().equals(studentId)) {
                        String first = u.getFirstName() == null ? "" : u.getFirstName();
                        String last = u.getLastName() == null ? "" : u.getLastName();
                        String combined = (first + " " + last).trim();
                        if (!combined.isEmpty()) studentName = combined;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize session: " + e.getMessage());
        }

        // Create the scene first, then pass it into build(...)
        Scene scene = new Scene(new javafx.scene.layout.StackPane(), 1100, 700);
        scene.setRoot(new StudentDashboardApp().build(scene, studentName, studentId));

        // Load CSS if present
        var css = getClass().getResource("/dashboard.css");
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

package frontend;

import config.UserSQL;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;
import service.UserService;

import java.util.List;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // load from auth in future
        UserService userService = new UserService(new UserSQL());
        Long teacherId = 2L;
        String teacherName = "Name";
        try {
            List<User> all = userService.getAllUsers();
            if (all != null) {
                for (User u : all) {
                    if (u.getId() != null && u.getId().equals(teacherId)) {
                        String first = u.getFirstName() == null ? "" : u.getFirstName();
                        String last = u.getLastName() == null ? "" : u.getLastName();
                        String combined = (first + " " + last).trim();
                        if (!combined.isEmpty()) teacherName = combined;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize teacher info: " + e.getMessage());
        }

        // start with an empty root first
        Scene scene = new Scene(new javafx.scene.layout.StackPane(), 1100, 700);

        // load your app stylesheet
        scene.getStylesheets().add(
                getClass().getResource("/app.css").toExternalForm()
        );

        // set dashboard as first page
        scene.setRoot(new TeacherDashboardApp().build(scene, teacherName, teacherId));

        stage.setTitle("Teacher Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }
}

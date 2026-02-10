package frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminMainApp extends Application {

    @Override
    public void start(Stage stage) {
        String studentName = "Name"; // later from login

        // Create the scene first, then pass it into build(...)
        Scene scene = new Scene(new javafx.scene.layout.StackPane(), 1100, 700);
        scene.setRoot(new StudentDashboardApp().build(scene, studentName));

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

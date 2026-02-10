package frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        String teacherName = "Name"; // later from login/backend

        // start with an empty root first
        Scene scene = new Scene(new javafx.scene.layout.StackPane(), 1100, 700);

        // load your app stylesheet
        scene.getStylesheets().add(
                getClass().getResource("/app.css").toExternalForm()
        );

        // set dashboard as first page
        scene.setRoot(new TeacherDashboardApp().build(scene, teacherName));

        stage.setTitle("Teacher Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }
}

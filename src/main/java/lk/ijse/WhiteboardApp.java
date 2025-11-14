package lk.ijse;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class WhiteboardApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/WhiteDboardView.fxml"));
        Parent root = loader.load();

        // Get the controller instance
        WhiteboardController controller = loader.getController();

        // Set up the scene and stage
        primaryStage.setTitle("Collaborative Whiteboard Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // Add a shutdown hook to close network connections
        primaryStage.setOnCloseRequest(e -> {
            controller.shutdown();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

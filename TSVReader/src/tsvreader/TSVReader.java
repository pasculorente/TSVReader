package tsvreader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author uichuimi03
 */
public class TSVReader extends Application {

    private static Stage mainWindow;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));

        root.getStylesheets().add("tsvreader/default.css");
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
        stage.setTitle("TSV Reader");
        mainWindow = stage;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getMainWindow() {
        return mainWindow;
    }

    public static void setTitle(String title) {
        mainWindow.setTitle("TSV Reader - " + title);
    }

}

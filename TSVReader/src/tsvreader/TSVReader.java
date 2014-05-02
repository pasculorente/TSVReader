package tsvreader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main class of the application. Loads the main window and keeps a pointer to the Stage.
 * Furthermore, it has a static method to change the title of the window.
 *
 * @author Pascual Lorente Arencibia
 */
public class TSVReader extends Application {

    /**
     * The main window of the application.
     */
    private static Stage mainWindow;

    /**
     * This methods should be called instead of main. it loads the main window and shows it. Here is
     * where the CSS and the Locale are applied.
     *
     * @param stage The main window.
     * @throws Exception
     */
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
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as
     * fallback in case the application can not be launched through deployment artifacts, e.g., in
     * IDEs with limited FX support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Gets the main window.
     *
     * @return the main window of the TSVReader.
     */
    public static Stage getMainWindow() {
        return mainWindow;
    }

    /**
     * Changes the title of the application.
     * <pre>TSVReader - title</pre>.
     *
     * @param title the new title.
     */
    public static void setTitle(String title) {
        mainWindow.setTitle("TSV Reader - " + title);
    }

}

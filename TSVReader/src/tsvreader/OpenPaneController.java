package tsvreader;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class OpenPaneController {

    @FXML
    private TextField file;

    private Scene scene;

    private boolean cancelled = true;

    @FXML
    private ComboBox<String> types;
    private final String[] file_types = {
        "annovar", "sift"
    };

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        File tsv = new File("tsv_files");
        if (tsv.exists()) {
            for (File f : tsv.listFiles((File pathname) -> pathname.getName().endsWith(".header"))) {
                types.getItems().add(f.getName().replace(".header", ""));
            }
        }
//        types.getItems().setAll(file_types);
    }

    String getFile() {
        return file.getText();
    }

    String getType() {
        return types.getValue();
    }

    @FXML
    private void openFile() {
        File f = OS.openTSV(scene.getWindow());
        if (f != null) {
            file.setText(f.getAbsolutePath());
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @FXML
    public void cancel() {
        cancelled = true;
        scene.getWindow().hide();
    }

    @FXML
    public void accept() {
        if (!file.getText().isEmpty() && types.getValue() != null) {
            cancelled = false;
            scene.getWindow().hide();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

}

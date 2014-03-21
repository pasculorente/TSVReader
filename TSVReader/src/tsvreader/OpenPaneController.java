/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tsvreader;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for the Open File window. This window shows a TextField to select a File
 * and a ComboBox to select the file type. File types are loaded from files .header in the
 * tsv_files/ folder.
 *
 * @author Pascual Lorente Arencibia
 */
public class OpenPaneController {

    /**
     * The TextField file.
     */
    @FXML
    private TextField file;

    /**
     * The window to block. Probably TSVReader window.
     */
    private Scene scene;

    /**
     * True if the user canceled the open action, or didn't select a file and a type.
     */
    private boolean cancelled = true;

    /**
     * A ComboBox with all the available types.
     */
    @FXML
    private ComboBox<String> types;

    /**
     * Initializes the controller class. This method is called automatically when the windows is
     * loaded. It fills the types ComboBox with all the files that matches *.header pattern in
     * tsv_files directory.
     */
    @FXML
    public void initialize() {
        File tsv = new File("tsv_files");
        if (tsv.exists()) {
            for (File f : tsv.listFiles((File pathname) -> pathname.getName().endsWith(".header"))) {
                types.getItems().add(f.getName().replace(".header", ""));
            }
        }
    }

    /**
     * Gets the selected file.
     *
     * @return the selected file.
     */
    String getFile() {
        return file.getText();
    }

    /**
     * Gets the file type.
     *
     * @return the selected file type.
     */
    String getType() {
        return types.getValue();
    }

    /**
     * Opens a file selection pane, and waits until the users selects a file or cancels the action.
     */
    @FXML
    private void openFile() {
        File f = OS.openTSV(scene.getWindow());
        if (f != null) {
            file.setText(f.getAbsolutePath());
        }
    }

    /**
     * Sets the scene to block when this window is open.
     *
     * @param scene The scene to block.
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Method called when the user cancels the window.
     */
    @FXML
    public void cancel() {
        cancelled = true;
        scene.getWindow().hide();
    }

    /**
     * Method called when the user accepts the window.
     */
    @FXML
    public void accept() {
        if (!file.getText().isEmpty() && types.getValue() != null) {
            cancelled = false;
            scene.getWindow().hide();
        }
    }

    /**
     * If the user canceled this window or didn't select a file and a type.
     *
     * @return the canceled property.
     */
    public boolean isCancelled() {
        return cancelled;
    }

}

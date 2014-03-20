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

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import tsvreader.Dataset.Logic;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class FilterViewController {

    @FXML
    private Label name;
    @FXML
    private ComboBox logic;
    @FXML
    private Button delete;
    @FXML
    private HBox values;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        logic.setItems(FXCollections.observableArrayList(Logic.values()));
        logic.getSelectionModel().selectFirst();
    }

    /**
     * Gets the delete Button.
     *
     * @return the delete Button.
     */
    public Button getDelete() {
        return delete;
    }

    /**
     * Gets the logic ComboBox.
     *
     * @return the logic ComboBox.
     */
    public ComboBox getLogic() {
        return logic;
    }

    /**
     * Gets the name Label.
     *
     * @return the name Label.
     */
    public Label getName() {
        return name;
    }

    /**
     * Gets the values HBox.
     *
     * @return the values HBox.
     */
    public HBox getValues() {
        return values;
    }
}

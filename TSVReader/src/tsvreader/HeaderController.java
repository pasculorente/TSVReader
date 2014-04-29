/*
 * Copyright (C) 2014 uichuimi03
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

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author uichuimi03
 */
public class HeaderController {

    @FXML
    private Label count;
    @FXML
    private Label title;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        // TODO
    }

    public Label getCount() {
        return count;
    }

    public Label getTitle() {
        return title;
    }

    public void setCount(Label count) {
        this.count = count;
    }

    public void setTitle(Label title) {
        this.title = title;
    }

}

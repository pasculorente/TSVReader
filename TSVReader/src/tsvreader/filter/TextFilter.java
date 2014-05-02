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
package tsvreader.filter;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class TextFilter extends Filter {

    final TextField valueTextField;

    public TextFilter(String type, int column, ComboBox logic, CheckBox empty, TextField value) {
        super(type, column, logic, empty);
        this.valueTextField = value;
    }

    public TextField getValue() {
        return valueTextField;
    }

    @Override
    public boolean accept(String value) {
        if (value.isEmpty() && getEmpty().isSelected()) {
            return true;
        }
        switch ((Logic) getLogic().getValue()) {
            case ONLY:
                return value.matches(this.valueTextField.getText());
            case NOT:
                return !value.matches(this.valueTextField.getText());
        }
        return false;
    }
}

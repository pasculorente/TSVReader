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

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import tsvreader.MainViewController;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class NumericFilter extends Filter {

    private final TextField min;
    private final TextField max;
    double maxValue, minValue;

    public NumericFilter(String type, int column, ComboBox logic, CheckBox empty,
            TextField min, TextField max) {
        super(type, column, logic, empty);
        this.min = min;
        this.max = max;
        min.setOnAction((ActionEvent t) -> {
            try {
                minValue = Double.valueOf(min.getText());
            } catch (NumberFormatException ex) {
                MainViewController.printMessage("ERROR: Min is not a number.");
            }
        });
        max.setOnAction((ActionEvent t) -> {
            try {
                maxValue = Double.valueOf(max.getText());
            } catch (NumberFormatException ex) {
                MainViewController.printMessage("ERROR: Max is not a number.");
            }
        });
    }

    public TextField getMax() {
        return max;
    }

    public TextField getMin() {
        return min;
    }

    @Override
    public boolean accept(String value) {
        if (min.getText().isEmpty() || max.getText().isEmpty()) {
            return true;
        }
        if (value.isEmpty() && getEmpty().isSelected()) {
            return true;
        }
        try {
            double val = Double.valueOf(value);
            switch ((Logic) getLogic().getValue()) {
                case ONLY:
                    return minValue <= val && val <= maxValue;
                case NOT:
                    return minValue > val || val > maxValue;
            }
        } catch (NumberFormatException ex) {
            MainViewController.printMessage("WARNING: Some cells didn't contain numeric values.");
        }
        return false;
    }

}

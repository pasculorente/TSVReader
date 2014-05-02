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

/**
 * This is an internal representation of a Filter in the GUI. It stores type (text or numeric), the
 * corresponding column in the table and the GUI elements where the user put the parameters (like
 * TextFileds or ComboBoxes).
 *
 * @author Pascual Lorente Arencibia.
 */
public abstract class Filter {

    public enum Logic {

        /**
         * Inclusive Logic.
         */
        ONLY,
        /**
         * Exclusive Logic.
         */
        NOT
    }

    private final ComboBox logic;
    private final CheckBox empty;
    /**
     * The column in the table that is filtered.
     */
    private final int column;
    /**
     * The type of the column (text or numeric).
     */
    private final String type;

    /**
     * The first node must be the ComboBox of the logic, then the user parameters.
     *
     * @param type the filter type.
     * @param column the column in the table.
     * @param logic
     * @param empty
     */
    public Filter(String type, int column, ComboBox logic, CheckBox empty) {
        this.type = type;
        this.logic = logic;
        this.empty = empty;
        this.column = column;
    }

    /**
     * Gets the column to filter.
     *
     * @return the column to filter.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the type of the column.
     *
     * @return the type of the column.
     */
    public String getType() {
        return type;
    }

    public CheckBox getEmpty() {
        return empty;
    }

    public ComboBox getLogic() {
        return logic;
    }

    public abstract boolean accept(String string);

}

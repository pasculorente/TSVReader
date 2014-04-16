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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A dataset stores the whole data of a TSV file. <strong>headers</strong> attribute stores the
 * column titles, type of column data (text or numeric), original column from file, and title
 * structure. See <em>Header</em> for more information. <strong>rows</strong> is a list with the
 * original data, stored as a List of String[]s.</p>
 * <p>
 * Data can be filtered, using <strong>filterText()</strong> and <strong>filterNumeric()</strong>
 * methods. The result of filtering is stored in the attribute <strong>cachedRows</strong></p>
 * <p>
 * It is possible to create a Dataset using the default constructor, but the best way is using a
 * <em>Parser</em>.</p>
 *
 *
 * @author Pascual Lorente Arencibia
 * @see Header
 * @see Parser
 */
public class Dataset {

    /**
     * Stores columns information.
     */
    private final List<Header> headers;
    /**
     * Stores original data from file.
     */
    private final List<String[]> rows;
    /**
     * Stores filtered data.
     */
    private final List<String[]> cachedRows;

    /**
     * Type of filtering that can be done. ONLY to include rows and NOT to exclude.
     */
    enum Logic {

        /**
         * Inclusive Logic.
         */
        ONLY,
        /**
         * Exclusive Logic.
         */
        NOT
    }

    /**
     * Creates a new Dataset. Be sure that headers and any String[] from row have the same length.
     * The field parent of Header must be the same for consecutive columns, in order to create a
     * unique group column in the GUI.
     *
     * @param headers a list representing file columns.
     * @param rows a list of String[] containing the data of the file.
     */
    Dataset(List<Header> headers, List<String[]> rows) {
        this.headers = headers;
        this.rows = rows;
        this.cachedRows = new ArrayList<>(rows);
    }

    /**
     * Gets the headers.
     *
     * @return the headers.
     */
    List<Header> getHeaders() {
        return headers;
    }

    /**
     * Gets the original rows from file.
     *
     * @return the headers.
     */
    List<String[]> getRows() {
        return rows;
    }

    /**
     * Gets the filtered rows.
     *
     * @return the cachedRows.
     */
    List<String[]> getCachedRows() {
        return cachedRows;
    }

    /**
     * Filters a Text column. Use logic to decide if you want the filter to include(Filter.ONLY) or
     * remove(Filter.NOT) the rows matching value as regex.
     *
     * @param column The column to filter.
     * @param value A regular expression.
     * @param logic Type of filter. Filter.ONLY for addition and Filter.NOT for subtraction.
     * @return the new cachedRows from Dataset.
     */
    List<String[]> filterText(int column, String value, Logic logic, boolean allowEmpty) {
        List<String[]> origin = new ArrayList<>(cachedRows);
        cachedRows.clear();
        if (allowEmpty) {
            switch (logic) {
                case ONLY:
                    origin.stream().forEach((data) -> {
                        if (data[column].isEmpty() || data[column].matches(value)) {
                            cachedRows.add(data);
                        }
                    });
                    break;
                case NOT:
                    origin.stream().forEach((data) -> {
                        if (data[column].isEmpty() || !data[column].matches(value)) {
                            cachedRows.add(data);
                        }
                    });
            }
        } else {
            switch (logic) {
                case ONLY:
                    origin.stream().filter((String[] data) -> data[column].matches(value)).
                            forEach((String[] t) -> {
                        cachedRows.add(t);
                    });
                    break;
                case NOT:
                    origin.parallelStream().forEach((data) -> {
                        if (!data[column].matches(value)) {
                            cachedRows.add(data);
                        }
                    });
            }
        }
        return cachedRows;
    }

    /**
     * Filters a Numeric column. Use logic to decide if you want the filter to include(Filter.ONLY)
     * or remove(Filter.NOT) the rows between min and max.
     *
     * @param column The column to filter.
     * @param min Min value.
     * @param max Max value.
     * @param logic Type of filter. Filter.ONLY for addition and Filter.NOT for subtraction.
     * @return the new cachedRows from Dataset.
     */
    List<String[]> filterNumeric(int column, double min, double max, Logic logic, boolean allowEmpty) {
        List<String[]> origin = new ArrayList<>(cachedRows);
        cachedRows.clear();
        if (allowEmpty) {
            switch (logic) {
                case ONLY:
                    try {
                        origin.stream().forEach((data) -> {
                            if (data[column].isEmpty()) {
                                cachedRows.add(data);
                            } else {
                                String[] values = data[column].split(",");
                                for (String value : values) {
                                    Double val = Double.valueOf(value);
                                    if (min <= val && val <= max) {
                                        cachedRows.add(data);
                                    }
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                    }
                    break;
                case NOT:
                    try {
                        origin.stream().filter((String[] data) -> {
                            String[] values = data[column].split(",");
                            for (String value : values) {
                                Double val = Double.valueOf(value);
                                if (min <= val && val <= max) {
                                    return false;
                                }
                            }
                            return true;
                        }).forEach((String[] t) -> {
                            cachedRows.add(t);
                        });
                    } catch (NumberFormatException ex) {
                    }
            }
        } else {
            switch (logic) {
                case ONLY:
                    try {
                        origin.stream().forEach((data) -> {
                            if (data[column] != null && !data[column].isEmpty()) {
                                String[] values = data[column].split(",");
                                for (String value : values) {
                                    Double val = Double.valueOf(value);
                                    if (min <= val && val <= max) {
                                        cachedRows.add(data);
                                    }
                                }
                            }
                        });
                    } catch (NumberFormatException ex) {
                    }
                    break;
                case NOT:
                    try {
                        origin.stream().filter((String[] data) -> {
                            if (data[column].isEmpty()) {
                                return false;
                            }
                            String[] values = data[column].split(",");
                            for (String value : values) {
                                Double val = Double.valueOf(value);
                                if (min <= val && val <= max) {
                                    return false;
                                }
                            }
                            return true;
                        }).forEach((String[] t) -> {
                            cachedRows.add(t);
                        });
                    } catch (NumberFormatException ex) {
                    }
            }
        }
        return cachedRows;
    }

    /**
     * Resets the filtered variants. After reseting, cachedRows contains the same data than rows.
     */
    void resetVariants() {
        cachedRows.clear();
        cachedRows.addAll(rows);
    }

}

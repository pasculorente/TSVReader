package tsvreader;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Dataset {

    private final List<Header> headers;
    private final List<String[]> rows;
    private final List<String[]> cachedRows;

    public enum Logic {

        ONLY, NOT
    }

    Dataset(List<Header> headers, List<String[]> rows) {
        this.headers = headers;
        this.rows = rows;
        this.cachedRows = new ArrayList<>(rows);
    }

    List<Header> getHeaders() {
        return headers;
    }

    List<String[]> getRows() {
        return rows;
    }

    public List<String[]> getCachedRows() {
        return cachedRows;
    }

    List<String[]> filterText(int column, String value, Logic logic) {
        List<String[]> origin = new ArrayList<>(cachedRows);
        switch (logic) {
            case ONLY:
                cachedRows.clear();
                origin.stream().forEach((data) -> {
                    if (data[column].matches(value)) {
                        cachedRows.add(data);
                    }
                });
                break;
            case NOT:
                rows.stream().forEach((data) -> {
                    if (data[column].matches(value)) {
                        cachedRows.remove(data);
                    }
                });
        }
        return cachedRows;
    }

    /**
     * Filters a Numeric column.
     *
     * @param column The column to filter.
     * @param min Min value.
     * @param max Max value.
     * @param logic Type of filter. Filter.ONLY for intersection, Filter.ADD for sum and Filter.NOT
     * for subtraction.
     * @return
     */
    List<String[]> filterNumeric(int column, double min, double max, Logic logic) {
        List<String[]> origin = new ArrayList<>(cachedRows);
        switch (logic) {
            case ONLY:
                cachedRows.clear();
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
                break;
            case NOT:
                try {
                    rows.stream().forEach((data) -> {
                        if (data[column] != null && !data[column].isEmpty()) {
                            String[] values = data[column].split(",");
                            for (String value : values) {
                                Double val = Double.valueOf(value);
                                if (min <= val && val <= max) {
                                    cachedRows.remove(data);
                                }
                            }
                        }
                    });
                } catch (NumberFormatException ex) {
                }
        }
        // NOT removes data.
        if (logic == Logic.NOT) {

            // ONLY/OR add data.
        } else {
            try {

            } catch (NumberFormatException ex) {
            }
        }
        return cachedRows;
    }

    void resetVariants() {
        cachedRows.clear();
        cachedRows.addAll(rows);
    }

    void save(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write headers
            for (int i = 0; i < headers.size() - 1; i++) {
                bw.write(headers.get(i).getOrigin() + "\t");
            }
            bw.write(headers.get(headers.size() - 1).getOrigin());
            bw.newLine();
            // Write rows
            for (String[] row : cachedRows) {
                for (int i = 0; i < headers.size() - 1; i++) {
                    bw.write(row[i] + "\t");
                }
                bw.write(row[row.length - 1]);
                bw.newLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TSVData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TSVData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

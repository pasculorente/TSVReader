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
    private List<String[]> cachedRows;

    Dataset(List<Header> headers, List<String[]> rows) {
        this.headers = headers;
        this.rows = rows;
        this.cachedRows = rows;
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

    List<String[]> filterText(boolean cache, int column, boolean match, String value) {
        List<String[]> origin = cache ? cachedRows : rows;
        cachedRows = new ArrayList<>();
        if (match) {
            origin.stream().filter((data) -> (data[column].equals(value))).
                    forEach((data) -> {
                cachedRows.add(data);
            });
        } else {
            origin.stream().filter((data) -> (data[column].toLowerCase().contains(value.
                    toLowerCase()))).
                    forEach((data) -> {
                cachedRows.add(data);
            });
        }
        return cachedRows;
    }

    List<String[]> filterGroup(boolean cache, int column, String... values) {
        List<String[]> origin = cache ? cachedRows : rows;
        cachedRows = new ArrayList<>();
        for (String[] data : origin) {
            for (String value : values) {
                if (data[column] != null && data[column].contains(value)) {
                    cachedRows.add(data);
                    break;
                }
            }
        }
        return cachedRows;
    }

    List<String[]> filterNumeric(boolean cache, int column, double min, double max) {
        List<String[]> origin = cache ? cachedRows : rows;
        cachedRows = new ArrayList<>();
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
        return cachedRows;
    }

    void resestVariants() {
        cachedRows = rows;
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

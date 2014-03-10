package tsvreader;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author uichuimi03
 */
public class TSVData {

    public enum DataType {

        NUMERIC, TEXT, GROUP
    }

    private final List<String[]> rows;
    private List<String[]> cachedRows;
    private String fileType;

    private final List<Header> headers;

    public TSVData() {
        this.rows = new ArrayList<>();
        this.headers = new ArrayList<>();
        this.cachedRows = rows;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    String getValue(int row, int column) {
        String[] r = rows.get(row);
        return column < r.length ? r[column] : "";
    }

    List<String[]> filterText(boolean cache, int column, String value) {
        List<String[]> origin = cache ? cachedRows : rows;
        cachedRows = new ArrayList<>();
        origin.stream().filter((data) -> (data[column].toLowerCase().contains(value.toLowerCase()))).
                forEach((data) -> {
                    cachedRows.add(data);
                });
        return cachedRows;
    }

    List<String[]> filterGroup(boolean cache, int column, String... values) {
        List<String[]> origin = cache ? cachedRows : rows;
        cachedRows = new ArrayList<>();
        for (String[] data : origin) {
            for (String value : values) {
                if (data[column].contains(value)) {
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
        origin.stream().forEach((data) -> {
            try {
                Double value = Double.valueOf(data[column]);
                if (min <= value && value <= max) {
                    cachedRows.add(data);
                }
            } catch (NumberFormatException ex) {
            }
        });
        return cachedRows;
    }

    void resestVariants() {
        cachedRows = rows;
    }

    void save(String filename) {
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
//            if (fileType.equals("NONE")) {
//                for (Header header : headers) {
//                    if (header.getChildren().isEmpty()) {
//                        bw.write("#" + header.getName() + ":" + header.getType() + ":" + header.
//                                getDescription());
//                        bw.newLine();
//                    } else {
//                        bw.write("#" + header.getName() + ":multicolumn:" + header.getChildren().
//                                size());
//                        bw.newLine();
//                        for (Header header1 : header.getChildren()) {
//                            bw.write("#" + header1.getName() + ":" + header1.getType() + ":"
//                                    + header1.getDescription());
//                            bw.newLine();
//                        }
//                    }
//                }
//            } else {
//                bw.write("#fileformat:" + fileType);
//                bw.newLine();
//            }
//
//            for (String[] row : cachedRows) {
//                int i = 0;
//                while (i < row.length - 1) {
//                    bw.write(row[i] + "\t");
//                    i++;
//                }
//                bw.write(row[i]);
//                bw.newLine();
//            }
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(TSVData.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(TSVData.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}

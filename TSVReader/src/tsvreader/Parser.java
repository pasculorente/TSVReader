package tsvreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Parser extends Task<Dataset> {

    private final String file, headersfile;

    public Parser(String file, String headersfile) {
        this.file = file;
        this.headersfile = headersfile;
    }

    protected int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].toLowerCase().equals(value.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @param file
     * @return
     */
    String[] parse(String[] inHeaders, String[] line, List<Header> outHeaders) {
        String[] outLine = new String[outHeaders.size()];
        for (int i = 0; i < outLine.length; i++) {
            outLine[i] = "";
        }
        for (int i = 0; i < outHeaders.size(); i++) {
            try {
                int from = indexOf(inHeaders, outHeaders.get(i).getOrigin());
                outLine[i] = line[from];
            } catch (ArrayIndexOutOfBoundsException ex) {
            }
        }
        return outLine;
    }

    private List<Header> readHeadersFile() {
        List<Header> headers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(headersfile))) {
            String line;
            String[] fields;
            while ((line = br.readLine()) != null) {
                fields = line.split(":");
                if (fields.length < 4) {
                    System.err.println("Header bad formed: " + line);
                    continue;
                }
                headers.add(new Header(fields[0], fields[1], fields[2], fields[3], fields.length > 4
                        ? fields[4] : ""));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SIFTParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SIFTParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return headers;
    }

    @Override
    protected Dataset call() throws Exception {
        List<Header> headers = readHeadersFile();
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            String[] inHeaders = line.split("\t");
            removeEmptyHeaders(headers, inHeaders);
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (++i % 100 == 0) {
                    updateMessage(i + " rows loaded.");
                }
                rows.add(parse(inHeaders, line.split("\t"), headers));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Dataset(headers, rows);
    }

    private void removeEmptyHeaders(List<Header> headers, String[] inHeaders) {
        int i = 0, c = 0;
        while (i < headers.size()) {
            int index = indexOf(inHeaders, headers.get(i).getOrigin());
            if (index == -1) {
                headers.remove(i);
                c++;
            } else {
                i++;
            }
        }
        System.out.println(c + " missed columns.");
    }
}

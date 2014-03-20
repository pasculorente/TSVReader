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
 * A parser opens a file and interprets it on a Dataset. By default, Parser stores data using
 * tabulator as field separator. Headers are interpreted from headers file as is.
 *
 * @author Pascual Lorente Arencibia
 */
public class Parser extends Task<Dataset> {

    private final String file, headersfile;

    /**
     * Creates a new Parser, ready to process the file with the headers file.
     *
     * @param file The name of the file to process.
     * @param headersfile The name of the headers file.
     */
    public Parser(String file, String headersfile) {
        this.file = file;
        this.headersfile = headersfile;
    }

    /**
     * Returns the first position of a value in an array.
     *
     * @param array the array.
     * @param value the value to search.
     * @return the index of the first position or -1.
     */
    protected int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].toLowerCase().equals(value.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Parses a single line from the file. The line is passed as a String[], with fields already
     * splitted.
     *
     * @param inHeaders Columns as they are in the file.
     * @param line The splitted line.
     * @param outHeaders Headers as read from headers file.
     * @return an array with the fields for the dataset.
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

    /**
     * Interprets the headers file, creating a list of Header.
     *
     * @return a list of Header, or an empty list.
     */
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

    /**
     * Starts reading lines from file. The first line must be columns names separated by tabs. The
     * rest of rows must have the same length.
     *
     * @return a Dataset with the file parsed.
     * @throws Exception
     */
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

    /**
     * Removes headers that appear in the headers file, but not in the columns names, so they aren't
     * shown in the GUI.
     *
     * @param headers The headers read in the headers file.
     * @param inHeaders The columns names from the input file.
     */
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

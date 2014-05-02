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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import tsvreader.filter.Filter;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Parser2 {

    private final File database, headerFile;
    private ObservableList<String[]> rows;
    private List<Header> headers;
    private final SimpleIntegerProperty lines;
    private final List<Filter> filters;
    private int[] cIndexes;
    private final List<Map<String, Integer>> statsMap;
    private final List<SimpleIntegerProperty> stats;

    public Parser2(File database, File headerFile) {
        this.database = database;
        this.headerFile = headerFile;
        this.filters = new ArrayList<>();
        this.lines = new SimpleIntegerProperty();
        stats = new ArrayList<>();
        statsMap = FXCollections.observableArrayList();
        initialize();
    }

    public ObservableList<String[]> getRows() {
        return rows;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public SimpleIntegerProperty getLines() {
        return lines;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    private void initialize() {
        setHeaders();
        rows = FXCollections.observableArrayList();
        rows.addListener((ListChangeListener.Change<? extends String[]> change) -> {
            Platform.runLater(() -> {
                lines.set(change.getList().size());
            });
        });
        new Thread(new Reader()).start();
    }

    /**
     * Interprets the headers file, creating a list of Header.
     *
     * @return a list of Header, or an empty list.
     */
    private void setHeaders() {
        headers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(headerFile));
                BufferedReader db = new BufferedReader(new FileReader(database))) {
            String line;
            String[] fields;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                fields = line.split(":");
                if (fields.length < 2) {
                    MainViewController.printMessage("Header bad formed: " + line);
                    continue;
                }
                // origin:type:alias:description
                // Fields alias and description are optional
                final String origin = fields[0];
                final String type = fields[1];
                // If alias is empty or not specified, falls back to origin.
                final String alias = fields.length <= 2 ? origin
                        : (fields[2].isEmpty() ? origin : fields[2]);
                // If description is empty or not specified, falls back to alias.
                final String description = fields.length <= 3 ? alias
                        : (fields[3].isEmpty() ? alias : fields[3]);
                headers.add(new Header(origin, type, alias, description));
                // Each header has an associate stat column.
                statsMap.add(new TreeMap<>());
            }
            cIndexes = new int[headers.size()];
            String[] dbHeaders = db.readLine().split("\t");
            for (int i = 0; i < dbHeaders.length; i++) {
                for (int k = 0; k < headers.size(); k++) {
                    if (dbHeaders[i].equals(headers.get(k).getOrigin())) {
                        cIndexes[k] = i;
                        break;
                    }
                }
            }
            for (int i = 0; i < statsMap.size(); i++) {
                stats.add(new SimpleIntegerProperty());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SIFTParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SIFTParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void applyFilters() {
        rows.clear();
        new Thread(new Reader()).start();
    }

    public List<Map<String, Integer>> getStatsMap() {
        return statsMap;
    }

    public List<SimpleIntegerProperty> getStats() {
        return stats;
    }

    private class Reader extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            MainViewController.printMessage("Loading rows.");
            try (BufferedReader br = new BufferedReader(new FileReader(database))) {
                br.readLine();
                br.lines().forEach((String t) -> {
                    String[] row = t.split("\t");
                    String[] destiny = new String[cIndexes.length];
                    for (int i = 0; i < cIndexes.length; i++) {
                        destiny[i] = row[cIndexes[i]];
                    }
                    for (Filter filter : filters) {
                        if (!filter.accept(destiny[filter.getColumn()])) {
                            return;
                        }
                    }
                    rows.add(destiny);
                    // Update stats:
                    for (int i = 0; i < statsMap.size(); i++) {
                        final Map<String, Integer> map = statsMap.get(i);
                        final int index = i;
                        if (map.containsKey(destiny[i])) {
                            final int incr = map.get(destiny[i]) + 1;
                            map.put(destiny[i], incr);
                            stats.get(index).set(incr);
                        } else {
                            map.put(destiny[i], 1);
                            stats.get(index).set(1);
                        }
                    }
                });
            }
            MainViewController.printMessage("Load complete.");
            return null;
        }

    }
}

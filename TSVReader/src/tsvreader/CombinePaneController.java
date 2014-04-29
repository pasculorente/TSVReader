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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author uichuimi03
 */
public class CombinePaneController {

    @FXML private TextField firstFile;
    @FXML private TextField secondFile;
    @FXML private VBox matchingColumns;
    @FXML private VBox secondShowColumns;
    @FXML private VBox firstShowColumns;
    @FXML private ProgressBar progressBar;
    @FXML private Label message;
    @FXML private Label progressText;

    private List<String> fHeaders, sHeaders;
    private Dataset dataset;

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        fHeaders = null;
        sHeaders = null;
    }

    @FXML private void openFirstFile() {
        File file = OS.openTSV(null);
        if (file != null) {
            firstFile.setText(file.getAbsolutePath());
            fHeaders = readHeaders(file);
            firstShowColumns.getChildren().clear();
            matchingColumns.getChildren().clear();
            fHeaders.stream().forEach((String s) -> {
                CheckBox cb = new CheckBox(s);
                cb.setSelected(true);
                firstShowColumns.getChildren().add(cb);
            });
        }
    }

    @FXML private void openSecondFile() {
        File file = OS.openTSV(null);
        if (file != null) {
            secondFile.setText(file.getAbsolutePath());
            sHeaders = readHeaders(file);
            secondShowColumns.getChildren().clear();
            matchingColumns.getChildren().clear();
            sHeaders.stream().forEach((String s) -> {
                CheckBox cb = new CheckBox(s);
                cb.setSelected(true);
                secondShowColumns.getChildren().add(cb);
            });
        }
    }

    @FXML private void addMatchingColumn() {
        if (firstFile.getText().isEmpty() || secondFile.getText().isEmpty()) {
            return;
        }
        ComboBox fCombo = new ComboBox(FXCollections.observableArrayList(fHeaders));
        ComboBox sCombo = new ComboBox(FXCollections.observableArrayList(sHeaders));
        Label equals = new Label("=");
        HBox.setHgrow(fCombo, Priority.SOMETIMES);
        HBox.setHgrow(sCombo, Priority.SOMETIMES);
        HBox box = new HBox(5, fCombo, equals, sCombo);
        box.setAlignment(Pos.CENTER);
        matchingColumns.getChildren().add(box);
    }

    private List<String> readHeaders(File file) {
        List<String> list = new ArrayList();
        try (BufferedReader br = OS.openTextBR(file)) {
            String firstLine = br.readLine();
            list.addAll(Arrays.asList(firstLine.split("\t")));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CombinePaneController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CombinePaneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @FXML private void match() {
        if (firstFile.getText().isEmpty() || secondFile.getText().isEmpty()) {
            return;
        }
        Task<Dataset> task = new Matcher();
        message.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        progressText.textProperty().bind(progressBar.progressProperty().asString("%5.2f"));
        new Thread(task).start();
    }

    private int indexOf(String value, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public Dataset getDataset() {
        return dataset;
    }

    private class Matcher extends Task<Dataset> {

        @Override
        protected Dataset call() throws Exception {
            try (BufferedReader br = OS.openTextBR(firstFile.getText());
                    BufferedReader br2 = OS.openTextBR(secondFile.getText())) {
                // Matching indexes (columns to match).
                updateProgress(0, 10);
                updateMessage("(1/4) Reading meta info");
                List<Integer> fmatchingIndex = new ArrayList<>();
                List<Integer> smatchingIndex = new ArrayList<>();
                matchingColumns.getChildren().stream().forEach((Node n) -> {
                    HBox hbox = (HBox) n;
                    ComboBox fc = (ComboBox) hbox.getChildren().get(0);
                    ComboBox sc = (ComboBox) hbox.getChildren().get(2);
                    fmatchingIndex.add(fc.getSelectionModel().getSelectedIndex());
                    smatchingIndex.add(sc.getSelectionModel().getSelectedIndex());
                    System.out.println(fc.getSelectionModel().getSelectedItem() + " <> "
                            + sc.getSelectionModel().getSelectedItem());
                });
                // Show indexes (columns to show).
                updateProgress(1, 10);
                List<Integer> fShowIndex = new ArrayList<>();
                List<Integer> sShowIndex = new ArrayList<>();
                for (int i = 0; i < firstShowColumns.getChildren().size(); i++) {
                    CheckBox checkBox = (CheckBox) firstShowColumns.getChildren().get(i);
                    if (checkBox.isSelected()) {
                        fShowIndex.add(i);
                    }
                }
                for (int i = 0; i < secondShowColumns.getChildren().size(); i++) {
                    CheckBox checkBox = (CheckBox) secondShowColumns.getChildren().get(i);
                    if (checkBox.isSelected()) {
                        sShowIndex.add(i);
                    }
                }
                // Load files into memory.
                updateProgress(2, 10);
                updateMessage("(2/4) Loading into memory: " + firstFile.getText());
                List<String[]> f1 = new ArrayList<>();
                List<String[]> f2 = new ArrayList<>();
                br.lines().forEach((String t) -> {
                    f1.add(t.split("\t"));
                });
                updateProgress(3, 10);
                updateMessage("(3/4) Loading into memory: " + secondFile.getText());
                br2.lines().forEach((String t) -> {
                    f2.add(t.split("\t"));
                });
                // Take headers appart.
                String[] fHeads = f1.remove(0);
                String[] sHeads = f2.remove(0);
                updateMessage("(4/4) Combining files");
                List<Header> headers = new ArrayList<>();
                List<String[]> rows = new ArrayList<>();
                int rowWidth = fShowIndex.size() + sShowIndex.size();
                // Double bouble. Can be improved if both files are sorted.
                // Must be improved.
                // Two files of 300k -> 80lines/second
                try {
                    f1.parallelStream().forEachOrdered(new Consumer<String[]>() {
                        int progress = 0;
                        int lastAccess = 0;

                        @Override
                        public void accept(String[] l1) {
                            updateProgress(progress++, f1.size());
                            updateMessage("(4/4) Combinig files (" + progress + "/" + f1.size()
                                    + " lines processed)");
                            int index = lastAccess;
                            while (index != lastAccess - 1) {
//                            for (String[] l2 : f2) {
                                String[] l2 = f2.get(index);
                                index = (index + 1) % f2.size();
                                boolean match = true;
                                for (int i = 0; i < fmatchingIndex.size(); i++) {
                                    if (!l1[fmatchingIndex.get(i)].equals(l2[smatchingIndex.get(i)])) {
                                        match = false;
                                        break;
                                    }
                                }
                                if (match) {
                                    String[] cRow = new String[rowWidth];
                                    int p = 0;
                                    for (int i : fShowIndex) {
                                        cRow[p++] = l1[i];
                                    }
                                    for (int i : sShowIndex) {
                                        cRow[p++] = l2[i];
                                    }
                                    rows.add(cRow);
                                    break;
                                }
                            }
                            lastAccess = index;
                        }
                    });
                } catch (ArrayIndexOutOfBoundsException ex) {
                    MainViewController.printMessage("Warning. Some rows could not loaded"
                            + " because they are shorter than header.");
                }
                fShowIndex.stream().forEach((Integer i) -> {
                    headers.add(new Header(fHeads[i], "text", fHeads[i], ""));
                });
                sShowIndex.stream().forEach((Integer i) -> {
                    headers.add(new Header(sHeads[i], "text", sHeads[i], ""));
                });
                dataset = new Dataset(headers, rows);
                MainViewController.printMessage(rows.size() + " matches.");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CombinePaneController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CombinePaneController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return dataset;
        }

    }
}

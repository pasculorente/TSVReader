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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tsvreader.filter.Filter;
import tsvreader.filter.NumericFilter;
import tsvreader.filter.TextFilter;

/**
 * The controller of the main view. With the open button, it shows a window to
 * select a file. Then, loads the Dataset, creates the filters view, and shows
 * the table. It also interprets user filters and translate them to the Dataset.
 *
 * @author Pascual Lorente Arencibia
 */
public class MainViewController {

    /**
     * The ScrollPane where the data table is wrapped.
     */
    @FXML
    private ScrollPane tableContainer;
    /**
     * The VBox where filters are shown.
     */
    @FXML
    private VBox filtersBox;
    /**
     * The label to show the number of rows.
     */
    @FXML
    private Label lines;
    /**
     * The save Button.
     */
    @FXML
    private Button saveButton;
    /**
     * The ComboBox to load new filters.
     */
    @FXML
    private ComboBox filtersComboBox;
    @FXML
    private Label message;
    private static Label staticMessage;
    /**
     * THE TABLE!!!!.
     */
    private TableView<String[]> table;
    /**
     * The Dataset where the file is loaded.
     */
    private Dataset dataset;
    /**
     * The list of Filters (not the graphical filters, but the intermediate
     * ones). Any filter in this list corresponds to the same column filter in
     * the GUI filters list.
     */
    // private List<Filter> filters;
    /**
     * The file parser/loader.
     */
//    private Parser parser;
    private Parser2 parser2;
    /**
     * The file type.
     */
    private String type;

    /**
     * Makes some initial configuration. This method is called automatically
     * when the window is loaded.
     */
    public void initialize() {
        staticMessage = message;
        saveButton.setDisable(true);
        filtersComboBox.getItems().clear();
    }

    /**
     * Shows the file selection, waits for the user to select a file, closes the
     * file selection windows and launches a Parser to load the Dataset. When
     * the dataset is loaded, it will automatically call restartGUI().
     */
    @FXML
    private void load() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("openPane.fxml"));
            Parent parent = loader.load();
            OpenPaneController controller = loader.getController();
            Stage s = new Stage();
            s.setScene(new Scene(parent));
            controller.setScene(parent.getScene());
            s.showAndWait();
            if (!controller.isCancelled()) {
                String file = controller.getFile();
                type = controller.getType();
                parser2 = new Parser2(new File(file), new File("tsv_files", type + ".header"));
                setTable();
                setFilters();
//                new Thread(new StatsRunner()).start();
//                switch (type) {
//                    case "sift_snp":
//                        parser = new SIFTParser(file);
//                        break;
//                    default:
//                        parser = new Parser(file, "tsv_files/" + type + ".header");
//                }
//                parser.setOnSucceeded((WorkerStateEvent t) -> {
//                    restartGUI();
//                });
//                lines.textProperty().bind(parser.messageProperty());
//                new Thread(parser).start();
                TSVReader.setTitle(file);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    /**
     * Refresh the filters pane, according to the dataset. It also resets the
     * list of internal filters.
     */
    private void loadFilters() {
        filtersBox.getChildren().clear();
        parser2.getFilters().clear();
        filtersComboBox.getItems().clear();
        for (int i = 0; i < parser2.getHeaders().size(); i++) {
            Header header = parser2.getHeaders().get(i);
            filtersComboBox.getItems().add(header.getName());
        }
    }

    /**
     * Method called when the user presses the Add Button in the filters pane.
     * if there is a column selected in the ComboBox, it will add a new Filter.
     */
    @FXML
    private void addNewFilter() {
        int index = filtersComboBox.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            addFilter(parser2.getHeaders().get(index), index);
        }
    }

    /**
     * Adds a new filter to the filters pane and creates the corresponding
     * internal filter.
     *
     * @param header
     * @param index
     */
    private void addFilter(Header header, int index) {
        FilterViewController controller;
        final Parent parent;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FilterView.fxml"));
        try {
            parent = loader.load();
            if (parent == null) {
                return;
            }
            controller = loader.getController();
            controller.getName().setText(header.getName());
            controller.getName().setTooltip(new Tooltip(header.getDescription()));
            controller.getDelete().setOnAction((ActionEvent t) -> {
                removeFilter(parent);
            });
            controller.getLogic().setOnAction((Event t) -> {
                filter();
            });
            controller.getEmpty().setOnAction((ActionEvent t) -> {
                filter();
            });
            switch (header.getType().toLowerCase()) {
                case "numeric":
                    TextField min = new TextField();
                    TextField max = new TextField();
                    min.setPromptText("min");
                    max.setPromptText("Max");
                    min.setOnAction((ActionEvent t) -> {
                        filter();
                    });
                    max.setOnAction((ActionEvent t) -> {
                        filter();
                    });
                    controller.getValues().getChildren().addAll(min, max);
                    filtersBox.getChildren().add(parent);
                    parser2.getFilters().add(new NumericFilter("numeric", index, controller.
                            getLogic(), controller.getEmpty(), min, max));
                    HBox.setHgrow(min, Priority.SOMETIMES);
                    HBox.setHgrow(max, Priority.SOMETIMES);
                    break;
                case "text":
                    TextField value = new TextField();
                    value.setPromptText(header.getName());
                    if (!header.getDescription().isEmpty()) {
                        value.setTooltip(new Tooltip(header.getDescription()));
                    }
                    value.setOnAction((ActionEvent t) -> {
                        filter();
                    });
                    controller.getValues().getChildren().add(value);
                    HBox.setHgrow(value, Priority.SOMETIMES);
                    filtersBox.getChildren().add(parent);
                    parser2.getFilters().add(new TextFilter(header.getType(), index, controller.
                            getLogic(), controller.getEmpty(), value));
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Removes a filter from the GUI and from the internal filters.
     *
     * @param parent The GUI filter pane.
     */
    private void removeFilter(Parent parent) {
        for (int i = 0; i < filtersBox.getChildren().size(); i++) {
            Parent p = (Parent) filtersBox.getChildren().get(i);
            if (p == parent) {
                filtersBox.getChildren().remove(i);
                parser2.getFilters().remove(i);
                filter();
                return;
            }
        }
    }

    /**
     * Applies all the current filters to the Dataset and reloads the table
     * using the cachedRows from the Dataset.
     */
    private void filter() {
        parser2.applyFilters();
//        dataset.resetVariants();
//        filters.forEach(this::filter);
//        table.setItems(FXCollections.observableArrayList(dataset.getCachedRows()));
//        lines.setText(dataset.getCachedRows().size() + " rows (" + dataset.getRows().size()
//                + " in total)");
 //       new Thread(new StatsRunner()).start();
    }

    /**
     * Applies a filter to the Dataset.
     *
     * @param filter The filter.
     * @param logic If it is a ONLY or a NOT filter.
     * @see Filter
     */
    private void filter(Filter filter) {
        Dataset.Logic logic = (Dataset.Logic) filter.getLogic().getValue();
        boolean empty = filter.getEmpty().isSelected();
        switch (filter.getType().toLowerCase()) {
            case "numeric":
                try {
                    NumericFilter nFilter = (NumericFilter) filter;
                    String min = nFilter.getMin().getText();
                    String max = nFilter.getMax().getText();
                    if (!min.isEmpty() && !max.isEmpty()) {
                        double minimun = Double.valueOf(min);
                        double maximum = Double.valueOf(max);
                        dataset.filterNumeric(filter.getColumn(), minimun, maximum, logic, empty);
                    }
                } catch (NumberFormatException ex) {
                    printMessage("Bad number format");
                }
                break;
            case "text":
                String value = ((TextFilter) filter).getValue().getText();
                if (!value.isEmpty()) {
                    dataset.filterText(filter.getColumn(), value, logic, empty);
                }
                break;
        }
    }

    /**
     * Shows a pane to the user to save the filtered Rows in the Dataset in a
     * file. This method will use a Saver to do the job. See Saver for mor
     * details.
     *
     * @see Saver
     */
    @FXML
    private void save() {
        File f = OS.saveTSV(TSVReader.getMainWindow());
        if (f != null) {
            switch (type) {
                case "sift_snp":
                    SIFTSaver.save(dataset, f);
                    break;
                default:
                    Saver.save(dataset, f);
            }

        }
    }

    @FXML
    private void combine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CombinePane.fxml"));
            Parent parent = loader.load();
            CombinePaneController controller = loader.getController();
            Stage s = new Stage();
            s.setTitle("Combine two files");
            s.setScene(new Scene(parent));
            s.showAndWait();
            dataset = controller.getDataset();
            lines.textProperty().unbind();
            lines.setText(dataset.getRows().size() + " rows (" + dataset.getRows().size()
                    + " in total)");
            loadFilters();
            saveButton.setDisable(false);
//            table = populateTable();
            tableContainer.setContent(table);
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    public static void printMessage(String message) {
        System.out.println(message);
        Platform.runLater(() -> {
            staticMessage.setText(message);
        });
    }

    /**
     * Creates a new table and associates the data in parser2.
     */
    private void setTable() {
        TableView<String[]> newTable = new TableView<>(parser2.getRows());
        // Creating the columns...
        for (int i = 0; i < parser2.getHeaders().size(); i++) {
            Header h = parser2.getHeaders().get(i);
            TableColumn<String[], String> aColumn = new TableColumn<>();
            aColumn.setText(null);
            // The column header is a personalized Node (HeaderVBox)
            // that displays not only the column name, but stats too.
            HeaderVbox hv = new HeaderVbox(h.getName(), h.getDescription());
            hv.count.textProperty().bind(parser2.getStats().get(i).asString());
            aColumn.setGraphic(hv);
            final int index = i;
            aColumn.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> row) -> {
                return new SimpleStringProperty(row.getValue()[index]);
            });
            aColumn.setCellFactory((TableColumn<String[], String> p) -> new CopiableCell());
            newTable.getColumns().add(aColumn);
        }
        newTable.setSortPolicy((TableView<String[]> p) -> {
            return false;
        });
        newTable.setEditable(true);
        newTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lines.textProperty().bind(parser2.getLines().asString());
        table = newTable;
        tableContainer.setContent(table);
    }

    /**
     * Clears the current filters and creates a new list with columns from
     * parser2.
     *
     */
    private void setFilters() {
        filtersBox.getChildren().clear();
        parser2.getFilters().clear();
        filtersComboBox.getItems().clear();
        for (int i = 0; i < parser2.getHeaders().size(); i++) {
            Header header = parser2.getHeaders().get(i);
            filtersComboBox.getItems().add(header.getName());
        }
    }

    private class StatsRunner extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            printMessage("Running stats.");
            List<Map<String, Integer>> values = new ArrayList<>(parser2.getHeaders().size());
            parser2.getHeaders().forEach((Header header) -> {
                values.add(new TreeMap<>());
            });
            parser2.getRows().forEach((String[] row) -> {
                for (int column = 0; column < row.length; column++) {
                    final Map<String, Integer> map = values.get(column);
                    if (map.containsKey(row[column])) {
                        map.put(row[column], map.get(row[column]) + 1);
                    } else {
                        map.put(row[column], 1);
                    }
                }
            });
            for (int i = 0; i < values.size(); i++) {
                final int index = i;
                Platform.runLater(() -> {
                    HeaderVbox header = (HeaderVbox) table.getColumns().get(index).getGraphic();
                    header.count.setText(values.get(index).size() + "");
                });
            }
            printMessage("Stats done.");
            return null;
        }

    };

    private class CopiableCell extends TableCell<String[], String> {

        private TextField textField;

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }

            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getItem());
                    }
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getItem());
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getItem());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setEditable(false);
            textField.selectAll();
        }
    }

    private class HeaderVbox extends VBox {

        Label title, count;

        public HeaderVbox(String title, String description) {
            this.title = new Label(title);
            this.count = new Label();
            setAlignment(Pos.TOP_CENTER);
            getChildren().addAll(this.title, this.count);
            this.title.setTooltip(new Tooltip(description));
        }

    }

}

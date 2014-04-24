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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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

/**
 * The controller of the main view. With the open button, it shows a window to select a file. Then,
 * loads the Dataset, creates the filters view, and shows the table. It also interprets user filters
 * and translate them to the Dataset.
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
     * The list of Filters (not the graphical filters, but the intermediate ones). Any filter in
     * this list corresponds to the same column filter in the GUI filters list.
     */
    private List<Filter> filters;
    /**
     * The file parser/loader.
     */
    private Parser parser;
    /**
     * The file type.
     */
    private String type;

    /**
     * Makes some initial configuration. This method is called automatically when the window is
     * loaded.
     */
    public void initialize() {
        staticMessage = message;
        filters = new ArrayList<>();
        saveButton.setDisable(true);
        filtersComboBox.getItems().clear();
    }

    /**
     * Shows the file selection, waits for the user to select a file, closes the file selection
     * windows and launches a Parser to load the Dataset. When the dataset is loaded, it will
     * automatically call restartGUI().
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
                switch (type) {
                    case "sift_snp":
                        parser = new SIFTParser(file);
                        break;
                    default:
                        parser = new Parser(file, "tsv_files/" + type + ".header");
                }
                parser.setOnSucceeded((WorkerStateEvent t) -> {
                    restartGUI();
                });
                lines.textProperty().bind(parser.messageProperty());
                new Thread(parser).start();
                TSVReader.setTitle(file);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    /**
     * Takes a new Dataset from the parser and reloads all the Graphical User Interface: the table
     * and the filters box.
     */
    private void restartGUI() {
        try {
            dataset = parser.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        lines.textProperty().unbind();
        lines.setText(dataset.getRows().size() + " rows (" + dataset.getRows().size()
                + " in total)");
        loadFilters();
        saveButton.setDisable(false);
        table = populateTable();
        tableContainer.setContent(table);
        new Thread(new StatsRunner()).start();
    }

    /**
     * Using the current Dataset, makes a new table. Creates all the columns and assigns the content
     * to the view. This new table is not allocated anywhere in the GUI. Use the return value to put
     * it whereas you want.
     *
     * @return the new table created from the current Dataset.
     */
    private TableView<String[]> populateTable() {
        TableView<String[]> newTable = new TableView<>(FXCollections.observableArrayList(dataset.
                getRows()));
        //TableColumn iColumn = new TableColumn("#");
        //iColumn.setCellFactory((Object p) -> new NumberedCell());
        //newTable.getColumns().add(iColumn);
        for (int i = 0; i < dataset.getHeaders().size(); i++) {
            Header header = dataset.getHeaders().get(i);
            TableColumn<String[], String> aColumn = new TableColumn<>(header.getName());
            final int index = i;
            aColumn.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> row) -> {
                return new SimpleStringProperty(index < row.getValue().length
                        ? row.getValue()[index] : "");
            });
            aColumn.setCellFactory((TableColumn<String[], String> p) -> new CopiableCell());
            newTable.getColumns().add(aColumn);
        }
        newTable.setSortPolicy((TableView<String[]> p) -> {
            return false;
        });
        newTable.setEditable(true);
        newTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return newTable;
    }

    /**
     * Refresh the filters pane, according to the dataset. It also resets the list of internal
     * filters.
     */
    private void loadFilters() {
        filtersBox.getChildren().clear();
        filters.clear();
        filtersComboBox.getItems().clear();
        for (int i = 0; i < dataset.getHeaders().size(); i++) {
            Header header = dataset.getHeaders().get(i);
            filtersComboBox.getItems().add(header.getName());
        }
    }

    /**
     * Method called when the user presses the Add Button in the filters pane. if there is a column
     * selected in the ComboBox, it will add a new Filter.
     */
    @FXML
    private void addNewFilter() {
        int index = filtersComboBox.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            addFilter(dataset.getHeaders().get(index), index);
        }
    }

    /**
     * Adds a new filter to the filters pane and creates the corresponding internal filter.
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
                    filters.add(new NumericFilter("numeric", index, controller.getLogic(),
                            controller.getEmpty(), min, max));
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
                    filters.add(new TextFilter(header.getType(), index, controller.getLogic(),
                            controller.getEmpty(), value));
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
                filters.remove(i);
                filter();
                return;
            }
        }
    }

    /**
     * Applies all the current filters to the Dataset and reloads the table using the cachedRows
     * from the Dataset.
     */
    private void filter() {
        dataset.resetVariants();
        filters.forEach(this::filter);
        table.setItems(FXCollections.observableArrayList(dataset.getCachedRows()));
        lines.setText(dataset.getCachedRows().size() + " rows (" + dataset.getRows().size()
                + " in total)");
        new Thread(new StatsRunner()).start();
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
     * Shows a pane to the user to save the filtered Rows in the Dataset in a file. This method will
     * use a Saver to do the job. See Saver for mor details.
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

    @FXML private void combine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CombinePane.fxml"));
            Parent parent = loader.load();
            CombinePaneController controller = loader.getController();
            Stage s = new Stage();
            s.setTitle("Combine two files");
            s.setScene(new Scene(parent));
            s.showAndWait();
            if (dataset != null) {
                dataset = controller.getDataset();
                lines.textProperty().unbind();
                lines.setText(dataset.getRows().size() + " rows (" + dataset.getRows().size()
                        + " in total)");
                loadFilters();
                saveButton.setDisable(false);
                table = populateTable();
                tableContainer.setContent(table);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    public static void printMessage(String message) {
        Platform.runLater(() -> {
            staticMessage.setText(message);
        });
    }

    /**
     * This is an internal representation of a Filter in the GUI. It stores type (text or numeric),
     * the corresponding column in the table and the GUI elements where the user put the parameters
     * (like TextFileds or ComboBoxes).
     */
    private abstract static class Filter {

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
         * @param nodes the parameters nodes.
         */
        private Filter(String type, int column, ComboBox logic, CheckBox empty) {
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

    }

    private static class NumericFilter extends Filter {

        private final TextField min, max;

        public NumericFilter(String type, int column, ComboBox logic, CheckBox empty, TextField min,
                TextField max) {
            super(type, column, logic, empty);
            this.min = min;
            this.max = max;
        }

        public TextField getMax() {
            return max;
        }

        public TextField getMin() {
            return min;
        }

    }

    private static class TextFilter extends Filter {

        private final TextField value;

        public TextFilter(String type, int column, ComboBox logic, CheckBox empty, TextField value) {
            super(type, column, logic, empty);
            this.value = value;
        }

        public TextField getValue() {
            return value;
        }

    }

    private class StatsRunner extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            System.out.println("You called StatsRunner");
            List<Map<String, Integer>> values = new ArrayList<>(dataset.getHeaders().size());
            dataset.getHeaders().forEach((Header header) -> {
                values.add(new TreeMap<>());
            });
            dataset.getCachedRows().forEach((String[] row) -> {
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
                    table.getColumns().get(index).setText(dataset.getHeaders().get(index).getName()
                            + "\n" + values.get(index).size());
                });
            }
            System.out.println("I finished");
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

    private class NumberedCell extends TableCell {

        @Override
        protected void updateItem(Object t, boolean bln) {
            setText(getIndex() + "");
        }

    }
}

package tsvreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author uichuimi03
 */
public class MainViewController {

    @FXML
    private ScrollPane tableContainer;
    @FXML
    private VBox filtersBox;
    @FXML
    private Label lines;
    @FXML
    private Button saveButton;
    @FXML
    private ComboBox filtersComboBox;

    private TableView<String[]> table;
    private Dataset dataset;
    private List<Filter> filters;
    private Parser parser;
    private String type;

    public void initialize() {
        filters = new ArrayList<>();
        saveButton.setDisable(true);
        filtersComboBox.getItems().clear();
    }

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
                    restartTable();
                });
                lines.textProperty().bind(parser.messageProperty());
                new Thread(parser).start();
                TSVReader.setTitle(file);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    private void restartTable() {
        try {
            dataset = parser.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        lines.textProperty().unbind();
        lines.setText(dataset.getRows().size() + " rows (" + dataset.getRows().size()
                + " in total)");
        loadFilters();
        saveButton.setDisable(false);
        table = populateTable();
        tableContainer.setContent(table);
    }

    private TableView<String[]> populateTable() {
        TableView<String[]> newTable = new TableView<>(FXCollections.observableArrayList(dataset.
                getRows()));
        TableColumn groupColumn = null;
        for (int i = 0; i < dataset.getHeaders().size(); i++) {
            Header header = dataset.getHeaders().get(i);
            TableColumn<String[], String> aColumn = new TableColumn<>(header.getName());
            final int index = i;
            aColumn.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> row) -> {
                return new SimpleStringProperty(index < row.getValue().length
                        ? row.getValue()[index] : "");
            });
            if (header.getParent().isEmpty()) {
                if (groupColumn != null) {
                    newTable.getColumns().add(groupColumn);
                }
                newTable.getColumns().add(aColumn);
                groupColumn = null;
            } else if (groupColumn != null && groupColumn.getText().equals(header.getParent())) {
                groupColumn.getColumns().add(aColumn);
            } else {
                if (groupColumn != null) {
                    newTable.getColumns().add(groupColumn);
                }
                groupColumn = new TableColumn(header.getParent());
                groupColumn.getColumns().add(aColumn);
            }
        }
        if (groupColumn != null) {
            newTable.getColumns().add(groupColumn);
        }
        newTable.setSortPolicy((TableView<String[]> p) -> {
            return false;
        });
        return newTable;
    }

    private void loadFilters() {
        filtersBox.getChildren().clear();
        filters.clear();
        filtersComboBox.getItems().clear();
        for (int i = 0; i < dataset.getHeaders().size(); i++) {
            Header header = dataset.getHeaders().get(i);
            filtersComboBox.getItems().add(header.getParent() + ":" + header.getName());
        }
    }

    @FXML
    private void addNewFilter() {
        int index = filtersComboBox.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            addFilter(dataset.getHeaders().get(index), index);
        }
    }

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
            controller.getName().setText(header.getParent() + ":" + header.getName());
            controller.getName().setTooltip(new Tooltip(header.getDescription()));
            controller.getDelete().setOnAction((ActionEvent t) -> {
                removeFilter(parent);
            });
            controller.getLogic().setOnAction((Event t) -> {
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
                    filters.
                            add(new Filter(header.getType(), index, controller.getLogic(), min, max));
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
                    filters.add(new Filter(header.getType(), index, controller.getLogic(), value));
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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

    private void filter(Filter filter, Dataset.Logic logic) {
        switch (filter.getType().toLowerCase()) {
            case "numeric":
                try {
                    String min = ((TextField) filter.getNodes()[1]).getText();
                    String max = ((TextField) filter.getNodes()[2]).getText();
                    if (!min.isEmpty() && !max.isEmpty()) {
                        double minimun = Double.valueOf(min);
                        double maximum = Double.valueOf(max);
                        dataset.filterNumeric(filter.getIndex(), minimun, maximum, logic);
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Bad number format");
                }
                break;
            case "text":
                String value = ((TextField) filter.getNodes()[1]).getText();
                if (!value.isEmpty()) {
                    dataset.filterText(filter.getIndex(), value, logic);
                }
                break;
        }
    }

    private void filter() {
        dataset.resetVariants();
        for (Filter filter : filters) {
            ComboBox cb = (ComboBox) filter.nodes[0];
            Dataset.Logic logic = (Dataset.Logic) cb.getValue();
            filter(filter, logic);
        }
        table.setItems(FXCollections.observableArrayList(dataset.getCachedRows()));
        lines.setText(dataset.getCachedRows().size() + " rows (" + dataset.getRows().size()
                + " in total)");
    }

    @FXML
    private void save() {
        File f = OS.saveTSV();
        if (f != null) {
            switch (type) {
                case "sift_snp":
                    SIFTSaver.save(dataset, f);
                    break;
                default:
                    dataset.save(f.getAbsolutePath());

            }

        }
    }

    private static class Filter {

        private final Node[] nodes;
        private final int index;
        private final String type;

        private Filter(String type, int index, Node... nodes) {
            this.type = type;
            this.nodes = nodes;
            this.index = index;
        }

        public Node[] getNodes() {
            return nodes;
        }

        public int getIndex() {
            return index;
        }

        public String getType() {
            return type;
        }
    }

}

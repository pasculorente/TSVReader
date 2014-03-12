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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
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
    private Label filename;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Button openButton;
    @FXML
    private Button saveButton;
    private TableView<String[]> table;
    private Dataset dataset;
    private List<Filter> filters;

    private Parser parser;
    private String type;

    public void initialize() {
        filters = new ArrayList<>();
        saveButton.setDisable(true);
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
                progress.progressProperty().bind(parser.progressProperty());
                lines.textProperty().bind(parser.messageProperty());
                new Thread(parser).start();
                this.filename.setText(file);
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
        progress.progressProperty().unbind();
        progress.setProgress(1);
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
        String currentParent = "";
        for (int i = 0; i < dataset.getHeaders().size(); i++) {
            if (!dataset.getHeaders().get(i).getParent().isEmpty()) {
                if (!dataset.getHeaders().get(i).getParent().equals(currentParent)) {
                    currentParent = dataset.getHeaders().get(i).getParent();
                    filtersBox.getChildren().add(new Label(currentParent));
                    filtersBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
                }
            } else {
                filtersBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
                currentParent = "";
            }
            addFilter(dataset.getHeaders().get(i), i);
        }
    }

    private void addFilter(Header header, int index) {
        HBox hBox = new HBox(5);
        switch (header.getType().toLowerCase()) {
            case "numeric":
                TextField min = new TextField();
                min.setPromptText(header.getName());
                if (!header.getDescription().isEmpty()) {
                    min.setTooltip(new Tooltip(header.getDescription()));
                }
                min.setOnAction((ActionEvent t) -> {
                    filter();
                });
                min.setMaxWidth(80);
                TextField max = new TextField();
                max.setPromptText("Max");
                max.setOnAction((ActionEvent t) -> {
                    filter();
                });
                max.setMaxWidth(80);
                hBox.getChildren().setAll(min, max);
                filtersBox.getChildren().add(hBox);
                filters.add(new Filter(header.getType(), index, min, max));
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
                CheckBox cb = new CheckBox();
                cb.setTooltip(new Tooltip("Exact match"));
                cb.setOnAction((ActionEvent t) -> {
                    filter();
                });
                hBox.getChildren().addAll(value, cb);
                filtersBox.getChildren().add(hBox);
                filters.add(new Filter(header.getType(), index, value, cb));
                break;
        }
    }

    private void filter() {
        dataset.resestVariants();
        List<String[]> filtered = dataset.getRows();
        for (Filter filter : filters) {
            switch (filter.getType().toLowerCase()) {
                case "numeric":
                    try {
                        String min = ((TextField) filter.getNodes()[0]).getText();
                        String max = ((TextField) filter.getNodes()[1]).getText();
                        if (!min.isEmpty() && !max.isEmpty()) {
                            double minimun = Double.valueOf(min);
                            double maximum = Double.valueOf(max);
                            filtered = dataset.filterNumeric(true, filter.getIndex(), minimun,
                                    maximum);
                        }
                    } catch (NumberFormatException ex) {
                        System.err.println("Bad number format");
                    }
                    break;
                case "text":
                    String value = ((TextField) filter.getNodes()[0]).getText();
                    boolean match = ((CheckBox) filter.getNodes()[1]).isSelected();
                    if (!value.isEmpty()) {
                        filtered = dataset.filterText(true, filter.getIndex(), match, value);
                    }
                    break;
            }
        }
        table.setItems(FXCollections.observableArrayList(filtered));
        lines.setText(filtered.size() + " rows (" + dataset.getRows().size() + " in total)");
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

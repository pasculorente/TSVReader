package tsvreader;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Contains methods to control files in DNAnalytics and fields containing file
 * filters. Open and save files, set TextFileds with the name of the files and
 * compress and uncompress files.
 *
 * @author Pascual Lorente Arencibia
 */
public class OS {

    private static File lastPath;

    public static final String VCF_EXTENSION = ".vcf";
    public static final String VCF_DESCRIPTION = "Variant Call Format";
    public static final String[] VCF_FILTERS = new String[]{"*.vcf"};

    public static final String TSV_DESCRIPTION = "Tabular Separated Values";
    public static final String TSV_EXTENSION = ".tsv";
    public static final String[] TSV_FILTERS = new String[]{"*.tsv", "*.txt"};

    private static File openFile(String title, String description, String[] filters,
            Window window) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        fileChooser.setInitialDirectory(lastPath);
        if (description != null && filters != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(description, filters));
        }
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            lastPath = file.getParentFile();
            return file;
        }
        return null;
    }

    public OS() {
        switch (System.getProperty("os.name")) {
            case "Windows 7":
                lastPath = new File(System.getenv("user.dir"));
                break;
            case "Linux":
            default:
                lastPath = new File(System.getenv("PWD"));
        }
    }

    /**
     * Opens a dialog for the users to select a File from local directory.
     *
     * @param title Dialog title.
     * @param description Description of file type.
     * @param filters Regular expressions to filter file types (*.extension).
     * @return A File with user selected file, or null if user canceled.
     */
    public static File openFile(String title, String description, String[] filters) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        fileChooser.setInitialDirectory(lastPath);
        if (description != null && filters != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(description, filters));
        }
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            return file;
        }
        return null;
    }

    /**
     * Opens a dialog for the user to select a file and sets textField text to
     * selected file name. If FileChooser is canceled, textField is not
     * modified.
     *
     * @param title FileChooser Title.
     * @param filterDesc Short description of the file filter.
     * @param filters List of regular expressions to filter.
     * @param textField The textField to modify.
     * @return The chosen file or null if the operation was canceled.
     */
    public static File openFile(String title, String filterDesc, String[] filters,
            TextField textField) {
        File file = openFile(title, filterDesc, filters);
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Shows a dialog to the user to select a FASTA file (.fa or .fasta). Sets
     * the text of the TextField to the name of the file.
     *
     * @return The file selected or null if user canceled.
     */
    public static File openTSV() {
        return openFile(TSV_DESCRIPTION, TSV_EXTENSION, TSV_FILTERS);
    }

    /**
     * Shows a dialog to the user to select a FASTA file (.fa or .fasta). Sets
     * the text of the TextField to the name of the file.
     *
     * @param window
     * @return The file selected or null if user canceled.
     */
    public static File openTSV(Window window) {
        return openFile(TSV_DESCRIPTION, TSV_EXTENSION, TSV_FILTERS, window);
    }

    /**
     * Opens a dialog for the user to create a File. File system file is not
     * created immediately. The File is just passed to one of the Workers. If
     * the Worker ends successfully, then the file will have been created.
     *
     * @param title Dialog title
     * @param filterDesc Description of file type
     * @param filters Regular expressions to filter file types (*.extension)
     * @param extension default extension
     * @return A File with the user selected file, or null if not file selected
     */
    public static File saveFile(String title, String filterDesc, String[] filters, String extension) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        fileChooser.setInitialDirectory(lastPath);
        if (filters != null && filterDesc != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(filterDesc, filters));
        }
        File file = fileChooser.showSaveDialog(TSVReader.getMainWindow());
        if (file != null) {
            lastPath = file.getParentFile();
            // Add extension to bad named files
            return file.getAbsolutePath().endsWith(extension) ? file : new File(
                    file.getAbsolutePath() + extension);
        }
        return null;
    }

    /**
     * Opens a dialog for the user to create a file and sets the text of the
     * TextField to the file name. File system file is not created immediately.
     * The File is just passed to one of the Workers. If the Worker ends
     * successfully, then the file will have been created.
     *
     * @param title Dialog title
     * @param filterDesc Description of file type
     * @param filters Regular expressions to filter file types (*.extension)
     * @param extension Default extension
     * @param textField textField associated to the file
     */
    public static void saveFile(String title, String filterDesc, String[] filters, String extension,
            TextField textField) {
        File file = saveFile(title, filterDesc, filters, extension);
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Opens a dialog for the user to create a Tabular Separated Vaules file
     * (.tsv). The file is not created immediately, just stored as text.
     *
     * @param textField textField containig TSV file name.
     */
    public static void saveTSV(TextField textField) {
        saveFile(TSV_DESCRIPTION, TSV_DESCRIPTION, TSV_FILTERS, TSV_EXTENSION, textField);
    }

    static File saveTSV() {
        return saveFile(TSV_DESCRIPTION, TSV_DESCRIPTION, TSV_FILTERS, TSV_EXTENSION);
    }

    /**
     * Opens a Dialog to select a folder.
     *
     * @param title The title for the DirectoryChooser.
     * @return A File or null if user canceled.
     */
    public static File selectFolder(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (title != null) {
            chooser.setTitle(title);
        }
        chooser.setInitialDirectory(lastPath);
        File file = chooser.showDialog(null);
        return (file != null) ? (lastPath = file) : null;
    }

    /**
     * Launches the default system web browser and opens the specified url.
     *
     * @param url URL to visit.
     */
    public static void browse(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (URISyntaxException ex) {
            System.err.println("Bad URL");
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("URN not found");
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

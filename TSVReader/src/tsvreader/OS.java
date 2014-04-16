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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Contains static methods to open an save files.
 *
 * @author Pascual Lorente Arencibia
 */
public class OS {

    /**
     * The last path.
     */
    private static File lastPath;

    /**
     * The TSV description.
     */
    private static final String TSV_DESCRIPTION = "Tabular Separated Values";
    /**
     * The TSV extension.
     */
    private static final String TSV_EXTENSION = ".tsv";
    /**
     * The TSV filters (.tsv and .txt).
     */
    private static final String[] TSV_FILTERS = new String[]{"*.tsv", "*.txt"};

    /**
     * OS only has static methods and do not stores anything, so don't try to create an instance.
     */
    OS() {
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
     * Shows a dialog to the user to select a TSV file (.tsv or .txt).
     *
     * @param window The window to block during file selection.
     * @return The file selected or null if user canceled.
     */
    public static File openTSV(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(TSV_DESCRIPTION);
        fileChooser.setInitialDirectory(lastPath);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(TSV_EXTENSION, TSV_FILTERS));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All formats", "*"));
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            lastPath = file.getParentFile();
            return file;
        }
        return null;
    }

    /**
     * Opens a dialog to the user to save the data. If the users does not write an extension, a .tsv
     * extension will be added.
     *
     * @param window the window to block during file creation.
     * @return a file with the user creation, or null if canceled.
     */
    static File saveTSV(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(TSV_DESCRIPTION);
        fileChooser.setInitialDirectory(lastPath);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(TSV_DESCRIPTION, TSV_FILTERS));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All formats", "*"));
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            lastPath = file.getParentFile();
            if (file.getName().contains(".")) {
                return file;
            }
            // Add extension to bad named files
            return file.getAbsolutePath().endsWith(TSV_EXTENSION) ? file : new File(
                    file.getAbsolutePath() + TSV_EXTENSION);
        }
        return null;
    }

    /**
     * Opens a BufferedReader to read a simple text file.
     *
     * @param input the file.
     * @return a BufferedReader
     * @throws FileNotFoundException
     */
    public static BufferedReader openTextBR(File input) throws FileNotFoundException {
        return new BufferedReader(new FileReader(input));
    }

    /**
     * Opens a BufferedWriter
     *
     * @param output
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static BufferedWriter openTextBW(File output) throws FileNotFoundException, IOException {
        return new BufferedWriter(new FileWriter(output));
    }

    /**
     * Opens a BufferedReader to read a simple text file.
     *
     * @param input the file.
     * @return a BufferedReader
     * @throws FileNotFoundException
     */
    public static BufferedReader openTextBR(String input) throws FileNotFoundException {
        return new BufferedReader(new FileReader(input));
    }

    /**
     * Opens a BufferedWriter
     *
     * @param output
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static BufferedWriter openTextBW(String output) throws FileNotFoundException, IOException {
        return new BufferedWriter(new FileWriter(output));
    }
}

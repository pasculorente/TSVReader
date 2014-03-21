/*
 * Copyright (C) 2014 uichuimi03
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class only has one static method. This is the antonym of Parser. Using the headers and the
 * cachedRows from the Dataset, it will create or override a file with the data.
 *
 * @author Pascual Lorente Arencibia
 */
public class Saver {

    /**
     * Saves the data inside the dataset in the specified file. The first row will have the column
     * names (header.getOrigin()) separated by tabulators. The data will be also stored separated by
     * tabulators.
     *
     * @param dataset The dataset to store. only cachedRows will be stored.
     * @param file The file where to store the data.
     */
    public static void save(Dataset dataset, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Write headers
            for (int i = 0; i < dataset.getHeaders().size() - 1; i++) {
                bw.write(dataset.getHeaders().get(i).getOrigin() + "\t");
            }
            bw.write(dataset.getHeaders().get(dataset.getHeaders().size() - 1).getOrigin());
            bw.newLine();
            // Write rows
            for (String[] row : dataset.getCachedRows()) {
                for (int i = 0; i < dataset.getHeaders().size() - 1; i++) {
                    bw.write(row[i] + "\t");
                }
                bw.write(row[row.length - 1]);
                bw.newLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Saver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Saver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

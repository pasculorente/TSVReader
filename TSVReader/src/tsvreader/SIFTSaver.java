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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A special saver for SIFT files. There is only a mismatch, in the coordinates field, it only puts
 * a 1 for the 3rd value.
 *
 * @author Pascual Lorente Arencibia
 */
class SIFTSaver {

    /**
     * Takes a dataset representing a SIFT SNP file, and stores it trying to conserve the SIFT file
     * structure.
     *
     * @param dataset a dataset containing a SIFT file representation.
     * @param f the file where to save the data.
     */
    static void save(Dataset dataset, File f) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            // Write headers
            bw.write("Coordinates\t");
            for (int i = 0; i < dataset.getHeaders().size() - 1; i++) {
                if (!dataset.getHeaders().get(i).getOrigin().equals("Coordinates")) {
                    bw.write(dataset.getHeaders().get(i).getOrigin() + "\t");
                }
            }
            bw.write(dataset.getHeaders().get(dataset.getHeaders().size() - 1).getOrigin());
            bw.newLine();
            // Write rows
            for (String[] row : dataset.getCachedRows()) {
                bw.write(row[0] + "," + row[1] + ",1," + row[2] + "/" + row[3] + "\t");
                for (int i = 4; i < dataset.getHeaders().size() - 1; i++) {
                    if (dataset.getHeaders().get(i).getOrigin().contains("Allele Freqs") && !row[i].
                            isEmpty()) {
                        String[] freqs = row[i].split(",");
                        bw.write(row[3] + "," + freqs[0] + ":" + row[2] + "," + freqs[1] + "\t");
                    } else if (dataset.getHeaders().get(i).getOrigin().contains("Allele Freq.")
                            && !row[i].isEmpty()) {
                        String[] freqs = row[i].split(",");
                        bw.write(row[2] + ":" + freqs[0] + "," + row[3] + ":" + freqs[1] + "\t");
                    } else {
                        bw.write(row[i] + "\t");
                    }
                }
                bw.write(row[row.length - 1]);
                bw.newLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SIFTSaver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SIFTSaver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

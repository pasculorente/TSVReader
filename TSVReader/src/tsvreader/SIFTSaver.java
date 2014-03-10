package tsvreader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia
 */
class SIFTSaver {

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
            Logger.getLogger(TSVData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TSVData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
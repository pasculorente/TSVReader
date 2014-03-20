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

import java.util.List;

/**
 * A specific Parser for SIFT files. As SIFT files have several particularities, they need a special
 * parsing. First, Coordinates column mixes Chromosomes, position, reference and alternate, and they
 * should be separated to improve filtering. Second, frequencies are stored as T:0.001, A:0.999 in
 * some columns but as T,0.001:A,0.999 in others. All letters, (,) and (:) are deleted, leaving the
 * field as 0.001,0,999.
 *
 * @author Pascual Lorente Arencibia
 */
class SIFTParser extends Parser {

    /**
     * The name of the headers file.
     */
    private final static String HEADERS_FILE = "tsv_files/sift_snp.header";

    /**
     * Creates a SIFT parser. As the name of the headers is always the same, it is not needed to
     * select it.
     *
     * @param file The name of the input file.
     */
    public SIFTParser(String file) {
        super(file, HEADERS_FILE);
    }

    /**
     * Parses a single line. The coordinates field is separated into CHROM, POS, REF and ALT. The
     * frequencies fields are normalized to: value1,value2.
     *
     * @param inHeaders Columns as they are in the file.
     * @param line The splitted line.
     * @param outHeaders Headers as read from headers file.
     * @return an array with the fields for the dataset.
     */
    @Override
    String[] parse(String[] inHeaders, String[] line, List<Header> outHeaders) {
        int coordinateIndex = indexOf(inHeaders, "coordinates");
        String[] ses = null;
        if (coordinateIndex != -1) {
            ses = line[coordinateIndex].split(",");
        } else {
            System.err.println("Warning: Coordinates column is not present.");
        }
        String[] outLine = new String[outHeaders.size()];
        for (int i = 0; i < outLine.length; i++) {
            outLine[i] = "";
        }
        for (int i = 0; i < outHeaders.size(); i++) {
            switch (outHeaders.get(i).getName().toLowerCase()) {
                case "chrom":
                    if (coordinateIndex != -1) {
                        outLine[i] = ses[0];
                    }
                    break;
                case "pos":
                    if (coordinateIndex != -1) {
                        outLine[i] = ses[1];
                    }
                    break;
                case "ref":
                    if (coordinateIndex != -1) {
                        outLine[i] = ses[3].split("/")[0];
                    }
                    break;
                case "alt":
                    if (coordinateIndex != -1) {
                        outLine[i] = ses[3].split("/")[1];
                    }
                    break;
                default:
                    int from = indexOf(inHeaders, outHeaders.get(i).getOrigin());
                    if (outHeaders.get(i).getOrigin().toLowerCase()
                            .contains(" allele freq")) {
                        if (!line[from].isEmpty()) {
                            String[] refalt = line[from].split("[,:]");
                            outLine[i] = refalt[1] + "," + refalt[3];
                        }
                    } else {
                        try {
                            outLine[i] = line[from];
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    }
            }
        }
        return outLine;
    }
}

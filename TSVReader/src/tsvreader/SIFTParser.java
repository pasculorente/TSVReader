package tsvreader;

import java.util.List;

/**
 *
 * @author Pascual Lorente Arencibia
 */
class SIFTParser extends Parser {

    private final static String HEADERS_FILE = "tsv_files/sift_snp.header";

    public SIFTParser(String file) {
        super(file, HEADERS_FILE);
    }

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

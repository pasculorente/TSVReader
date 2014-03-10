package tsvreader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class DatasetTest {

    public DatasetTest() {
    }

    @Test
    public void testSomeMethod() {
        List<Header> headers = new ArrayList<>();
        String origin = "Chr";
        String name = "chr";
        String type = "numeric";
        String desc = "Chromosome";
        String parent = null;
        headers.add(new Header(origin, name, type, desc, parent));
        String[] row = new String[]{"1"};
        List<String[]> rows = new ArrayList<>();
        rows.add(row);
        Dataset dataset = new Dataset(headers, rows);
        assertEquals(headers, dataset.getHeaders());
        assertEquals(rows, dataset.getRows());
    }

    @Test
    public void testFilterNumeric() {
        Dataset d = null;
        try {
            d = new Parser("tsv_files/annovar.txt", "tsv_files/annovar.header").call();
        } catch (Exception ex) {
            Logger.getLogger(DatasetTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertNotNull(d);
        List<String[]> filteredRows = d.filterNumeric(true, 15, 0, 0.5);
        assertEquals(244, filteredRows.size());
    }

    @Test
    public void testFilterText() {
        Dataset d = null;
        try {
            d = new Parser("tsv_files/annovar.txt", "tsv_files/annovar.header").call();
        } catch (Exception ex) {
            Logger.getLogger(DatasetTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertNotNull(d);
        assertEquals(240, d.filterText(false, 0, false, "1").size());
        assertEquals(33, d.filterText(false, 0, true, "1").size());

    }
}

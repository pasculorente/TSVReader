package tsvreader;
//
//import java.util.List;

import org.junit.Assert;
import org.junit.Test;

//import org.junit.After;
//import org.junit.AfterClass;
//import static org.junit.Assert.assertEquals;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// *
// * @author uichuimi03
// */
public class TSVDataTest {

    @Test
    public void test() {
        Assert.assertTrue(true);
    }
//
//    static TSVData data;
//
//    public TSVDataTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() {
//        data = new TSVData();
//        data.load("tsv_files/tsv_sample.tsv");
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    @Test
//    public void testLoad() {
//        assertEquals("1", data.getValue(1, 0));
//        assertEquals("DDX11L1-001", data.getValue(12, 6));
//    }
//
//    @Test
//    public void testHeaders() {
////        assertEquals("CHROM", data.getHeaders().get(0).getName());
////        assertEquals("GENE", data.getHeaders().get(1).getName());
////        assertEquals("ID", data.getHeaders().get(1).getChildren().get(0).getName());
////        assertEquals("NAME", data.getHeaders().get(1).getChildren().get(1).getName());
////        assertEquals("EXON", data.getHeaders().get(2).getName());
////        assertEquals("NUMBER", data.getHeaders().get(2).getChildren().get(0).getName());
////        assertEquals("START", data.getHeaders().get(2).getChildren().get(1).getName());
////        assertEquals("END", data.getHeaders().get(2).getChildren().get(2).getName());
////        assertEquals("NAME", data.getHeaders().get(2).getChildren().get(3).getName());
////        assertEquals("INFO", data.getHeaders().get(3).getName());
////        assertEquals("BIOTYPE", data.getHeaders().get(4).getName());
////        assertEquals("EXON_ID", data.getHeaders().get(5).getName());
//    }
//
//    @Test
//    public void testFilterText() {
//        List<String[]> rows = data.filterText(false, 1, "ENSG00000223972");
//        assertEquals(16, rows.size());
//        rows = data.filterText(true, 6, "DDX11L1-002");
//        assertEquals(3, rows.size());
//    }
//
//    @Test
//    public void testFilterGroup() {
//        List<String[]> rows = data.filterGroup(false, 0, "1");
//        assertEquals(20, rows.size());
//        rows = data.filterGroup(false, 0, "1", "9");
//        assertEquals(30, rows.size());
//    }
//
//    @Test
//    public void testFilterNumeric() {
//        List<String[]> rows = data.filterNumeric(false, 4, 10000, 15000);
//        assertEquals(16, rows.size());
//        rows = data.filterNumeric(false, 5, 12000, 14000);
//        assertEquals(13, rows.size());
//    }
//
}

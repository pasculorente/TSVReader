package tsvreader;
//
//import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

//import org.junit.Assert;
//import static org.junit.Assert.assertEquals;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// *
// * @author Pascual Lorente Arencibia
// */
public class ParserTest {
//
//    static Parser parser;
//

    public ParserTest() {
    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

    @Test
    public void testParse() {
        String filename = "tsv_files/annovar.txt";
        Dataset d = null;
        try {
            d = new Parser(filename, "tsv_files/annovar.header").call();
        } catch (Exception ex) {
            Logger.getLogger(ParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Assert.assertNotNull(d);
        assertEquals(27, d.getHeaders().size());
        assertEquals(394, d.getRows().size());
    }

}

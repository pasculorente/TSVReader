package tsvreader;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class HeaderTest {

    Header h;

    public HeaderTest() {
    }

    @Before
    public void initHeader() {
        h = new Header("Chr", "", "chr", "text", "Chromosome");
    }

    /**
     * Test of getDescription method, of class Header.
     */
    @Test
    public void testGetDescription() {
        assertEquals("chr", h.getName());
    }

    /**
     * Test of getName method, of class Header.
     */
    @Test
    public void testGetName() {
        assertEquals("text", h.getType());
    }

    /**
     * Test of getParent method, of class Header.
     */
    @Test
    public void testGetParent() {
        assertEquals("Chromosome", h.getDescription());
    }

    /**
     * Test of getType method, of class Header.
     */
    @Test
    public void testGetType() {
        assertEquals("", h.getParent());
    }

    /**
     * Test of toString method, of class Header.
     */
    @Test
    public void testToString() {
    }

}

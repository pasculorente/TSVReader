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
        h = new Header("Chr", "text", "Chromosome", "");
    }

    /**
     * Test of getDescription method, of class Header.
     */
    @Test
    public void testGetDescription() {
        assertEquals("", h.getDescription());
    }

    /**
     * Test of getName method, of class Header.
     */
    @Test
    public void testGetName() {
        assertEquals("Chromosome", h.getName());
    }

    /**
     * Test of getType method, of class Header.
     */
    @Test
    public void testGetType() {
        assertEquals("text", h.getType());
    }

    /**
     * Test of toString method, of class Header.
     */
    @Test
    public void testToString() {
    }

}

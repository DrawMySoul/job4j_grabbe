import org.junit.Test;
import ru.job4j.Grabber;

import static org.junit.Assert.*;

public class GrabberTest {
    @Test
    public void defaultTest() {
        assertEquals(1, Grabber.doSth());
    }
}
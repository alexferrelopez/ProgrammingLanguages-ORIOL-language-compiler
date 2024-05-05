import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void testNoArguments() {
        String[] args = {};
        Main.main(args);
        assertEquals(Main.NO_ARGS_ERROR + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testTooManyArguments() {
        String[] args = {"file1.farm", "file2.farm"};
        Main.main(args);
        assertEquals(Main.TOO_MANY_ARGS_ERROR + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testInvalidFileExtension() {
        String[] args = {"file1.txt"};
        Main.main(args);
        assertEquals(Main.INVALID_EXTENSION_ERROR + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testValidArguments() {
        String[] args = {"files/Exemple1.farm"};    // File must exist.

        Main.main(args);

        // Check if the output is the expected.
        assertTrue(outContent.toString().isEmpty());
    }
}
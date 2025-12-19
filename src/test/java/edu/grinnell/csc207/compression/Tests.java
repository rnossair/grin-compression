package edu.grinnell.csc207.compression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class Tests {
    @Test
    public void basic() throws IOException{
        Grin.encode("./testFiles/test.txt", "./testFiles/test_output.grin");
        Grin.decode("./testFiles/test_output.grin", "./testFiles/test_output.txt");
        assertEquals(-1, Files.mismatch(Paths.get("./testFiles/test.txt"), Paths.get("./testFiles/test_output.txt")));
    }
}

package util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeGeneratorTest {

    private static final String VALID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // Basic validation
    @Test
    void testGenerateLengthIsSix() {
        String code = QRCodeGenerator.generate();
        assertEquals(6, code.length());
    }

    @Test
    void testGenerateNotNull() {
        String code = QRCodeGenerator.generate();
        assertNotNull(code);
    }

    @Test
    void testGenerateNotEmpty() {
        String code = QRCodeGenerator.generate();
        assertFalse(code.isEmpty());
    }

    // Character validation
    @Test
    void testGenerateContainsOnlyValidCharacters() {
        String code = QRCodeGenerator.generate();

        for (char c : code.toCharArray()) {
            assertTrue(VALID_CHARS.indexOf(c) >= 0,
                    "Invalid character found: " + c);
        }
    }

    @Test
    void testGenerateNoLowercaseLetters() {
        String code = QRCodeGenerator.generate();

        for (char c : code.toCharArray()) {
            assertFalse(Character.isLowerCase(c));
        }
    }

    @Test
    void testGenerateNoSpecialCharacters() {
        String code = QRCodeGenerator.generate();

        assertTrue(code.matches("[A-Z2-9]{6}"));
    }

    // Randomness validation
    @Test
    void testGenerateRandomness() {
        String code1 = QRCodeGenerator.generate();
        String code2 = QRCodeGenerator.generate();
        assertNotEquals(code1, code2);
    }

    @Test
    void testGenerateUniquenessOverManyRuns() {
        Set<String> codes = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            codes.add(QRCodeGenerator.generate());
        }

        assertTrue(codes.size() > 190);
    }

    // Edge case validation
    @Test
    void testGenerateConsistentLengthEveryTime() {
        for (int i = 0; i < 50; i++) {
            assertEquals(6, QRCodeGenerator.generate().length());
        }
    }
}
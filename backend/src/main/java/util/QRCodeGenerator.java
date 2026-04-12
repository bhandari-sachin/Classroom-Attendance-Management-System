package util;

import java.security.SecureRandom;

/**
 * Utility class for generating random QR-like codes.
 * The generated code consists of uppercase letters and digits,
 * excluding ambiguous characters for better readability.
 */
public class QRCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random alphanumeric code.
     * @return a 6-character string composed of uppercase letters and digits
     */
    public static String generate() {
        StringBuilder codeBuilder = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(index));
        }

        return codeBuilder.toString();
    }

    /**
     * Generates a random code of custom length.
     * @param length the desired length of the code
     * @return a random alphanumeric string
     */
    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Code length must be positive");
        }

        StringBuilder codeBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(index));
        }

        return codeBuilder.toString();
    }
}

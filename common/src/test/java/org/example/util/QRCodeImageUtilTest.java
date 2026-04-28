package org.example.util;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeImageUtilTest {

    @Test
    void generateQRCodeImageShouldReturnBufferedImage() throws Exception {
        BufferedImage image =
                QRCodeImageUtil.generateQRCodeImage("hello", 200, 200);

        assertNotNull(image);
        assertTrue(image.getWidth() > 0);
        assertTrue(image.getHeight() > 0);
    }

    @Test
    void generateQRCodeImageShouldWorkWithLongText() throws Exception {
        String text = "someverylongtextfortesting";

        BufferedImage image =
                QRCodeImageUtil.generateQRCodeImage(text, 300, 300);

        assertNotNull(image);
        assertEquals(300, image.getWidth());
        assertEquals(300, image.getHeight());
    }
}
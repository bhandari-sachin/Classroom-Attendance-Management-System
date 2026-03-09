package org.example.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.image.BufferedImage;

public class QRCodeImageUtil {

    public static BufferedImage generateQRCodeImage(String text, int width, int height) {
        try {

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

            return MatrixToImageWriter.toBufferedImage(matrix);

        } catch (Exception e) {
            throw new RuntimeException("QR generation failed", e);
        }
    }
}
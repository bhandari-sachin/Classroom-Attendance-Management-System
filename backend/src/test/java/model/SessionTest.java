package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void testConstructorAndGetters() {
        LocalDate date = LocalDate.of(2026, 1, 1);

        Session session = new Session(1L, 100L, date, "QR123");

        assertEquals(1L, session.getId());
        assertEquals(100L, session.getClassId());
        assertEquals(date, session.getSessionDate());
        assertEquals("QR123", session.getQrCode());
    }

    @Test
    void testSetQrCode() {
        LocalDate date = LocalDate.of(2026, 1, 1);

        Session session = new Session(1L, 100L, date, "OLD_QR");

        session.setQRCode("NEW_QR");

        assertEquals("NEW_QR", session.getQrCode());
    }

    @Test
    void testQrCodeUpdateMultipleTimes() {
        Session session = new Session(
                2L,
                200L,
                LocalDate.of(2026, 2, 2),
                "QR1"
        );

        session.setQRCode("QR2");
        assertEquals("QR2", session.getQrCode());

        session.setQRCode("QR3");
        assertEquals("QR3", session.getQrCode());
    }

    @Test
    void testImmutabilityOfOtherFields() {
        LocalDate date = LocalDate.of(2026, 3, 3);

        Session session = new Session(10L, 20L, date, "QRX");

        assertAll(
                () -> assertEquals(10L, session.getId()),
                () -> assertEquals(20L, session.getClassId()),
                () -> assertEquals(date, session.getSessionDate())
        );
    }
}
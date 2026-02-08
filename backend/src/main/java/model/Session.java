package model;

import java.sql.Date;
import java.time.LocalDate;

public class Session {

    private Long sessionId;
    private Long classId;
    private LocalDate date;
    private String QRCode;

    public Session(Long sessionId, Long classId, LocalDate date, String qrToken) {
        this.sessionId = sessionId;
        this.classId = classId;
        this.date = date;
    }

    public Session(Long id, Long classId, Date sessionDate, String qrCode) {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }
}


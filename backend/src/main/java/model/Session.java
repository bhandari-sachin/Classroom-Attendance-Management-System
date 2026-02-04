package model;

import java.time.LocalDate;

public class Session {

    private Long sessionId;
    private Long classId;
    private LocalDate date;
    private String QRCode;

    public Session(Long sessionId, Long classId, LocalDate date) {
        this.sessionId = sessionId;
        this.classId = classId;
        this.date = date;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }
}


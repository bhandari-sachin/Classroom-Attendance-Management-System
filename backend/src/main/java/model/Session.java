package model;

import java.time.LocalDate;

public class Session {

    private final long id;
    private final long classId;
    private final LocalDate sessionDate;
    private String qrCode;

    public Session(long id, long classId, LocalDate sessionDate, String qrCode) {
        this.id = id;
        this.classId = classId;
        this.sessionDate = sessionDate;
        this.qrCode = qrCode;
    }

    public long getId() {
        return id;
    }

    public long getClassId() {
        return classId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public String getQrCode() {
        return qrCode;
    }
    public void setQRCode(String qrCode) {
        this.qrCode = qrCode;
    }
}


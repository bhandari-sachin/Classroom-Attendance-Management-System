package model;

import java.time.LocalDate;

public class Session {

    private final long id;
    private final long classId;
    private final LocalDate sessionDate;
    private String QRCode;

    public Session(long id, long classId, LocalDate sessionDate, String QRCode) {
        this.id = id;
        this.classId = classId;
        this.sessionDate = sessionDate;
        this.QRCode = QRCode;
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
        return QRCode;
    }
    public void setQRCode(String QRCode) {
        this.QRCode = QRCode;
    }
}
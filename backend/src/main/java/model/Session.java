package model;

import java.time.LocalDate;

/**
 * Represents a class session within the academic system.
 * Each session is linked to a specific class and date,
 * and may include a QR code for attendance tracking.
 */
public class Session {

    private final long id;
    private final long classId;
    private final LocalDate sessionDate;
    private String qrCode;

    /**
     * Constructs a Session with all required details.
     * @param id the unique identifier of the session
     * @param classId the ID of the class associated with this session
     * @param sessionDate the date when the session occurs
     * @param qrCode the QR code used for attendance tracking
     */
    public Session(long id, long classId, LocalDate sessionDate, String qrCode) {
        this.id = id;
        this.classId = classId;
        this.sessionDate = sessionDate;
        this.qrCode = qrCode;
    }

    // --- Getters ---

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

    // --- Setters ---

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    // --- Utility Methods ---

    /**
     * Checks if the session date is today.
     * @return true if the session date matches the current date
     */
    public boolean isToday() {
        return LocalDate.now().equals(sessionDate);
    }

    /**
     * Returns a readable summary of the session.
     * @return formatted string with session details
     */
    @Override
    public String toString() {
        return String.format(
                "Session[id=%d, classId=%d, sessionDate=%s, qrCode=%s]",
                id, classId, sessionDate, qrCode
        );
    }
}

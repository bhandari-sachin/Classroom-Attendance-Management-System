package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Session {

    private Long id;
    private Long classId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String topic;
    private String qrToken;
    private String status;

    public Session(Long id, Long classId, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, String qrToken, String topic, String status) {
        this.id = id;
        this.classId = classId;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.qrToken = qrToken;
        this.topic = topic;
        this.status = status;
    }

    public Long getId() {
        return id;
    }
    public Long getClassId() { return classId; }
    public LocalDate getSessionDate() { return sessionDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getTopic() { return topic; }
    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
    public String getStatus() { return status; }

}


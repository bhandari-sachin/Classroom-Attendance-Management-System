package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Session {

    private Long sessionId;
    private Long classId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public Session(Long classId, LocalDate date) {
        this.classId = classId;
        this.date = date;
    }

    public Long getSessionId() {
        return sessionId;
    }
}


package service;

import model.Session;
import util.QRCodeGenerator;
import config.SessionSQL;
import java.util.List;

public class SessionService {

    private final SessionSQL sessionSQL;

    public SessionService(SessionSQL sessionSQL) {
        this.sessionSQL = sessionSQL;
    }

    public List<Session> getSessionsByClassId(Long classId) {
        return sessionSQL.findByClassId(classId);
    }

    public String startSession(Long sessionId) {
        String qrCode = QRCodeGenerator.generate();
        sessionSQL.activateSession(sessionId, qrCode);
        return qrCode;
    }
    public void endSession(Long sessionId) {
        sessionSQL.completeSession(sessionId);
    }
    public void cancelSession(Long sessionId) {
        sessionSQL.updateStatus(sessionId, "CANCELED");
    }
}
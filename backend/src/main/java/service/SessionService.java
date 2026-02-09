package service;

import util.QRCodeGenerator;
import config.SessionSQL;

public class SessionService {

    private final SessionSQL sessionSQL;

    public SessionService(SessionSQL sessionSQL) {
        this.sessionSQL = sessionSQL;
    }

    public String startSession(Long sessionId) {
        String code = QRCodeGenerator.generate();
        sessionSQL.updateQRCode(sessionId, code);
        return code;
    }
}

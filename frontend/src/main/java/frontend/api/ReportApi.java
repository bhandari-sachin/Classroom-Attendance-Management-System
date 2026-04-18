package frontend.api;

import frontend.auth.AuthState;
import frontend.auth.JwtStore;
import frontend.i18n.FrontendI18n;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ReportApi {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;

    public ReportApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String token(JwtStore store, AuthState state) {
        return store.load().map(AuthState::getToken).orElse(state.getToken());
    }

    public void exportStudentPdf(JwtStore store, AuthState state, String destPath) throws Exception {
        String url = baseUrl + "/api/reports/export/student?format=pdf&lang=" + FrontendI18n.getLanguage();
        download(url, token(store, state), destPath);
    }

    public void exportTeacherReport(JwtStore store, AuthState state, long classId, String format, String destPath) throws Exception {
        String url = baseUrl + "/api/reports/export/teacher?classId=" + classId + "&format=" + format+ "&lang=" + FrontendI18n.getLanguage();
        download(url, token(store, state), destPath);
    }

    public void exportAdminReport(JwtStore store, AuthState state, String format, String destPath) throws Exception {
        String url = baseUrl + "/api/reports/export/admin?format=" + format + "&lang=" + FrontendI18n.getLanguage();
        download(url, token(store, state), destPath);
    }

    private void download(String url, String token, String destPath) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<InputStream> res = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

        if (res.statusCode() >= 400) {
            String body;
            try (InputStream err = res.body()) {
                body = new String(err.readAllBytes());
            }
            throw new RuntimeException("Export failed (HTTP " + res.statusCode() + "): " + body);
        }

        try (InputStream in = res.body();
             FileOutputStream out = new FileOutputStream(destPath)) {
            in.transferTo(out);
        }
    }
}


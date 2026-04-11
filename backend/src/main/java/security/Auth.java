package security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;

public class Auth {
    private Auth () {}

    public static DecodedJWT requireJwt(HttpExchange ex, JwtService jwtService) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new SecurityException("Missing Authorization header");
        }
        String token = auth.substring("Bearer ".length());
        return jwtService.verify(token);
    }

    public static void requireRole(DecodedJWT jwt, String... roles) {
        String role = jwt.getClaim("role").asString();
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new SecurityException("Forbidden for role: " + role);
    }
}
package security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.util.Date;

public class JwtService {

    private final Algorithm algo;
    private final String issuer = "attendance-backend";

    public JwtService(String secret) {
        this.algo = Algorithm.HMAC256(secret);
    }

    public String issueToken(long userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(60 * 60); // 1 hour

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(String.valueOf(userId))
                .withClaim("email", email)
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algo);
    }

    public DecodedJWT verify(String token) {
        return JWT.require(algo)
                .withIssuer(issuer)
                .build()
                .verify(token);
    }
}
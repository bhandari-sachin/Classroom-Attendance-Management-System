package security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final String secret = "test-secret";
    private final JwtService jwtService = new JwtService(secret);

    @Test
    void issueTokenShouldContainCorrectClaims() {
        String token = jwtService.issueToken(
                1L,
                "test@test.com",
                "STUDENT"
        );

        assertNotNull(token);

        DecodedJWT decoded = jwtService.verify(token);

        assertEquals("attendance-backend", decoded.getIssuer());
        assertEquals("1", decoded.getSubject());

        assertEquals(1L, decoded.getClaim("id").asLong());
        assertEquals("test@test.com", decoded.getClaim("email").asString());
        assertEquals("STUDENT", decoded.getClaim("role").asString());
    }

    @Test
    void verifyShouldReturnDecodedJWTIfTokenValid() {
        String token = jwtService.issueToken(2L, "a@b.com", "ADMIN");

        DecodedJWT decoded = jwtService.verify(token);

        assertEquals("2", decoded.getSubject());
    }

    @Test
    void verifyShouldThrowIfTokenInvalid() {
        JwtService service = new JwtService(secret);

        String invalidToken = "invalid.token.value";

        assertThrows(Exception.class, () -> service.verify(invalidToken));
    }

    @Test
    void verifyShouldThrowIfWrongSecret() {
        JwtService service1 = new JwtService("secret1");
        JwtService service2 = new JwtService("secret2");

        String token = service1.issueToken(1L, "x@y.com", "STUDENT");

        assertThrows(Exception.class, () -> service2.verify(token));
    }

    @Test
    void tokenShouldHaveExpiration() {
        String token = jwtService.issueToken(1L, "test@test.com", "STUDENT");

        DecodedJWT decoded = jwtService.verify(token);

        assertNotNull(decoded.getExpiresAt());
        assertTrue(decoded.getExpiresAt().after(decoded.getIssuedAt()));
    }
}
package security;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthTest {

    // requireJwt
    @Test
    void requireJwtShouldThrowIfHeaderMissing() {
        HttpExchange ex = mock(HttpExchange.class);
        JwtService jwtService = mock(JwtService.class);

        Headers headers = new Headers();
        when(ex.getRequestHeaders()).thenReturn(headers);

        assertThrows(SecurityException.class,
                () -> Auth.requireJwt(ex, jwtService));
    }

    @Test
    void requireJwtShouldThrowIfNotBearer() {
        HttpExchange ex = mock(HttpExchange.class);
        JwtService jwtService = mock(JwtService.class);

        Headers headers = new Headers();
        headers.add("Authorization", "Basic abc123");

        when(ex.getRequestHeaders()).thenReturn(headers);

        assertThrows(SecurityException.class,
                () -> Auth.requireJwt(ex, jwtService));
    }

    @Test
    void requireJwtShouldReturnDecodedJWTIfValid() {
        HttpExchange ex = mock(HttpExchange.class);
        JwtService jwtService = mock(JwtService.class);
        DecodedJWT jwt = mock(DecodedJWT.class);

        Headers headers = new Headers();
        headers.add("Authorization", "Bearer TOKEN123");

        when(ex.getRequestHeaders()).thenReturn(headers);
        when(jwtService.verify("TOKEN123")).thenReturn(jwt);

        DecodedJWT result = Auth.requireJwt(ex, jwtService);

        assertEquals(jwt, result);
        verify(jwtService).verify("TOKEN123");
    }

    // requireRole
    @Test
    void requireRoleShouldPassIfRoleMatches() {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("role")).thenReturn(claim);
        when(claim.asString()).thenReturn("ADMIN");

        assertDoesNotThrow(() ->
                Auth.requireRole(jwt, "ADMIN", "TEACHER"));
    }

    @Test
    void requireRoleShouldThrowIfRoleNotAllowed() {
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(jwt.getClaim("role")).thenReturn(claim);
        when(claim.asString()).thenReturn("STUDENT");

        assertThrows(SecurityException.class,
                () -> Auth.requireRole(jwt, "ADMIN", "TEACHER"));
    }
}
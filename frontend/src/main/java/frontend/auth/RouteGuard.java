package frontend.auth;

import java.util.Set;

public class RouteGuard {

    public static void require(AuthState state, Set<Role> allowed) {
        if (state == null) throw new RuntimeException("Not authenticated");
        if (!allowed.contains(state.getRole())) {
            throw new RuntimeException("Forbidden for role: " + state.getRole());
        }
    }
}
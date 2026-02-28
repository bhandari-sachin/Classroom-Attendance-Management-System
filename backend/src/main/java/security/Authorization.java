package security;

import model.UserRole;

public final class Authorization {

    private Authorization() {}

    public static void require(UserRole... allowedRoles) {
        if (!SecurityContext.isAuthenticated()) {
            throw new SecurityException("User not logged in");
        }

        UserRole currentRole = SecurityContext.get().getRole();

        for (UserRole role : allowedRoles) {
            if (currentRole == role) {
                return;
            }
        }

        throw new SecurityException("Access denied for role: " + currentRole);
    }
}
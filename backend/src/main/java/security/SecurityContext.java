package security;

public final class SecurityContext {

    private static Session currentSession;

    private SecurityContext() {}

    public static void set(Session session) {
        currentSession = session;
    }

    public static Session get() {
        return currentSession;
    }

    public static void clear() {
        currentSession = null;
    }

    public static boolean isAuthenticated() {
        return currentSession != null;
    }
}
package frontend.auth;

public enum Role {
    ADMIN, TEACHER, STUDENT;

    public static Role fromString(String s) {
        if (s == null) return STUDENT;
        return Role.valueOf(s.trim().toUpperCase());
    }
}

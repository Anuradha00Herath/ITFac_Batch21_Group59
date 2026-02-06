package utils;

public class Config {
    public static final String BASE_URL = env("BASE_URL", "http://localhost:8080");

    // API endpoints (match your TS config.ts)
    public static final String API_LOGIN = env("API_LOGIN", "/api/auth/login");

    public static final String API_PLANTS_ALL = "/api/plants";




    public static Creds creds(String role) {
        if ("admin".equals(role)) return new Creds("admin", "admin123");
        return new Creds("testuser", "test123");
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    public record Creds(String username, String password) {}
}

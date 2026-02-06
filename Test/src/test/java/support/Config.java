package support;

public class Config {
    public static final String BASE_URL = env("BASE_URL", "http://localhost:8080");

    // API endpoints
    public static final String API_LOGIN = env("API_LOGIN", "/api/auth/login");
    public static final String API_CATEGORIES_ALL = env("API_CATEGORIES_ALL", "/api/categories");
    public static final String API_CATEGORY_BY_ID = env("API_CATEGORY_BY_ID", "/api/categories/{id}");

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
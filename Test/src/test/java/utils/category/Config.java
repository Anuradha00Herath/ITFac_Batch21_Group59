package utils.category;

public class Config {
    // Base URLs
    public static final String BASE_URL = env("BASE_URL", "http://localhost:8080");
    public static final String UI_BASE_URL = env("UI_BASE_URL", "http://localhost:8080");
    
    // UI paths
    public static final String UI_LOGIN = env("UI_LOGIN", "/ui/login");
    public static final String UI_DASHBOARD = env("UI_DASHBOARD", "/ui/dashboard");
    public static final String UI_CATEGORIES = env("UI_CATEGORIES", "/ui/categories");
    public static final String UI_CATEGORY_EDIT = env("UI_CATEGORY_EDIT", "/ui/categories/edit/{id}");
    // API endpoints
    public static final String API_LOGIN = env("API_LOGIN", "/api/auth/login");
    public static final String API_CATEGORIES_ALL = env("API_CATEGORIES_ALL", "/api/categories");
    public static final String API_CATEGORY_BY_ID = env("API_CATEGORY_BY_ID", "/api/categories/{id}");
    public static final String API_CATEGORIES_SUMMARY = env("API_CATEGORIES_SUMMARY", "/api/categories/summary");

    public static Creds creds(String role) {
        if ("admin".equals(role)) return new Creds("admin", "admin123");
        return new Creds("testuser", "test123");
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null ) ? def : v;
    }

    public record Creds(String username, String password) {}
}
package utils.sales;

import utils.EnvConfig;

public class Config {
    public static final String BASE_URL = env("BASE_URL", "http://localhost:8080");

    // API endpoints (match your TS config.ts)
    public static final String API_LOGIN = env("API_LOGIN", "/api/auth/login");
    public static String API_GET_PLANT(String id) { return "/api/plants/" + id; }
    public static final String API_SALES_ALL = env("API_SALES_ALL", "/api/sales");
    public static final String API_SALES_PAGE = env("API_SALES_PAGE", "/api/sales/page");
    public static String API_SALE_BY_ID(String id) { return "/api/sales/" + id; }

    static String admin_username = EnvConfig.get("ADMIN_USERNAME");
    static String admin_password = EnvConfig.get("ADMIN_PASSWORD");
    static String user_username = EnvConfig.get("USER_USERNAME");
    static String user_password = EnvConfig.get("USER_PASSWORD");



    // Swagger-based sell endpoint used in TS api steps
    public static String API_SELL_PLANT(Object plantId, int qty) {
        return "/api/sales/plant/" + String.valueOf(plantId) + "?quantity=" + qty;
    }

    public static Creds creds(String role) {
        if ("admin".equals(role)) return new Creds(admin_username, admin_password);
        return new Creds(user_username, user_password);
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    public record Creds(String username, String password) {}
}
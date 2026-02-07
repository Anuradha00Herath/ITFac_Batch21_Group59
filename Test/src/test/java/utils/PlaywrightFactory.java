package utils;

import com.microsoft.playwright.*;

public class PlaywrightFactory {
    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;
    private static String baseUrl;
    
    static {
        // Get base URL from system property or default
        baseUrl = System.getProperty("base.url", "http://localhost:8080");
        System.out.println("Using base URL: " + baseUrl);
    }
    
    public static Page getPage() {
        if (page == null) {
            initialize();
        }
        return page;
    }
    
    private static void initialize() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false) // Set to true in CI
                .setSlowMo(100)); // Slow down for visibility
        
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        
        page = context.newPage();
    }
    
    public static String getBaseUrl() {
        return baseUrl;
    }
    
    public static String getFullUrl(String path) {
        if (path.startsWith("http")) {
            return path;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }
    
    public static void close() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
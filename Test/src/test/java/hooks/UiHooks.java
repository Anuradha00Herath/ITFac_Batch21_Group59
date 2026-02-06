package hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class UiHooks {
    public static Page page;
    private static Browser browser;
    private static Playwright playwright;

    @Before("@ui")
    public void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    @After("@ui")
    public void tearDown() {
        browser.close();
        playwright.close();
    }
}
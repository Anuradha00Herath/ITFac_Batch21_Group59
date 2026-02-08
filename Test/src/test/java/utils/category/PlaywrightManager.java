package utils.category;

import com.microsoft.playwright.*;

public class PlaywrightManager {

    private static Playwright playwright;
    private static Browser browser;
    private static Page page;

    public static Page getPage() {
        if (page == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            BrowserContext context = browser.newContext();
            page = context.newPage();
        }
        return page;
    }

    // public static void close() {
    //     if (playwright != null) {
    //         playwright.close();
    //         page = null;
    //         browser = null;
    //         playwright = null;
    //     }
    // }
}

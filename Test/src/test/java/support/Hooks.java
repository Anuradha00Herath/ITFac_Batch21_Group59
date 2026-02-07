package support;

import com.microsoft.playwright.*;
import io.cucumber.java.*;

import java.nio.file.*;
import java.time.Instant;

public class Hooks {
    private final TestContext ctx;

    public Hooks(TestContext ctx) {
        this.ctx = ctx;
    }
    @Before("@ui")
    public void beforeUi() {
        ctx.playwright = Playwright.create();
        ctx.browser = ctx.playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(800));                
        ctx.browserContext = ctx.browser.newContext();
        ctx.page = ctx.browserContext.newPage();
        ctx.page.onDialog(dialog -> ctx.pendingDialog = dialog);
    }

    @After("@ui")
    public void afterUi(Scenario scenario) {
        try {
            if (scenario.isFailed() && ctx.page != null) {
                try {
                    Path dir = Paths.get("tests", "screenshots");
                    Files.createDirectories(dir);
                    ctx.page.screenshot(new Page.ScreenshotOptions()
                            .setPath(dir.resolve(Instant.now().toEpochMilli() + "-FAILED.png"))
                            .setFullPage(true));
                } catch (Exception ignored) {}
            }
        } finally {
            if (ctx.browserContext != null) ctx.browserContext.close();
            if (ctx.browser != null) ctx.browser.close();
            if (ctx.playwright != null) ctx.playwright.close();
        }
    }
}

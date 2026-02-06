package steps.ui;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import utils.Config;
import utils.TestContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthUiSteps {

    private final TestContext ctx;

    public AuthUiSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    @Given("I am logged in as {string}")
    public void i_am_logged_in_as(String role) {

        Page page = ctx.page;

        var creds = Config.creds(role);

        page.navigate("http://localhost:8080/ui/login");

        page.fill("input[name='username']", creds.username());
        page.fill("input[name='password']", creds.password());

        page.click("button[type='submit']");

        page.waitForLoadState();

        // simple sanity check
        assertTrue(
                page.url().contains("/"),
                "Login failed. Current URL: " + page.url()
        );
    }

}

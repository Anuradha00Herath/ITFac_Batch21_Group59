package pages.sales;

import com.microsoft.playwright.Page;
import hooks.Hooks;

public class LoginPage {
    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void login(String username, String password) {
        page.navigate("http://localhost:8080/ui/login");
        page.fill("input[name=\"username\"]", username);
        page.fill("input[name=\"password\"]", password);
        page.click("button[type=\"submit\"]");
    }
}

package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;


public class AuthPage {

    private Page page;

    // Locators
    private static final String UI_LOGIN_PATH = "/ui/login";
    private String USERNAME_INPUT = "input[name='username']";
    private String PASSWORD_INPUT = "input[name='password']";
    private String LOGIN_BUTTON = "button[type='submit']";

    public AuthPage(Page page) {
        this.page = page;
    }

    public void loginWithValidCredentials(String user, String pass) {
        page.fill(USERNAME_INPUT, user);
        page.fill(PASSWORD_INPUT, pass);
        page.click(LOGIN_BUTTON);
    }

     public void verifyLogin(){
        page.waitForURL(url -> !url.contains(UI_LOGIN_PATH), new Page.WaitForURLOptions().setTimeout(10000));
    }


}

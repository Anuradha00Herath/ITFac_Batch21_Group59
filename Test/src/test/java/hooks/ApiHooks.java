package hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class ApiHooks {

    public static APIRequestContext api;

    @Before("@api")
    public void setupApi() {
        Playwright playwright = Playwright.create();
        api = playwright.request().newContext();
    }

    @After("@api")
    public void tearDownApi() {
        if (api != null) api.dispose();
    }
}

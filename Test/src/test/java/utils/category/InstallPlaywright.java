package utils.category;

import com.microsoft.playwright.Playwright;

public class InstallPlaywright {
    public static void main(String[] args) {
        Playwright.create().chromium().launch().close();
        System.out.println("Playwright browsers OK");
    }
}

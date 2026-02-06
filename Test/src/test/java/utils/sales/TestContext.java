package utils.sales;

import com.microsoft.playwright.*;

import io.restassured.response.Response;
import com.microsoft.playwright.Dialog;

public class TestContext {
    public TestContext() {}
    // UI
    public Playwright playwright;
    public Browser browser;
    public BrowserContext browserContext;
    public Page page;
    public Dialog pendingDialog;
    public String plantId;

    // API
    public String adminAuthHeader; // "Bearer xxx" OR "Basic xxx"
    public String userAuthHeader;

    // Shared state (mirrors your CustomWorld)
    public Integer beforeStock;
    public String createdSaleId;
    public String capturedSaleId;

    public Integer lastStatus;
    public String lastBodyText;
    public Response lastResponse;
    public Integer minStock;

    public String lastDialogMessage;
    public Integer lastSalePostStatus;
    public String lastSalePostBody;
}
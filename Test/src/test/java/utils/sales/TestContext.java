package utils.sales;

import com.microsoft.playwright.*;

import io.restassured.response.Response;

public class TestContext {
    public TestContext() {}

    public Page page;
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

}
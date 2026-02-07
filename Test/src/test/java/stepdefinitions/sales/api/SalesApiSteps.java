package stepdefinitions.sales.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.ApiAuth;
import utils.sales.Config;
import utils.sales.TestContext;
import io.cucumber.java.en.*;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;

public class SalesApiSteps {
    private static final ObjectMapper OM = new ObjectMapper();
    private final TestContext ctx;

    public SalesApiSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    private static void requireRole(String role) {
        if (!"admin".equals(role) && !"user".equals(role)) {
            throw new IllegalArgumentException("Unknown role \"" + role + "\". Use \"admin\" or \"user\".");
        }
    }

    private String authFor(String role) {
        if ("admin".equals(role)) {
            if (ctx.adminAuthHeader == null) throw new IllegalStateException("Admin not authenticated. Run auth step first.");
            return ctx.adminAuthHeader;
        }
        if ("user".equals(role)) {
            if (ctx.userAuthHeader == null) throw new IllegalStateException("User not authenticated. Run auth step first.");
            return ctx.userAuthHeader;
        }
        throw new IllegalArgumentException("role must be admin/user");
    }

    private Integer extractSaleId(JsonNode node) {
        if (node == null || node.isNull()) return null;
        JsonNode id = node.get("id");
        if (id != null && !id.isNull()) return id.asInt();
        JsonNode saleId = node.get("saleId");
        if (saleId != null && !saleId.isNull()) return saleId.asInt();
        JsonNode data = node.get("data");
        if (data != null && !data.isNull()) return extractSaleId(data);
        return null;
    }

    private int getPlantStockOrThrow(String plantId, String role) {
        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_GET_PLANT(plantId));

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        if (ctx.lastStatus < 200 || ctx.lastStatus >= 300) {
            throw new RuntimeException("Failed to GET plant (" + ctx.lastStatus + ") " +
                    Config.API_GET_PLANT(plantId) + "\nBody: " + ctx.lastBodyText);
        }

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);
            JsonNode stockNode =
                    firstNonNull(json.get("stock"),
                            json.get("quantity"),
                            json.get("availableStock"),
                            json.get("available"),
                            json.path("data").get("stock"),
                            json.path("data").get("quantity"));

            if (stockNode == null || !stockNode.isNumber()) {
                throw new RuntimeException("Plant response missing numeric stock field.\nBody: " + ctx.lastBodyText);
            }
            return stockNode.asInt();
        } catch (Exception e) {
            throw new RuntimeException("Plant response is not JSON.\nBody: " + ctx.lastBodyText, e);
        }
    }

    private static JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode n : nodes) {
            if (n != null && !n.isMissingNode() && !n.isNull()) return n;
        }
        return null;
    }

    private String getAnySaleIdOrThrow(String role) {
        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SALES_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        if (ctx.lastStatus < 200 || ctx.lastStatus >= 300) {
            throw new RuntimeException("Failed to GET sales (" + ctx.lastStatus + ") " +
                    Config.API_SALES_ALL + "\nBody: " + ctx.lastBodyText);
        }

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);
            JsonNode list = json.isArray() ? json : firstNonNull(json.get("data"), json.get("content"));
            if (list == null || !list.isArray() || list.size() == 0) {
                throw new RuntimeException("No sales exist to use. Body: " + ctx.lastBodyText);
            }
            Integer id = extractSaleId(list.get(0));
            if (id == null) throw new RuntimeException("Could not find sale id in first sale item. Body: " + ctx.lastBodyText);
            return String.valueOf(id);
        } catch (Exception e) {
            throw new RuntimeException("Sales list response is not JSON.\nBody: " + ctx.lastBodyText, e);
        }
    }

    //setup
    @Given("I am authenticated as {string} via API")
    public void i_am_authenticated_as_via_api(String role) {
        role = role.trim().replace("\"", "");
        requireRole(role);
        String authHeader = ApiAuth.getAuthHeader(role);
        if ("admin".equals(role)) ctx.adminAuthHeader = authHeader;
        else ctx.userAuthHeader = authHeader;

        assertNotNull(authHeader);
        assertFalse(authHeader.isBlank());
    }

    @Given("plant {int} exists with stock at least {int}")
    public void plant_exists_with_stock_at_least(Integer plantId, Integer minStock) {
        ctx.plantId = String.valueOf(plantId);
        ctx.minStock = minStock;

        int stock = getPlantStockOrThrow(String.valueOf(plantId), "admin"); // default admin like TS helper (falls back to any auth)
        ctx.beforeStock = stock;

        assertTrue(stock >= minStock, "Plant " + plantId + " must have stock >= " + minStock + ". Current stock=" + stock);
    }

    @Given("a sale exists as {word}")
    public void a_sale_exists_capture_as_role(String role) {
        requireRole(role);
        ctx.capturedSaleId = getAnySaleIdOrThrow(role);
        assertNotNull(ctx.capturedSaleId);
    }


    //actions
    @When("I sell plant {int} with quantity {int} as {word}")
    public void i_sell_plant_as_role(Integer plantId, Integer qty, String role) {
        requireRole(role);

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .post(Config.API_SELL_PLANT(plantId, qty));

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        // Try to store created sale id if returned
        try {
            JsonNode json = OM.readTree(ctx.lastBodyText == null ? "" : ctx.lastBodyText);
            Integer id = extractSaleId(json);
            ctx.createdSaleId = (id == null ? null : String.valueOf(id));
        } catch (Exception ignored) {}
    }

    @When("I get sale by id {string} as {word}")
    public void i_get_sale_by_id_as_role(String saleId, String role) {
        requireRole(role);

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SALE_BY_ID(saleId));

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }


    @When("I get all sales as {word}")
    public void i_get_all_sales_as_role(String role) {
        requireRole(role);

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SALES_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("I get sales page as {word} with page {int} size {int} sortField {string} sortDir {string}")
    public void i_get_sales_page(String role, Integer page, Integer size, String sortField, String sortDir) {
        requireRole(role);
        String url = Config.API_SALES_PAGE + "?page=" + page + "&size=" + size +
                "&sortField=" + encode(sortField) + "&sortDir=" + encode(sortDir);

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(url);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("I request sales endpoint without authentication")
    public void i_request_sales_without_auth() {
        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SALES_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("I delete the captured sale as admin")
    public void i_delete_captured_sale_as_admin() {
        assertNotNull(ctx.capturedSaleId, "No capturedSaleId. Run the capture step first.");

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .when()
                        .delete(Config.API_SALE_BY_ID(ctx.capturedSaleId));

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("I get the captured sale as user")
    public void i_get_captured_sale_as_user() {
        assertNotNull(ctx.capturedSaleId, "No capturedSaleId available.");

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("user"))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SALE_BY_ID(ctx.capturedSaleId));

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("I get sale by id captured as admin")
    public void i_get_sale_by_id_captured_as_admin() {
        assertNotNull(ctx.capturedSaleId, "No capturedSaleId available.");
        i_get_sale_by_id_as_role(ctx.capturedSaleId, "admin");
    }

    @When("I request sales pagination endpoint without parameters as user")
    public void i_request_sales_pagination_without_params_as_user() {
        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("user"))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SALES_PAGE);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    //assertion
    @Then("the response status should be {int}")
    public void the_response_status_should_be(Integer code) {
        assertNotNull(ctx.lastStatus);
        assertEquals(code, ctx.lastStatus, "Body: " + ctx.lastBodyText);
    }

    @Then("the sale should be created and plant stock should be reduced by {int}")
    public void stock_should_be_reduced_by(Integer soldQty) {
        assertNotNull(ctx.lastStatus);
        assertTrue(ctx.lastStatus >= 200 && ctx.lastStatus < 300, "Sell failed. Status=" + ctx.lastStatus + "\nBody=" + ctx.lastBodyText);

        assertNotNull(ctx.plantId, "plantId not set.");
        assertNotNull(ctx.beforeStock, "beforeStock not captured.");

        int afterStock = getPlantStockOrThrow(ctx.plantId, "admin");
        int expected = ctx.beforeStock - soldQty;

        assertEquals(expected, afterStock, "Stock mismatch. Before=" + ctx.beforeStock + ", After=" + afterStock + ", Expected=" + expected);
    }

    @Then("the response should contain sale data")
    public void response_should_contain_sale_data() {
        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText == null ? "" : ctx.lastBodyText);
            Integer id = extractSaleId(json);
            if (id == null) id = extractSaleId(json.get("data"));
            assertNotNull(id, "Could not find sale id in response. Body: " + ctx.lastBodyText);
        } catch (Exception e) {
            throw new RuntimeException("Response is not JSON. Body: " + ctx.lastBodyText, e);
        }
    }

    @Then("the response should contain a list of sales")
    public void response_should_contain_list_of_sales() {
        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText == null ? "" : ctx.lastBodyText);
            JsonNode list = json.isArray() ? json : firstNonNull(json.get("data"), json.get("content"));
            assertNotNull(list, "Expected array or {data/content: array}. Body: " + ctx.lastBodyText);
            assertTrue(list.isArray(), "Expected list to be array. Body: " + ctx.lastBodyText);
        } catch (Exception e) {
            throw new RuntimeException("Response is not JSON. Body: " + ctx.lastBodyText, e);
        }
    }

    @Then("the response should contain paginated sales")
    public void response_should_contain_paginated_sales() {
        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText == null ? "" : ctx.lastBodyText);
            JsonNode content = firstNonNull(json.get("content"), json.get("data"), json.get("items"), json.get("results"));
            assertNotNull(content, "Expected paginated response to include content/data/items/results. Body: " + ctx.lastBodyText);
        } catch (Exception e) {
            throw new RuntimeException("Response is not JSON. Body: " + ctx.lastBodyText, e);
        }
    }

    //helpers

    private static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
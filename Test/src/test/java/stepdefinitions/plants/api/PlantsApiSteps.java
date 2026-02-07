package stepdefinitions.plants.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import utils.sales.Config;
import utils.sales.TestContext;
import io.cucumber.java.en.Given;
import io.restassured.path.json.JsonPath;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlantsApiSteps {

    private static final ObjectMapper OM = new ObjectMapper();
    private final TestContext ctx;

    // Endpoint (not in Config.java currently)
    private static final String API_PLANTS_ALL = "/api/plants";
    private static final String API_PLANT_BY_ID = "/api/plants/{id}";
    private static final String API_PLANTS_BY_CATEGORY = "/api/plants/category/{categoryId}";
    private static final String CREATE_PLANT =
            "/api/plants/category/{categoryId}";
    private static final String UPDATE_PLANT =
            "/api/plants/{id}";


    public PlantsApiSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    private static void requireRole(String role) {
        if (!"admin".equals(role) && !"user".equals(role)) {
            throw new IllegalArgumentException("Unknown role \"" + role + "\". Use \"admin\" or \"user\".");
        }
    }

    private String authFor(String role) {
        role = role.toLowerCase();
        requireRole(role);

        if ("admin".equals(role)) {
            if (ctx.adminAuthHeader == null) {
                throw new IllegalStateException("Admin not authenticated. Run: Given I am authenticated as \"admin\" via API");
            }
            return ctx.adminAuthHeader;
        }

        if (ctx.userAuthHeader == null) {
            throw new IllegalStateException("User not authenticated. Run: Given I am authenticated as \"user\" via API");
        }
        return ctx.userAuthHeader;
    }

    // ----------------------------
    // ACTIONS
    // ----------------------------

    @When("I get all plants as {word}")
    public void i_get_all_plants_as(String role) {
        role = role.trim().replace("\"", "").toLowerCase();

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(API_PLANTS_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    // ----------------------------
    // ASSERTIONS
    // ----------------------------

    @Then("the response body should contain a list of plants")
    public void the_response_body_should_contain_a_list_of_plants() {
        assertNotNull(ctx.lastBodyText, "No response body captured.");

        JsonNode json;
        try {
            json = OM.readTree(ctx.lastBodyText);
        } catch (Exception e) {
            throw new AssertionError("Response is not valid JSON.\nBody: " + ctx.lastBodyText, e);
        }

        // Accept common response shapes:
        // 1) [ {...}, {...} ]
        // 2) { "data": [..] }  / { "plants": [..] } / { "items": [..] }
        // 3) { "content": [..] } (Spring pageable)
        JsonNode list =
                firstArray(
                        json,
                        json.get("data"),
                        json.get("plants"),
                        json.get("items"),
                        json.get("content")
                );

        assertNotNull(list, "Could not find a plants array in response.\nBody: " + ctx.lastBodyText);
        assertTrue(list.isArray(), "Plants list node is not an array.\nBody: " + ctx.lastBodyText);

        // If your test requires at least 1 plant, keep this:
        assertTrue(list.size() >= 1, "Expected at least 1 plant but got 0.\nBody: " + ctx.lastBodyText);

        // Optional sanity check (best-effort): first element has a name/id
        JsonNode first = list.size() > 0 ? list.get(0) : null;
        if (first != null && first.isObject()) {
            boolean hasId = first.hasNonNull("id");
            boolean hasName = first.hasNonNull("name");
            assertTrue(hasId || hasName,
                    "Plant objects look unexpected (missing id/name). First item: " + first.toString());
        }
    }

    private static JsonNode firstArray(JsonNode... nodes) {
        for (JsonNode n : nodes) {
            if (n != null && !n.isNull() && n.isArray()) return n;
        }
        return null;
    }
    @Given("at least one plant exists \\(capture a plant id)")
    public void at_least_one_plant_exists_capture_id() {
        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", ctx.userAuthHeader)
                        .contentType("application/json")
                        .when()
                        .get(API_PLANTS_ALL);

        assertEquals(200, res.statusCode(), "Failed to fetch plants list");

        JsonPath jp = res.jsonPath();

        Integer id = null;

        if (jp.get("id[0]") != null) {
            id = jp.getInt("id[0]");
        } else if (jp.get("data[0].id") != null) {
            id = jp.getInt("data[0].id");
        } else if (jp.get("plants[0].id") != null) {
            id = jp.getInt("plants[0].id");
        } else if (jp.get("content[0].id") != null) {
            id = jp.getInt("content[0].id");
        }

        assertNotNull(id, "Could not capture plant ID from response");

        ctx.capturedPlantId = String.valueOf(id);
    }

    @When("I get the plant by id as user")
    public void i_get_plant_by_id_as_user() {
        assertNotNull(ctx.capturedPlantId, "No captured plant ID available");

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", ctx.userAuthHeader)
                        .contentType("application/json")
                        .pathParam("id", ctx.capturedPlantId)
                        .when()
                        .get(API_PLANT_BY_ID);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }
    @Then("the response should contain plant details")
    public void response_should_contain_plant_details() {
        assertNotNull(ctx.lastBodyText, "Response body is null");

        JsonNode json;
        try {
            json = OM.readTree(ctx.lastBodyText);
        } catch (Exception e) {
            throw new AssertionError("Response is not valid JSON", e);
        }

        assertTrue(json.hasNonNull("id"), "Missing plant id");
        assertTrue(json.hasNonNull("name"), "Missing plant name");
        assertTrue(json.has("price"), "Missing plant price");
        assertTrue(json.has("quantity") || json.has("stock"),
                "Missing plant stock/quantity");
    }
    @Given("I prepare a non-existing plant id")
    public void i_prepare_a_non_existing_plant_id() {
        // Make it very unlikely to exist:
        // captured + big offset
        assertNotNull(ctx.capturedPlantId, "No capturedPlantId available. Run capture step first.");

        long base = Long.parseLong(ctx.capturedPlantId);
        ctx.nonExistingPlantId = String.valueOf(base + 999999);
    }

    @When("I get the plant by non-existing id as user")
    public void i_get_the_plant_by_non_existing_id_as_user() {
        assertNotNull(ctx.nonExistingPlantId, "nonExistingPlantId not prepared.");

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", ctx.userAuthHeader)
                        .contentType("application/json")
                        .pathParam("id", ctx.nonExistingPlantId)
                        .when()
                        .get(API_PLANT_BY_ID);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }
    @Given("I prepare a non-existing category id")
    public void i_prepare_a_non_existing_category_id() {
        // Using a very large ID to guarantee non-existence
        ctx.nonExistingCategoryId = "999999";
    }
    @When("I get plants by invalid category id as user")
    public void i_get_plants_by_invalid_category_id_as_user() {
        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", ctx.userAuthHeader)
                        .contentType("application/json")
                        .pathParam("categoryId", ctx.nonExistingCategoryId)
                        .when()
                        .get(API_PLANTS_BY_CATEGORY);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @Then("the response status should be 404 or 200 with empty list for invalid category")
    public void the_response_status_404_or_200_empty_for_invalid_category() {
        assertNotNull(ctx.lastStatus, "No response status captured.");
        if (ctx.lastStatus == 404) {
            return;
        }
        if (ctx.lastStatus == 200) {
            JsonNode json;
            try {
                json = OM.readTree(ctx.lastBodyText != null ? ctx.lastBodyText : "[]");
            } catch (Exception e) {
                fail("Invalid JSON: " + ctx.lastBodyText);
                return;
            }
            JsonNode list = json.isArray() ? json : firstArray(json.get("data"), json.get("content"), json.get("items"));
            assertTrue(list == null || (list.isArray() && list.size() == 0),
                    "Expected 404 or 200 with empty list. Got 200 with body: " + ctx.lastBodyText);
            return;
        }
        fail("Expected 404 or 200 with empty list. Got status " + ctx.lastStatus + ". Body: " + ctx.lastBodyText);
    }

    @Given("a valid category id exists")
    public void a_valid_category_id_exists() {
        ctx.validCategoryId = "2";
    }

    @When("I get plants by category id as user")
    public void i_get_plants_by_category_id_as_user() {
        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", ctx.userAuthHeader)
                        .contentType("application/json")
                        .pathParam("categoryId", ctx.validCategoryId)
                        .when()
                        .get(API_PLANTS_BY_CATEGORY);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }
    @Then("the response should contain only plants from that category")
    public void response_should_contain_only_plants_from_that_category() {
        assertNotNull(ctx.lastBodyText, "Response body is null");

        JsonNode json;
        try {
            json = OM.readTree(ctx.lastBodyText);
        } catch (Exception e) {
            throw new AssertionError("Response is not valid JSON", e);
        }

        // Accept common list response shapes
        JsonNode list =
                json.isArray() ? json :
                        json.get("data") != null ? json.get("data") :
                                json.get("plants") != null ? json.get("plants") :
                                        json.get("content");

        assertNotNull(list, "Could not find plant list in response");
        assertTrue(list.isArray(), "Expected array of plants");

        for (JsonNode plant : list) {
            // category can be {id,name} OR categoryId directly
            if (plant.has("category") && plant.get("category").has("id")) {
                assertEquals(ctx.validCategoryId,
                        plant.get("category").get("id").asText(),
                        "Plant belongs to a different category");
            } else if (plant.has("categoryId")) {
                assertEquals(ctx.validCategoryId,
                        plant.get("categoryId").asText(),
                        "Plant belongs to a different category");
            }
        }
    }

    @When("the admin creates a plant with valid data")
    public void createValidPlant() {
        ctx.createdPlantName ="Plant-" + UUID.randomUUID().toString().replace("-", "").substring(0, 19);
        ;
        createPlant(ctx.validCategoryId, ctx.createdPlantName);
    }
    @Then("the response should contain created plant with id")
    public void createdPlantResponse() throws Exception {
        JsonNode json = OM.readTree(ctx.lastBodyText);
        assertTrue(json.hasNonNull("id"));
        ctx.createdPlantId = json.get("id").asText();
    }
    @Given("a non-existing category id is prepared")
    public void invalidCategory() {
        ctx.invalidCategoryId = "999999";
    }

    @Given("a plant already exists in a category")
    public void existingPlant() {
        ctx.validCategoryId = "2";
        ctx.createdPlantName = "DupPlant" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        createPlant(ctx.validCategoryId, ctx.createdPlantName);
    }

    @Given("an existing plant id is available")
    public void existingPlantId() {
        ctx.validCategoryId = "2";
        ctx.createdPlantName = "UpdPlant" + UUID.randomUUID().toString().replace("-", "").substring(0,5);
        createPlant(ctx.validCategoryId, ctx.createdPlantName);
        ctx.existingPlantId = ctx.createdPlantId;
    }

    @Given("a non-existing plant id is prepared")
    public void invalidPlantId() {
        ctx.existingPlantId = "999999";
    }
    @When("the admin tries to create a duplicate plant")
    public void createDuplicate() {
        createPlant(ctx.validCategoryId, ctx.createdPlantName);
    }

    @When("the admin updates the plant with valid data")
    public void updatePlant() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", ctx.createdPlantName + "-Updated");
        body.put("price", 500);
        body.put("quantity", 20);

        Response res = RestAssured.given()
                .baseUri(Config.BASE_URL)
                .header("Authorization", ctx.adminAuthHeader)
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("id", ctx.existingPlantId)
                .when()
                .put(UPDATE_PLANT);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("the admin tries to update a non-existing plant")
    public void updateInvalid() {
        updatePlant();
    }

    @When("the admin tries to create a plant with invalid category")
    public void createInvalidCategory() {
        ctx.createdPlantName = "InvalidCat-";
        createPlant(ctx.invalidCategoryId, ctx.createdPlantName);
    }
    @Then("the response should contain duplicate plant error")
    public void duplicateError() {
        assertTrue(ctx.lastBodyText.toLowerCase().contains("duplicate"));
    }

    @Then("the response should contain updated plant details")
    public void updatedResponse() throws Exception {
        JsonNode json = OM.readTree(ctx.lastBodyText);
        assertTrue(json.get("name").asText().contains("Updated"));
    }

    @Then("the response should indicate plant not found")
    public void notFound() {
        assertTrue(ctx.lastBodyText.toLowerCase().contains("not"));
    }




    private void createPlant(String categoryId, String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", 100);
        body.put("quantity", 10);

        Response res = RestAssured.given()
                .baseUri(Config.BASE_URL)
                .header("Authorization", ctx.adminAuthHeader)
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("categoryId", categoryId)
                .when()
                .post(CREATE_PLANT);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);
            if (json.has("id")) ctx.createdPlantId = json.get("id").asText();
        } catch (Exception ignored) {}
    }
}
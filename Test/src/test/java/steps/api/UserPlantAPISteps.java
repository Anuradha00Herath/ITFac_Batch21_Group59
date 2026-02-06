package steps.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import utils.ApiAuth;
import utils.Config;
import utils.TestContext;

import static org.junit.jupiter.api.Assertions.*;

public class UserPlantAPISteps {

    private static final ObjectMapper OM = new ObjectMapper();
    private final TestContext ctx;

    public UserPlantAPISteps(TestContext ctx) {
        this.ctx = ctx;
    }

    // -----------------helpers-----------------

    private static void requireRole(String role) {
        if (!"user".equals(role) && !"admin".equals(role)) {
            throw new IllegalArgumentException("Unknown role \"" + role + "\". Use \"user\" or \"admin\".");
        }
    }

    private String authFor(String role) {
        if ("admin".equals(role)) {
            if (ctx.adminAuthHeader == null)
                throw new IllegalStateException("Admin not authenticated. Run auth step first.");
            return ctx.adminAuthHeader;
        }

        if ("user".equals(role)) {
            if (ctx.userAuthHeader == null)
                throw new IllegalStateException("User not authenticated. Run auth step first.");
            return ctx.userAuthHeader;
        }

        throw new IllegalArgumentException("role must be admin/user");
    }

    private static JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode n : nodes) {
            if (n != null && !n.isMissingNode() && !n.isNull()) return n;
        }
        return null;
    }

    // -------------------------------------------------------
    // AUTH
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // PRECONDITION
    // -------------------------------------------------------

    @Given("plant records exist in the system")
    public void plant_records_exist_in_the_system() {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization",
                                ctx.adminAuthHeader != null
                                        ? authFor("admin")
                                        : authFor("user"))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_PLANTS_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);
            JsonNode list = json.isArray()
                    ? json
                    : firstNonNull(json.get("data"), json.get("content"));

            assertNotNull(list, "Expected plant list. Body: " + ctx.lastBodyText);
            assertTrue(list.isArray(), "Expected plant list to be an array. Body: " + ctx.lastBodyText);
            assertTrue(list.size() > 0, "Expected at least one plant record in the system.");

        } catch (Exception e) {
            throw new RuntimeException("Response is not valid JSON. Body: " + ctx.lastBodyText, e);
        }
    }
    @Given("a plant record exists")
    public void a_plant_record_exists() {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_PLANTS_ALL);

        assertEquals(200, res.statusCode(), "Failed to fetch plants");

        try {
            JsonNode json = OM.readTree(res.asString());
            JsonNode list = json.isArray()
                    ? json
                    : firstNonNull(json.get("data"), json.get("content"));

            if (list != null && list.size() > 0) {
                ctx.plantId = String.valueOf(list.get(0).get("id").asInt());
                return;
            }

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON while reading plants", e);
        }

        // If no plant exists → create one
        String body = """
    {
      "name": "Plant For Update",
      "price": 100,
      "quantity": 5,
      "category": {
        "id": 1
      }
    }
    """;

        Response createRes =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/api/admin/plants");

        assertEquals(201, createRes.statusCode());

        try {
            ctx.plantId = String.valueOf(OM.readTree(createRes.asString()).get("id").asInt());
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON while creating plant", e);
        }
    }

    @Given("no plant records exist in the system")
    public void no_plant_records_exist_in_the_system() {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("user"))
                        .contentType("application/json")
                        .when()
                        .get("/api/plants/paged?page=0&size=1");

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        // Accept either 404 OR 200 with empty list
        if (ctx.lastStatus == 200) {

            try {
                JsonNode json = OM.readTree(ctx.lastBodyText);

                JsonNode list = firstNonNull(
                        json.get("data"),
                        json.get("content"),
                        json.get("items"),
                        json.get("results")
                );

                assertNotNull(list, "Expected plant list structure.");
                assertTrue(list.isArray(), "Expected plant list to be an array.");
                assertEquals(0, list.size(),
                        "Expected no plant records, but found " + list.size());

            } catch (Exception e) {
                throw new RuntimeException("Invalid JSON. Body: " + ctx.lastBodyText, e);
            }

        } else {
            assertEquals(404, ctx.lastStatus,
                    "Expected 404 when no plants exist. Body: " + ctx.lastBodyText);
        }
    }


    @Given("more than 10 plant records exist in the system")
    public void more_than_10_plant_records_exist_in_the_system() {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("user"))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_PLANTS_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);
            JsonNode list = json.isArray()
                    ? json
                    : firstNonNull(json.get("data"), json.get("content"));

            assertNotNull(list, "Expected plant list.");
            assertTrue(list.isArray(), "Expected array of plants.");
            assertTrue(list.size() > 10,
                    "Expected more than 10 plant records but found " + list.size());

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON. Body: " + ctx.lastBodyText, e);
        }
    }

    @Given("a valid plant category exists")
    public void a_valid_plant_category_exists() {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization",
                                ctx.adminAuthHeader != null
                                        ? authFor("admin")
                                        : authFor("user"))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_PLANTS_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);

            JsonNode list = json.isArray()
                    ? json
                    : firstNonNull(json.get("data"), json.get("content"));

            assertNotNull(list, "Expected plant list.");
            assertTrue(list.size() > 0, "Expected at least one plant.");

            JsonNode firstPlant = list.get(0);

            JsonNode categoryNode = firstNonNull(
                    firstPlant.get("categoryId"),
                    firstPlant.path("category").path("id")
            );

            assertNotNull(categoryNode, "Plant does not contain category info.");

            ctx.categoryId = categoryNode.asInt();
            assertTrue(ctx.categoryId > 0, "Invalid category ID found.");

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON. Body: " + ctx.lastBodyText, e);
        }
    }
    @Then("the response body should contain only plants from the category")
    public void the_response_body_should_contain_only_plants_from_the_category() {

        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);
        assertNotNull(ctx.categoryId, "categoryId not set.");

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);

            JsonNode list = json.isArray()
                    ? json
                    : firstNonNull(
                    json.get("data"),
                    json.get("content"),
                    json.get("items"),
                    json.get("results")
            );

            assertNotNull(list, "Expected plant list.");
            assertTrue(list.isArray(), "Expected array of plants.");
            assertTrue(list.size() > 0, "Expected at least one plant.");

            for (JsonNode plant : list) {

                JsonNode categoryNode = firstNonNull(
                        plant.get("categoryId"),
                        plant.path("category").path("id")
                );

                assertNotNull(categoryNode, "Plant missing category information.");
                assertEquals(
                        ctx.categoryId,
                        categoryNode.asInt(),
                        "Plant does not belong to requested category"
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON. Body: " + ctx.lastBodyText, e);
        }
    }



    // -------------------------------------------------------
    // ACTION
    // -------------------------------------------------------

    @When("I send a GET request to {string}")
    public void i_send_a_get_request_to(String endpoint) {

        if (endpoint.contains("{categoryId}")) {
            assertNotNull(ctx.categoryId, "categoryId not initialized.");
            endpoint = endpoint.replace("{categoryId}", ctx.categoryId.toString());
        }

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("user"))
                        .contentType("application/json")
                        .when()
                        .get(endpoint);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }


    // -------------------------------------------------------
    // ASSERTIONS
    // -------------------------------------------------------

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer code) {
        assertNotNull(ctx.lastStatus);
        assertEquals(code, ctx.lastStatus, "Body: " + ctx.lastBodyText);
    }


    @Then("the response body should contain a list of plant records")
    public void the_response_body_should_contain_a_list_of_plant_records() {

        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);

            JsonNode list = json.isArray()
                    ? json
                    : firstNonNull(
                    json.get("data"),
                    json.get("content"),
                    json.get("items"),
                    json.get("results")
            );

            assertNotNull(list, "Expected list of plants in response. Body: " + ctx.lastBodyText);
            assertTrue(list.isArray(), "Plant records should be an array. Body: " + ctx.lastBodyText);

        } catch (Exception e) {
            throw new RuntimeException("Response is not JSON. Body: " + ctx.lastBodyText, e);
        }
    }
    @Then("the response should contain pagination metadata")
    public void the_response_should_contain_pagination_metadata() {

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);

            JsonNode page = firstNonNull(
                    json.get("page"),
                    json.get("pageable")
            );

            JsonNode totalElements = json.get("totalElements");
            JsonNode totalPages = json.get("totalPages");
            JsonNode size = json.get("size");
            JsonNode number = json.get("number");

            assertTrue(
                    page != null || totalElements != null,
                    "Expected pagination metadata in response. Body: " + ctx.lastBodyText
            );

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON. Body: " + ctx.lastBodyText, e);
        }
    }

    @Then("the response body should contain an empty list")
    public void the_response_body_should_contain_an_empty_list() {

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);

            JsonNode list = json.isArray()
                    ? json
                    : firstNonNull(
                    json.get("data"),
                    json.get("content"),
                    json.get("items"),
                    json.get("results")
            );

            assertNotNull(list, "Expected list in response.");
            assertTrue(list.isArray(), "Expected an array.");
            assertEquals(0, list.size(),
                    "Expected empty list but found " + list.size());

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON. Body: " + ctx.lastBodyText, e);
        }
    }

    @When("I create a new plant under the category using valid data")
    public void i_create_a_new_plant_under_the_category_using_valid_data() {

        assertNotNull(ctx.categoryId, "categoryId must be initialized.");

        String requestBody = """
{
  "name": "Test Plant API",
  "price": 150,
  "quantity": 10,
  "category": {
    "id": %d
  }
}
""".formatted(ctx.categoryId);

        String endpoint = "/api/plants/category/" + ctx.categoryId;

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post(endpoint);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @Then("the response body should contain the created plant with an id")
    public void the_response_body_should_contain_the_created_plant_with_an_id() {

        assertEquals(201, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);

            JsonNode idNode = firstNonNull(
                    json.get("id"),
                    json.path("data").get("id")
            );

            assertNotNull(idNode, "Created plant ID is missing.");
            assertTrue(idNode.asInt() > 0, "Plant ID should be a positive number.");


        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON. Body: " + ctx.lastBodyText, e);
        }
    }

    @Given("a plant with the same name and category already exists")
    public void a_plant_with_the_same_name_and_category_already_exists() {

        // Ensure category exists
        if (ctx.categoryId == null) {
            a_valid_plant_category_exists();
        }

        assertNotNull(ctx.categoryId, "categoryId must be initialized.");

        // Use a fixed name for duplicate testing
        ctx.plantName = "Duplicate Test Plant";

        String requestBody = """
{
  "name": "%s",
  "price": 150,
  "quantity": 10,
  "category": {
    "id": %d
  }
}
""".formatted(ctx.plantName, ctx.categoryId);

        String endpoint = "/api/plants/category/" + ctx.categoryId;

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post(endpoint);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        assertEquals(201, ctx.lastStatus, "Failed to create initial plant. Body: " + ctx.lastBodyText);
    }

    @When("I create the same plant again under the same category")
    public void i_create_the_same_plant_again_under_the_same_category() {

        assertNotNull(ctx.categoryId, "categoryId must be initialized.");
        assertNotNull(ctx.plantName, "plantName must be initialized from previous step.");

        String requestBody = """
{
  "name": "%s",
  "price": 150,
  "quantity": 10,
  "category": {
    "id": %d
  }
}
""".formatted(ctx.plantName, ctx.categoryId);

        String endpoint = "/api/plants/category/" + ctx.categoryId;

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post(endpoint);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("I update the existing plant with valid data")
    public void i_update_the_existing_plant_with_valid_data() {

        assertNotNull(ctx.plantId, "plantId must exist before update");

        ctx.updatedPlantName = "Updated Plant Name";
        ctx.updatedQuantity = 20;

        String requestBody = """
    {
      "name": "%s",
      "price": 180,
      "quantity": %d
    }
    """.formatted(ctx.updatedPlantName, ctx.updatedQuantity);

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .pathParam("plantId", ctx.plantId)
                        .body(requestBody)
                        .when()
                        .put("/api/plants/{plantId}");

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }
    @Then("the response body should contain updated plant details")
    public void the_response_body_should_contain_updated_plant_details() {

        assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

        try {
            JsonNode json = OM.readTree(ctx.lastBodyText);

            assertEquals(ctx.updatedPlantName, json.get("name").asText());
            assertEquals(ctx.updatedQuantity, json.get("quantity").asInt());

        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON in update response", e);
        }
    }

    @When("I update a plant with invalid id")
    public void i_update_a_plant_with_invalid_id() {

        int invalidPlantId = 999999; // very unlikely to exist

        String requestBody = """
    {
      "name": "Invalid Update Test",
      "price": 150,
      "quantity": 10
    }
    """;

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .pathParam("plantId", invalidPlantId)
                        .body(requestBody)
                        .when()
                        .put("/api/plants/{plantId}");

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

    @When("I create a new plant under an invalid category")
    public void i_create_a_new_plant_under_an_invalid_category() {

        int invalidCategoryId = 999999;

        String endpoint = "/api/plants/category/" + invalidCategoryId;

        String requestBody = """
    {
      "name": "Invalid Category Plant",
      "price": 120,
      "quantity": 10
    }
    """;

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor("admin"))
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post(endpoint);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();
    }

}

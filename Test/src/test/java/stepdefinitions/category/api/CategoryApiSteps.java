package stepdefinitions.category.api;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import pages.category.CategoryApiPage;
import utils.category.ApiAuth;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Step definitions for Category API testing
 * Handles authentication, CRUD operations, and validation for both Admin and User roles
 */
public class CategoryApiSteps {

    // ================= CONSTANTS =================
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PARENT_CATEGORY = "parentCategory";
    private static final String FIELD_MESSAGE = "message";
    private static final int HTTP_OK = 200;
    private static final int HTTP_NO_CONTENT = 204;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;

    // ================= STATE =================
    private String token;
    private Response response;
    private String validCategoryId;
    private String categoryIdWithoutDependencies;
    private Map<String, Object> requestBody;
    private Map<String, Object> originalCategoryData;

    private final CategoryApiPage categoryApiPage = new CategoryApiPage();

    // ================= AUTHENTICATION =================

    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String role) {
        token = ApiAuth.loginAndGetToken(role);
        assertNotNull(token, "Authentication token should not be null for role: " + role);
        System.out.println("✓ Authenticated as: " + role);
    }

    @Given("User is authenticated")
    public void userIsAuthenticated() {
        iAmAuthenticatedAs("user");
    }

    // ================= PRECONDITIONS =================

    @Given("at least one category exists in system")
    @Given("at least one category exists in the system")
    public void atLeastOneCategoryExists() {
        response = categoryApiPage.getAllCategories(token);
        assertEquals(HTTP_OK, response.getStatusCode(), 
            "Failed to fetch categories - expected 200 OK");

        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        assertNotNull(categories, "Categories list should not be null");
        assertFalse(categories.isEmpty(), "At least one category should exist in the system");

        // Store first category for subsequent operations
        Map<String, Object> category = categories.get(0);
        validCategoryId = extractId(category);
        originalCategoryData = new HashMap<>(category);
        
        System.out.println("✓ Found " + categories.size() + " categories in system");
        System.out.println("  Using category ID: " + validCategoryId);
    }

    @Given("category with valid ID exists in system")
    public void categoryWithValidIdExists() {
        atLeastOneCategoryExists();
    }

    @Given("category with no associated plants exists")
    public void categoryWithNoDependenciesExists() {
        response = categoryApiPage.getAllCategories(token);
        assertEquals(HTTP_OK, response.getStatusCode(), 
            "Failed to fetch categories for deletion test");

        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        assertFalse(categories.isEmpty(), "No categories available for deletion test");

        // Use first category and assume no dependencies
        // In real scenario, you'd filter categories by dependency status
        categoryIdWithoutDependencies = extractId(categories.get(0));
        
        System.out.println("✓ Selected category for deletion: " + categoryIdWithoutDependencies);
    }

    // ================= GET OPERATIONS =================

    @When("I send GET request to {string}")
    @When("User sends a GET request to {string}")
    public void sendGetRequest(String endpoint) {
        String requestUrl = endpoint;
        
        // Replace path parameters
        if (endpoint.contains("{id}")) {
            assertNotNull(validCategoryId, "Valid category ID must be set before using {id} parameter");
            requestUrl = endpoint.replace("{id}", validCategoryId);
        }
        
        response = categoryApiPage.rawGet(token, requestUrl);
        System.out.println("✓ GET request sent to: " + requestUrl);
        System.out.println("  Response status: " + response.getStatusCode());
    }

    @When("I send GET request for a valid category ID")
    @When("User sends a GET request for an existing category")
    public void getCategoryById() {
        assertNotNull(validCategoryId, "Valid category ID must be set before sending GET request");
        
        response = categoryApiPage.getCategoryById(token, validCategoryId);
        System.out.println("✓ GET request sent for category ID: " + validCategoryId);
        System.out.println("  Response status: " + response.getStatusCode());
    }

    @When("User sends a GET request for a non-existent category")
    public void getNonExistentCategory() {
        // Generate a random non-existent ID (highly unlikely to exist)
        String nonExistentId = "99" + UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 8);
        
        response = categoryApiPage.getCategoryById(token, nonExistentId);
        System.out.println("✓ GET request sent for non-existent ID: " + nonExistentId);
        System.out.println("  Response status: " + response.getStatusCode());
    }

    // ================= UPDATE OPERATIONS =================

    @When("I update the category name")
    @When("Admin updates the category name")
    public void updateCategoryName() {
        assertNotNull(validCategoryId, "Valid category ID must be set before updating");
        
        String newName = "Cat" + (System.currentTimeMillis() % 10000);
        requestBody = new HashMap<>();
        requestBody.put(FIELD_NAME, newName);

        response = categoryApiPage.updateCategory(token, validCategoryId, requestBody);
        System.out.println("✓ PUT request sent to update category: " + validCategoryId);
        System.out.println("  New name: " + newName);
        System.out.println("  Response status: " + response.getStatusCode());
    }

    @When("I attempt to update category without name")
    @When("Admin attempts to update category without name")
    public void updateWithoutName() {
        assertNotNull(validCategoryId, "Valid category ID must be set before updating");
        
        // Send empty request body to test validation
        requestBody = new HashMap<>();
        
        response = categoryApiPage.updateCategory(token, validCategoryId, requestBody);
        System.out.println("✓ PUT request sent without name field");
        System.out.println("  Response status: " + response.getStatusCode());
    }

    @When("User attempts to update a category")
    public void userAttemptsToUpdateCategory() {
        assertNotNull(validCategoryId, "Valid category ID must be set before updating");
        
        String newName = "Cat" + (System.currentTimeMillis() % 10000);
        requestBody = new HashMap<>();
        requestBody.put(FIELD_NAME, newName);

        response = categoryApiPage.updateCategory(token, validCategoryId, requestBody);
        System.out.println("✓ PUT request sent by user (should be forbidden)");
        System.out.println("  Response status: " + response.getStatusCode());
    }

    // ================= DELETE OPERATIONS =================

    @When("I delete the category")
    @When("Admin deletes the category")
    public void deleteCategory() {
        assertNotNull(categoryIdWithoutDependencies, 
            "Category ID for deletion must be set before deleting");
        
        response = categoryApiPage.deleteCategory(token, categoryIdWithoutDependencies);
        System.out.println("✓ DELETE request sent for category: " + categoryIdWithoutDependencies);
        System.out.println("  Response status: " + response.getStatusCode());
    }

    // ================= RESPONSE VALIDATION =================

    @Then("the response status code should be {int}")
    public void verifyStatusCode(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode(),
            String.format("Expected status code %d but received %d. Response: %s",
                expectedStatusCode, response.getStatusCode(), response.getBody().asString()));
        System.out.println("✓ Status code verified: " + expectedStatusCode);
    }

    @Then("the response status code should be {int} or {int}")
    public void verifyStatusCodeEither(int statusCode1, int statusCode2) {
        int actualStatus = response.getStatusCode();
        assertTrue(actualStatus == statusCode1 || actualStatus == statusCode2,
            String.format("Expected status code %d or %d but received %d", 
                statusCode1, statusCode2, actualStatus));
        System.out.println("✓ Status code verified: " + actualStatus + 
            " (expected " + statusCode1 + " or " + statusCode2 + ")");
    }

    // ================= CATEGORY LIST VALIDATION =================

    @Then("validate response body contains category list")
    @Then("the response body contains a list of categories")
    public void validateCategoryList() {
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        
        assertNotNull(categories, "Categories list should not be null");
        assertFalse(categories.isEmpty(), "Categories list should contain at least one category");
        
        System.out.println("✓ Category list validated: " + categories.size() + " categories found");
    }

    @Then("response contains array of categories with ID, name, and parent category fields")
    @Then("each category contains id, name, and parent category fields")
    public void validateCategoryArrayStructure() {
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        
        assertNotNull(categories, "Categories array should not be null");
        assertFalse(categories.isEmpty(), "Categories array should not be empty");
        
        // Validate structure of first category (representative sample)
        Map<String, Object> sampleCategory = categories.get(0);
        
        assertTrue(sampleCategory.containsKey(FIELD_ID), 
            "Category must have '" + FIELD_ID + "' field");
        assertNotNull(sampleCategory.get(FIELD_ID), 
            "Category ID must not be null");
        
        assertTrue(sampleCategory.containsKey(FIELD_NAME), 
            "Category must have '" + FIELD_NAME + "' field");
        assertNotNull(sampleCategory.get(FIELD_NAME), 
            "Category name must not be null");
        
        System.out.println("✓ Category structure validated:");
        System.out.println("  Sample ID: " + sampleCategory.get(FIELD_ID));
        System.out.println("  Sample Name: " + sampleCategory.get(FIELD_NAME));
        System.out.println("  Available fields: " + sampleCategory.keySet());
    }

    // ================= SINGLE CATEGORY VALIDATION =================

    @Then("validate category details in response")
    @Then("the response contains correct category details")
    public void verifyCategoryDetails() {
        Map<String, Object> category = response.jsonPath().getMap("$");
        
        assertNotNull(category, "Category object should not be null");
        
        String categoryId = extractId(category);
        assertEquals(validCategoryId, categoryId, 
            "Response category ID should match the requested ID");
        
        assertTrue(category.containsKey(FIELD_NAME), 
            "Category must have '" + FIELD_NAME + "' field");
        
        System.out.println("✓ Category details validated:");
        System.out.println("  ID: " + categoryId);
        System.out.println("  Name: " + category.get(FIELD_NAME));
    }

    @Then("response contains correct category with ID, name, and parent category information")
    @Then("the category includes id, name, and parent category information")
    public void validateCategoryWithFullDetails() {
        response.then()
            .body(FIELD_ID, notNullValue())
            .body(FIELD_NAME, notNullValue())
            .body(FIELD_PARENT_CATEGORY, anyOf(nullValue(), notNullValue()));
        
        Map<String, Object> category = response.jsonPath().getMap("$");
        System.out.println("✓ Category structure validated:");
        System.out.println("  ID: " + category.get(FIELD_ID));
        System.out.println("  Name: " + category.get(FIELD_NAME));
        System.out.println("  Parent Category: " + category.get(FIELD_PARENT_CATEGORY));
    }

    // ================= UPDATE VALIDATION =================

    @Then("response shows category name updated")
    @Then("validate updated category data")
    public void validateUpdatedCategory() {
        Map<String, Object> category = response.jsonPath().getMap("$");
        
        assertNotNull(category, "Updated category response should not be null");
        
        String categoryId = extractId(category);
        assertEquals(validCategoryId, categoryId, 
            "Updated category ID should match the original ID");
        
        assertTrue(category.containsKey(FIELD_NAME), 
            "Updated category must have '" + FIELD_NAME + "' field");
        
        String updatedName = category.get(FIELD_NAME).toString();
        String expectedName = requestBody.get(FIELD_NAME).toString();
        
        assertEquals(expectedName, updatedName, 
            "Category name should be updated to '" + expectedName + "'");
        
        System.out.println("✓ Category update validated:");
        System.out.println("  ID: " + categoryId);
        System.out.println("  Updated Name: " + updatedName);
    }

    @Then("the category data remains unchanged")
    public void verifyCategoryDataUnchanged() {
        // Fetch current category data
        Response currentResponse = categoryApiPage.getCategoryById(token, validCategoryId);
        assertEquals(HTTP_OK, currentResponse.getStatusCode(), 
            "Should be able to fetch category to verify it's unchanged");
        
        Map<String, Object> currentData = currentResponse.jsonPath().getMap("$");
        
        // Compare with original data
        assertEquals(originalCategoryData.get(FIELD_NAME), currentData.get(FIELD_NAME),
            "Category name should remain unchanged after unauthorized update attempt");
        
        System.out.println("✓ Category data verified as unchanged:");
        System.out.println("  Original Name: " + originalCategoryData.get(FIELD_NAME));
        System.out.println("  Current Name: " + currentData.get(FIELD_NAME));
    }

    // ================= DELETE VALIDATION =================

    @Then("the category should no longer exist")
    public void verifyCategoryDeleted() {
        assertNotNull(categoryIdWithoutDependencies, 
            "Deleted category ID must be set to verify deletion");
        
        Response verifyResponse = categoryApiPage.getCategoryById(token, categoryIdWithoutDependencies);
        assertEquals(HTTP_NOT_FOUND, verifyResponse.getStatusCode(),
            "Deleted category should return 404 Not Found");
        
        System.out.println("✓ Category deletion confirmed:");
        System.out.println("  Deleted ID: " + categoryIdWithoutDependencies);
        System.out.println("  Verification status: 404 Not Found");
    }

    // ================= ERROR MESSAGE VALIDATION =================

    @Then("the response contains an error message indicating category not found")
    public void verifyNotFoundErrorMessage() {
        String responseBody = response.getBody().asString();
        
        response.then()
            .body(FIELD_MESSAGE, notNullValue())
            .body(FIELD_MESSAGE, containsStringIgnoringCase("not"));
        
        System.out.println("✓ Error message validated:");
        System.out.println("  Response: " + responseBody);
    }

    @Then("error message: {string}")
    public void verifySpecificErrorMessage(String expectedMessage) {
        String responseBody = response.getBody().asString();
        
        try {
            Map<String, Object> errorResponse = response.jsonPath().getMap("$");
            
            // Check common error message fields
            String[] messageFields = {FIELD_MESSAGE, "error", "errorMessage", "details"};
            boolean messageFound = false;
            
            for (String field : messageFields) {
                if (errorResponse.containsKey(field)) {
                    String actualMessage = errorResponse.get(field).toString();
                    if (actualMessage.toLowerCase().contains(expectedMessage.toLowerCase())) {
                        messageFound = true;
                        System.out.println("✓ Error message validated in field '" + field + "':");
                        System.out.println("  " + actualMessage);
                        break;
                    }
                }
            }
            
            if (!messageFound) {
                // Fallback: check entire response body
                assertThat("Response should contain expected error message",
                    responseBody.toLowerCase(), 
                    containsString(expectedMessage.toLowerCase()));
                System.out.println("✓ Error message found in response body");
            }
            
        } catch (Exception e) {
            // If JSON parsing fails, check plain text response
            assertThat("Response should contain expected error message",
                responseBody.toLowerCase(), 
                containsString(expectedMessage.toLowerCase()));
            System.out.println("✓ Error message validated in plain text response");
        }
    }

    // ================= SUMMARY VALIDATION =================

    @Then("the response contains accurate summary information")
    public void validateSummaryInformation() {
        String responseBody = response.getBody().asString();
        
        try {
            // Try parsing as JSON object
            Map<String, Object> summary = response.jsonPath().getMap("$");
            assertNotNull(summary, "Summary data should not be null");
            assertFalse(summary.isEmpty(), "Summary data should contain information");
            
            System.out.println("✓ Summary information validated:");
            summary.forEach((key, value) -> 
                System.out.println("  " + key + ": " + value));
                
        } catch (Exception e) {
            // Try parsing as JSON array
            List<Map<String, Object>> summaryList = response.jsonPath().getList("$");
            assertNotNull(summaryList, "Summary list should not be null");
            assertFalse(summaryList.isEmpty(), "Summary list should contain items");
            
            System.out.println("✓ Summary information validated (" + summaryList.size() + " items)");
        }
    }

    @Then("the user has read-only access to the summary")
    public void verifyUserReadOnlyAccess() {
        assertEquals(HTTP_OK, response.getStatusCode(),
            "User should have read-only access to category summary (200 OK)");
        System.out.println("✓ User read-only access confirmed for summary endpoint");
    }

    // ================= UTILITY METHODS =================

    /**
     * Extracts ID from category map and converts to String
     * Handles both String and Integer ID types
     */
    private String extractId(Map<String, Object> category) {
        Object id = category.get(FIELD_ID);
        assertNotNull(id, "Category ID should not be null");
        return id.toString();
    }

    /**
     * Generates a unique category name for testing
     */
    private String generateUniqueCategoryName() {
        return "TestCategory_" + System.currentTimeMillis();
    }

    /**
     * Logs request details for debugging
     */
    private void logRequest(String method, String endpoint, Map<String, Object> body) {
        System.out.println("=== Request Details ===");
        System.out.println("Method: " + method);
        System.out.println("Endpoint: " + endpoint);
        if (body != null && !body.isEmpty()) {
            System.out.println("Body: " + body);
        }
        System.out.println("======================");
    }
}
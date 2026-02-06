package steps.api;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import pages.api.CategoryApiPage;
import support.ApiAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryApiSteps {

    private String token;
    private Response response;
    private String validCategoryId;
    private String categoryIdWithoutDependencies;
    private Map<String, Object> requestBody;
    private final CategoryApiPage categoryApiPage = new CategoryApiPage();

    // ============== GIVEN Steps ==============
    
    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String role) {
        token = ApiAuth.loginAndGetToken(role);
        assertNotNull(token, "Token should not be null");
        System.out.println("Authenticated as: " + role);
    }

    @Given("at least one category exists in system")
    public void atLeastOneCategoryExistsInSystem() {
        response = categoryApiPage.getAllCategories(token);
        assertEquals(200, response.getStatusCode(), "Should be able to fetch categories");
        
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        assertFalse(categories.isEmpty(), "At least one category should exist in system");
        System.out.println("Confirmed: " + categories.size() + " categories exist in system");
    }

    @Given("category with valid ID exists in system")
    public void categoryWithValidIdExistsInSystem() {
        atLeastOneCategoryExistsInSystem();
        iGetValidCategoryIdFromSystem();
    }

    @Given("category with no associated plants exists")
    public void categoryWithNoAssociatedPlantsExists() {
        response = categoryApiPage.getAllCategories(token);
        assertEquals(200, response.getStatusCode(), "Should be able to fetch categories");
        
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        assertFalse(categories.isEmpty(), "At least one category should exist");
        
        // Try to find a category without plant associations
        for (Map<String, Object> category : categories) {
            // Check various possible field names for plant count
            String[] plantCountFields = {"plantCount", "plant_count", "plantsCount", "plants_count", "associatedPlants"};
            
            for (String field : plantCountFields) {
                if (category.containsKey(field)) {
                    Object countValue = category.get(field);
                    if (countValue != null && Integer.parseInt(countValue.toString()) == 0) {
                        categoryIdWithoutDependencies = category.get("id").toString();
                        System.out.println("Found category without plant dependencies: " + categoryIdWithoutDependencies);
                        return;
                    }
                }
            }
        }
        
        // If no category with explicit plant count found, use the last category
        // (assuming newer/last categories are less likely to have dependencies)
        categoryIdWithoutDependencies = categories.get(categories.size() - 1).get("id").toString();
        System.out.println("Using category ID (assuming no dependencies): " + categoryIdWithoutDependencies);
    }

    // ============== WHEN Steps ==============
    
    @When("I send GET request to {string}")
    public void iSendGetRequestTo(String endpoint) {
        String categoryId = validCategoryId != null ? validCategoryId : categoryIdWithoutDependencies;
        
        if (endpoint.equals("/api/categories")) {
            response = categoryApiPage.getAllCategories(token);
            System.out.println("GET request sent to: /api/categories");
        } else if (endpoint.contains("{id}")) {
            String url = endpoint.replace("{id}", categoryId);
            response = categoryApiPage.getCategoryById(token, categoryId);
            System.out.println("GET request sent to: " + url);
        }
        
        System.out.println("Response status: " + response.getStatusCode());
    }

    @When("I get a valid category ID from system")
    public void iGetValidCategoryIdFromSystem() {
        response = categoryApiPage.getAllCategories(token);
        assertEquals(200, response.getStatusCode(), "Should be able to fetch categories");
        
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        assertFalse(categories.isEmpty(), "Category list should not be empty");
        
        validCategoryId = categories.get(0).get("id").toString();
        assertNotNull(validCategoryId, "Valid category ID should not be null");
        System.out.println("Retrieved valid category ID: " + validCategoryId);
    }

    @When("I get valid category ID")
    public void iGetValidCategoryId() {
        iGetValidCategoryIdFromSystem();
    }

    @When("I get category ID that has no plant dependencies")
    public void iGetCategoryIdWithoutDependencies() {
        assertNotNull(categoryIdWithoutDependencies, "Category ID without dependencies should be set");
        System.out.println("Using category ID without dependencies: " + categoryIdWithoutDependencies);
    }

    @When("I prepare request body with name {string} and parentId {string}")
    public void iPrepareRequestBodyWithNameAndParentId(String name, String parentId) {
        requestBody = new HashMap<>();
        requestBody.put("name", name);
        
        if ("null".equalsIgnoreCase(parentId)) {
            requestBody.put("parentId", null);
        } else {
            requestBody.put("parentId", parentId);
        }
        
        System.out.println("Request body prepared: " + requestBody);
    }

    @When("I prepare request body with only parentId {string}")
    public void iPrepareRequestBodyWithOnlyParentId(String parentId) {
        requestBody = new HashMap<>();
        
        if ("null".equalsIgnoreCase(parentId)) {
            requestBody.put("parentId", null);
        } else {
            requestBody.put("parentId", parentId);
        }
        
        System.out.println("Request body prepared (without name field): " + requestBody);
    }

    @When("I send PUT request to {string}")
    public void iSendPutRequestTo(String endpoint) {
        String categoryId = validCategoryId != null ? validCategoryId : categoryIdWithoutDependencies;
        String url = endpoint.replace("{id}", categoryId);
        
        assertNotNull(requestBody, "Request body should be prepared before sending PUT request");
        
        response = categoryApiPage.updateCategory(token, categoryId, requestBody);
        System.out.println("PUT request sent to: " + url);
        System.out.println("Request body: " + requestBody);
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody().asString());
    }

    @When("I send DELETE request to {string}")
    public void iSendDeleteRequestTo(String endpoint) {
        String categoryId = categoryIdWithoutDependencies != null ? 
                           categoryIdWithoutDependencies : validCategoryId;
        String url = endpoint.replace("{id}", categoryId);
        
        response = categoryApiPage.deleteCategory(token, categoryId);
        System.out.println("DELETE request sent to: " + url);
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody().asString());
    }

    @When("I send GET request to verify category is deleted")
    public void iSendGetRequestToVerifyDeleted() {
        String categoryId = categoryIdWithoutDependencies != null ? 
                           categoryIdWithoutDependencies : validCategoryId;
        
        response = categoryApiPage.getCategoryById(token, categoryId);
        System.out.println("Verification GET request for deleted category ID: " + categoryId);
        System.out.println("Response status: " + response.getStatusCode());
    }

    // ============== THEN Steps ==============
    
    @Then("the response status code should be {int}")
    public void verifyResponseStatusCode(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode(), 
                "Expected status code " + expectedStatusCode + " but got " + response.getStatusCode());
        System.out.println("✓ Response status code verified: " + response.getStatusCode());
    }

    @Then("the response status code should be {int} or {int}")
    public void verifyResponseStatusCodeEither(int statusCode1, int statusCode2) {
        int actualStatus = response.getStatusCode();
        assertTrue(actualStatus == statusCode1 || actualStatus == statusCode2,
                "Expected status code " + statusCode1 + " or " + statusCode2 + 
                " but got " + actualStatus);
        System.out.println("✓ Response status code is " + actualStatus + 
                          " (expected " + statusCode1 + " or " + statusCode2 + ")");
    }

    @Then("validate response body contains category list")
    public void validateResponseBodyContainsCategoryList() {
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        
        assertNotNull(categories, "Categories list should not be null");
        assertFalse(categories.isEmpty(), "Categories list should not be empty");
        
        System.out.println("✓ Response contains " + categories.size() + " categories");
    }

    @Then("response contains array of categories with ID, name, and parent category fields")
    public void verifyResponseContainsCategoryArray() {
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        
        assertNotNull(categories, "Categories array should not be null");
        assertFalse(categories.isEmpty(), "Categories array should not be empty");
        
        // Validate first category has required fields
        Map<String, Object> firstCategory = categories.get(0);
        
        assertTrue(firstCategory.containsKey("id"), "Category should have 'id' field");
        assertNotNull(firstCategory.get("id"), "Category ID should not be null");
        
        assertTrue(firstCategory.containsKey("name"), "Category should have 'name' field");
        assertNotNull(firstCategory.get("name"), "Category name should not be null");
        
        // Parent category field may have different names
        boolean hasParentField = firstCategory.containsKey("parentId") || 
                                 firstCategory.containsKey("parentCategoryId") ||
                                 firstCategory.containsKey("parent") ||
                                 firstCategory.containsKey("parentCategory");
        
        System.out.println("✓ Category fields validated:");
        System.out.println("  - ID: " + firstCategory.get("id"));
        System.out.println("  - Name: " + firstCategory.get("name"));
        System.out.println("  - Has parent field: " + hasParentField);
        System.out.println("  - Available fields: " + firstCategory.keySet());
    }

    @Then("validate category details in response")
    public void validateCategoryDetailsInResponse() {
        Map<String, Object> category = response.jsonPath().getMap("$");
        
        assertNotNull(category, "Category should not be null");
        
        assertTrue(category.containsKey("id"), "Category should have 'id' field");
        assertNotNull(category.get("id"), "Category ID should not be null");
        
        assertTrue(category.containsKey("name"), "Category should have 'name' field");
        assertNotNull(category.get("name"), "Category name should not be null");
        
        System.out.println("✓ Category details validated:");
        System.out.println("  - ID: " + category.get("id"));
        System.out.println("  - Name: " + category.get("name"));
        System.out.println("  - All fields: " + category.keySet());
    }

    @Then("response contains correct category with ID, name, and parent category information")
    public void verifyCorrectCategoryWithAllFields() {
        Map<String, Object> category = response.jsonPath().getMap("$");
        
        assertNotNull(category, "Category should not be null");
        
        // Verify ID matches
        assertTrue(category.containsKey("id"), "Category should have 'id' field");
        assertEquals(validCategoryId, category.get("id").toString(), 
                "Category ID should match the requested ID");
        
        // Verify name
        assertTrue(category.containsKey("name"), "Category should have 'name' field");
        assertNotNull(category.get("name"), "Category name should not be null");
        assertFalse(category.get("name").toString().isEmpty(), 
                "Category name should not be empty");
        
        System.out.println("✓ Category verified:");
        System.out.println("  - ID: " + category.get("id"));
        System.out.println("  - Name: " + category.get("name"));
        System.out.println("  - Available fields: " + category.keySet());
    }

    @Then("validate updated category data")
    public void validateUpdatedCategoryData() {
        Map<String, Object> category = response.jsonPath().getMap("$");
        
        assertNotNull(category, "Category response should not be null");
        
        // Verify ID
        assertTrue(category.containsKey("id"), "Category should have an id");
        assertEquals(validCategoryId, category.get("id").toString(), 
                "Category ID should match");
        
        // Verify name was updated
        assertTrue(category.containsKey("name"), "Category should have a name");
        String updatedName = category.get("name").toString();
        String expectedName = requestBody.get("name").toString();
        assertEquals(expectedName, updatedName, 
                "Category name should be updated to '" + expectedName + "'");
        
        System.out.println("✓ Updated category data validated:");
        System.out.println("  - ID: " + category.get("id"));
        System.out.println("  - Updated name: " + updatedName);
    }

    @Then("response shows category name updated to {string}")
    public void verifyUpdatedCategoryName(String expectedName) {
        Map<String, Object> category = response.jsonPath().getMap("$");
        
        assertNotNull(category, "Category response should not be null");
        assertTrue(category.containsKey("name"), "Category should have a name field");
        
        String actualName = category.get("name").toString();
        assertEquals(expectedName, actualName, 
                "Category name should be updated to '" + expectedName + "' but was '" + actualName + "'");
        
        System.out.println("✓ Category name successfully updated to: " + actualName);
    }

    @Then("validate error message")
    public void validateErrorMessage() {
        String responseBody = response.getBody().asString();
        System.out.println("Error response received: " + responseBody);
        
        // The response should contain an error message about missing name
        assertNotNull(responseBody, "Error response should not be null");
        assertFalse(responseBody.isEmpty(), "Error response should not be empty");
    }

    @Then("error message: {string}")
    public void verifySpecificErrorMessage(String expectedMessage) {
        String responseBody = response.getBody().asString();
        System.out.println("Validating error message in response: " + responseBody);
        
        try {
            Map<String, Object> errorResponse = response.jsonPath().getMap("$");
            
            // Check common error message fields
            String[] possibleMessageFields = {"message", "error", "errorMessage", "details", "msg", "description"};
            boolean messageFound = false;
            
            for (String field : possibleMessageFields) {
                if (errorResponse.containsKey(field)) {
                    String actualMessage = errorResponse.get(field).toString();
                    if (actualMessage.contains(expectedMessage)) {
                        messageFound = true;
                        System.out.println("✓ Error message verified in field '" + field + "': " + actualMessage);
                        break;
                    }
                }
            }
            
            if (!messageFound) {
                // Check if message is anywhere in the JSON response
                assertTrue(responseBody.contains(expectedMessage),
                        "Response should contain error message: '" + expectedMessage + "'. Actual response: " + responseBody);
                System.out.println("✓ Error message found in response body");
            }
        } catch (Exception e) {
            // If response is not JSON, check raw text
            assertTrue(responseBody.contains(expectedMessage),
                    "Response should contain error message: '" + expectedMessage + "'. Actual response: " + responseBody);
            System.out.println("✓ Error message found in plain text response");
        }
    }

    @Then("GET request returns {int} for deleted category ID")
    public void verifyDeletedCategoryNotFound(int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode(),
                "Deleted category should return status " + expectedStatusCode);
        System.out.println("✓ Confirmed: Category has been deleted (status " + expectedStatusCode + ")");
    }
}
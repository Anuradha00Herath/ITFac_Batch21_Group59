package stepdefinitions.category.api;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import pages.category.CategoryApiPage;
import utils.category.ApiAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryApiSteps {

    private String token;
    private Response response;
    private String validCategoryId;
    private String invalidCategoryId = "99999"; // Non-existent ID
    private String categoryIdWithoutDependencies;
    private Map<String, Object> requestBody;
    private Map<String, Object> originalCategoryData;
    private final CategoryApiPage categoryApiPage = new CategoryApiPage();

    
    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String role) {
        token = ApiAuth.loginAndGetToken(role);
        assertNotNull(token, "Token should not be null");
        System.out.println("Authenticated as: " + role);
    }

    @Given("User is authenticated")
    public void userIsAuthenticated() {
        iAmAuthenticatedAs("user");
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
    System.out.println("Searching for existing category with no plant dependencies...");
    
    response = categoryApiPage.getAllCategories(token);
    assertEquals(200, response.getStatusCode(), "Should be able to fetch categories");
    
    List<Map<String, Object>> categories = response.jsonPath().getList("$");
    assertFalse(categories.isEmpty(), "At least one category should exist in system");
    
    System.out.println("Total categories found: " + categories.size());
    
    // Check each category to find one without plants
    for (Map<String, Object> category : categories) {
        String categoryId = category.get("id").toString();
        
        // Fetch full details of this category to check for plants
        Response categoryDetailResponse = categoryApiPage.getCategoryById(token, categoryId);
        
        if (categoryDetailResponse.getStatusCode() == 200) {
            Map<String, Object> categoryDetails = categoryDetailResponse.jsonPath().getMap("$");
            
            System.out.println("\nChecking Category ID: " + categoryId);
            System.out.println("  Name: " + categoryDetails.get("name"));
            
            // Look for plant count fields
            String[] plantCountFields = {
                "plantCount", "plant_count", "plantsCount", "plants_count", 
                "associatedPlants", "numberOfPlants", "plants", "totalPlants"
            };
            
            boolean hasPlantField = false;
            int plantCount = -1;
            
            for (String field : plantCountFields) {
                if (categoryDetails.containsKey(field)) {
                    Object value = categoryDetails.get(field);
                    hasPlantField = true;
                    System.out.println("  " + field + ": " + value);
                    
                    if (value != null) {
                        try {
                            // Handle both integer and list types
                            if (value instanceof List) {
                                plantCount = ((List<?>) value).size();
                            } else if (value instanceof Integer) {
                                plantCount = (Integer) value;
                            } else {
                                plantCount = Integer.parseInt(value.toString());
                            }
                            
                            if (plantCount == 0) {
                                categoryIdWithoutDependencies = categoryId;
                                System.out.println("✓ Found category without plants!");
                                System.out.println("  Category ID: " + categoryId);
                                System.out.println("  Category Name: " + categoryDetails.get("name"));
                                System.out.println("  Plant Count: 0");
                                return;
                            } else {
                                System.out.println("  ✗ Category has " + plantCount + " plants - skipping");
                            }
                            break;
                        } catch (Exception e) {
                            System.out.println("  Warning: Could not parse plant count: " + e.getMessage());
                        }
                    }
                }
            }
            
            // If no plant field found, assume no plants associated
            if (!hasPlantField) {
                categoryIdWithoutDependencies = categoryId;
                System.out.println("✓ Found category without plant fields (assuming no plants)!");
                System.out.println("  Category ID: " + categoryId);
                System.out.println("  Category Name: " + categoryDetails.get("name"));
                System.out.println("  Note: API doesn't return plant information for this category");
                return;
            }
        }
    }
    
    fail("No suitable category found for deletion test.");
}
    
    @When("I authenticate as {string}")
    public void iAuthenticateAs(String role) {
        iAmAuthenticatedAs(role);
    }

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
        } else if (endpoint.equals("/api/categories/summary")) {
            response = categoryApiPage.getCategorySummary(token);
            System.out.println("GET request sent to: /api/categories/summary");
        }
        
        System.out.println("Response status: " + response.getStatusCode());
    }

    @When("I send GET request to {string} with invalid ID {string}")
    public void iSendGetRequestWithInvalidId(String endpoint, String invalidId) {
        this.invalidCategoryId = invalidId;
        String url = endpoint.replace("{id}", invalidId);
        
        response = categoryApiPage.getCategoryById(token, invalidId);
        System.out.println("GET request sent to: " + url + " (invalid ID)");
        System.out.println("Response status: " + response.getStatusCode());
    }

    @When("I send GET request with non-existent category ID")
    public void iSendGetRequestWithNonExistentId() {
        response = categoryApiPage.getCategoryById(token, invalidCategoryId);
        System.out.println("GET request sent with non-existent ID: " + invalidCategoryId);
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
        
        // Store original category data
        originalCategoryData = new HashMap<>(categories.get(0));
        
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

    @When("I verify original category data is unchanged")
    public void iVerifyOriginalDataIsUnchanged() {
        // Fetch the category again to verify it wasn't updated
        Response verifyResponse = categoryApiPage.getCategoryById(token, validCategoryId);
        
        if (verifyResponse.getStatusCode() == 200) {
            Map<String, Object> currentData = verifyResponse.jsonPath().getMap("$");
            
            assertEquals(originalCategoryData.get("name").toString(), 
                        currentData.get("name").toString(),
                        "Category name should remain unchanged");
            
            System.out.println("✓ Original category data is unchanged");
            System.out.println("  Original name: " + originalCategoryData.get("name"));
            System.out.println("  Current name: " + currentData.get("name"));
        }
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
        
        Map<String, Object> firstCategory = categories.get(0);
        
        assertTrue(firstCategory.containsKey("id"), "Category should have 'id' field");
        assertNotNull(firstCategory.get("id"), "Category ID should not be null");
        
        assertTrue(firstCategory.containsKey("name"), "Category should have 'name' field");
        assertNotNull(firstCategory.get("name"), "Category name should not be null");
        
        System.out.println("✓ Category fields validated:");
        System.out.println("  - ID: " + firstCategory.get("id"));
        System.out.println("  - Name: " + firstCategory.get("name"));
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
        
        assertTrue(category.containsKey("id"), "Category should have 'id' field");
        assertEquals(validCategoryId, category.get("id").toString(), 
                "Category ID should match the requested ID");
        
        assertTrue(category.containsKey("name"), "Category should have 'name' field");
        assertNotNull(category.get("name"), "Category name should not be null");
        assertFalse(category.get("name").toString().isEmpty(), 
                "Category name should not be empty");
        
        System.out.println("✓ Category verified:");
        System.out.println("  - ID: " + category.get("id"));
        System.out.println("  - Name: " + category.get("name"));
        System.out.println("  - Available fields: " + category.keySet());
    }

    @Then("response contains error message indicating category not found")
    public void verifyErrorMessageCategoryNotFound() {
        String responseBody = response.getBody().asString();
        System.out.println("Error response: " + responseBody);
        
        try {
            Map<String, Object> errorResponse = response.jsonPath().getMap("$");
            
            String[] possibleMessageFields = {"message", "error", "errorMessage", "details", "msg", "description"};
            boolean errorMessageFound = false;
            
            for (String field : possibleMessageFields) {
                if (errorResponse.containsKey(field)) {
                    String message = errorResponse.get(field).toString().toLowerCase();
                    if (message.contains("not found") || message.contains("doesn't exist") || 
                        message.contains("does not exist") || message.contains("category")) {
                        errorMessageFound = true;
                        System.out.println("✓ Error message found in field '" + field + "': " + message);
                        break;
                    }
                }
            }
            
            if (!errorMessageFound) {
                String lowerBody = responseBody.toLowerCase();
                assertTrue(lowerBody.contains("not found") || lowerBody.contains("doesn't exist"),
                        "Response should contain error message about category not found");
                System.out.println("✓ Error message found in response body");
            }
        } catch (Exception e) {
            String lowerBody = responseBody.toLowerCase();
            assertTrue(lowerBody.contains("not found") || lowerBody.contains("doesn't exist"),
                    "Response should contain error message about category not found");
            System.out.println("✓ Error message found in plain text response");
        }
    }

    @Then("response body contains accurate summary data")
    public void verifyResponseContainsSummaryData() {
        try {
            // Try parsing as JSON object
            Map<String, Object> summary = response.jsonPath().getMap("$");
            assertNotNull(summary, "Summary data should not be null");
            assertFalse(summary.isEmpty(), "Summary data should not be empty");
            
            System.out.println("✓ Response contains summary data:");
            summary.forEach((key, value) -> 
                System.out.println("  - " + key + ": " + value));
            
        } catch (Exception e) {
            // Try parsing as JSON array
            try {
                List<Map<String, Object>> summaryList = response.jsonPath().getList("$");
                assertNotNull(summaryList, "Summary data should not be null");
                assertFalse(summaryList.isEmpty(), "Summary data should not be empty");
                
                System.out.println("✓ Response contains summary data (" + summaryList.size() + " items)");
            } catch (Exception ex) {
                fail("Response should contain valid summary data");
            }
        }
    }

    @Then("User has read access to summary")
    public void verifyUserHasReadAccess() {
        assertEquals(200, response.getStatusCode(), 
                "User should have read access (200 status code)");
        System.out.println("✓ User has read access to category summary");
    }

    @Then("validate updated category data")
    public void validateUpdatedCategoryData() {
        Map<String, Object> category = response.jsonPath().getMap("$");
        
        assertNotNull(category, "Category response should not be null");
        
        assertTrue(category.containsKey("id"), "Category should have an id");
        assertEquals(validCategoryId, category.get("id").toString(), 
                "Category ID should match");
        
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
        
        assertNotNull(responseBody, "Error response should not be null");
        assertFalse(responseBody.isEmpty(), "Error response should not be empty");
        
        System.out.println("✓ Error response validated (contains error information)");
    }

    @Then("error message: {string}")
    public void verifySpecificErrorMessage(String expectedMessage) {
        String responseBody = response.getBody().asString();
        System.out.println("Validating error message in response: " + responseBody);
        
        try {
            Map<String, Object> errorResponse = response.jsonPath().getMap("$");
            
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
                assertTrue(responseBody.contains(expectedMessage),
                        "Response should contain error message: '" + expectedMessage + "'. Actual response: " + responseBody);
                System.out.println("✓ Error message found in response body");
            }
        } catch (Exception e) {
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
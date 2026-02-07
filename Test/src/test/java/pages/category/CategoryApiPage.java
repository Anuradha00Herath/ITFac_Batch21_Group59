package pages.category;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

import utils.category.Config;

import java.util.Map;

/**
 * Page Object for Category API endpoints
 * Encapsulates all REST API interactions for Category resources
 */
public class CategoryApiPage {

    // ================= CONSTANTS =================
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String BEARER_PREFIX = "Bearer ";

    // ================= BASE REQUEST =================

    /**
     * Creates base request specification with common configurations
     * @param token Authentication token
     * @return Configured RequestSpecification
     */
    private RequestSpecification getBaseRequest(String token) {
        return given()
            .baseUri(Config.BASE_URL)
            .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
            .log().ifValidationFails(); // Log only if request fails
    }

    /**
     * Creates base request with JSON content type for POST/PUT operations
     * @param token Authentication token
     * @return Configured RequestSpecification with JSON content type
     */
    private RequestSpecification getBaseRequestWithJson(String token) {
        return getBaseRequest(token)
            .contentType(CONTENT_TYPE_JSON);
    }

    // ================= GET OPERATIONS =================

    /**
     * Generic GET request to any endpoint
     * @param token Authentication token
     * @param endpoint API endpoint path
     * @return Response from the server
     */
    public Response rawGet(String token, String endpoint) {
        validateToken(token);
        validateEndpoint(endpoint);
        
        return getBaseRequest(token)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * Fetches all categories
     * GET /api/categories
     * @param token Authentication token
     * @return Response containing list of categories
     */
    public Response getAllCategories(String token) {
        validateToken(token);
        
        return getBaseRequest(token)
            .when()
            .get(Config.API_CATEGORIES_ALL)
            .then()
            .extract()
            .response();
    }

    /**
     * Fetches a specific category by ID
     * GET /api/categories/{id}
     * @param token Authentication token
     * @param categoryId Category identifier
     * @return Response containing category details or error
     */
    public Response getCategoryById(String token, String categoryId) {
        validateToken(token);
        validateId(categoryId);
        
        String endpoint = buildCategoryEndpoint(categoryId);
        
        return getBaseRequest(token)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * Fetches category summary information
     * GET /api/categories/summary
     * @param token Authentication token
     * @return Response containing category summary
     */
    public Response getCategorySummary(String token) {
        validateToken(token);
        
        return getBaseRequest(token)
            .when()
            .get(Config.API_CATEGORIES_SUMMARY)
            .then()
            .extract()
            .response();
    }

    // ================= POST OPERATIONS =================

    /**
     * Creates a new category
     * POST /api/categories
     * @param token Authentication token
     * @param requestBody Category data (name, parentId, etc.)
     * @return Response containing created category or error
     */
    public Response createCategory(String token, Map<String, Object> requestBody) {
        validateToken(token);
        validateRequestBody(requestBody);
        
        return getBaseRequestWithJson(token)
            .body(requestBody)
            .when()
            .post(Config.API_CATEGORIES_ALL)
            .then()
            .extract()
            .response();
    }

    // ================= PUT OPERATIONS =================

    /**
     * Updates an existing category
     * PUT /api/categories/{id}
     * @param token Authentication token
     * @param categoryId Category identifier
     * @param requestBody Updated category data
     * @return Response containing updated category or error
     */
    public Response updateCategory(String token, String categoryId, Map<String, Object> requestBody) {
        validateToken(token);
        validateId(categoryId);
        validateRequestBody(requestBody);
        
        String endpoint = buildCategoryEndpoint(categoryId);
        
        return getBaseRequestWithJson(token)
            .body(requestBody)
            .when()
            .put(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * Partially updates a category (PATCH)
     * PATCH /api/categories/{id}
     * @param token Authentication token
     * @param categoryId Category identifier
     * @param requestBody Partial update data
     * @return Response containing updated category or error
     */
    public Response patchCategory(String token, String categoryId, Map<String, Object> requestBody) {
        validateToken(token);
        validateId(categoryId);
        validateRequestBody(requestBody);
        
        String endpoint = buildCategoryEndpoint(categoryId);
        
        return getBaseRequestWithJson(token)
            .body(requestBody)
            .when()
            .patch(endpoint)
            .then()
            .extract()
            .response();
    }

    // ================= DELETE OPERATIONS =================

    /**
     * Deletes a category
     * DELETE /api/categories/{id}
     * @param token Authentication token
     * @param categoryId Category identifier
     * @return Response indicating success or error
     */
    public Response deleteCategory(String token, String categoryId) {
        validateToken(token);
        validateId(categoryId);
        
        String endpoint = buildCategoryEndpoint(categoryId);
        
        return getBaseRequest(token)
            .when()
            .delete(endpoint)
            .then()
            .extract()
            .response();
    }

    // ================= UTILITY METHODS =================

    /**
     * Builds endpoint path for specific category
     * @param categoryId Category identifier
     * @return Full endpoint path
     */
    private String buildCategoryEndpoint(String categoryId) {
        return Config.API_CATEGORIES_ALL + "/" + categoryId;
    }

    /**
     * Validates authentication token
     * @param token Token to validate
     * @throws IllegalArgumentException if token is null or empty
     */
    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Authentication token cannot be null or empty");
        }
    }

    /**
     * Validates category ID
     * @param categoryId ID to validate
     * @throws IllegalArgumentException if ID is null or empty
     */
    private void validateId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
    }

    /**
     * Validates endpoint path
     * @param endpoint Endpoint to validate
     * @throws IllegalArgumentException if endpoint is null or empty
     */
    private void validateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint cannot be null or empty");
        }
    }

    /**
     * Validates request body
     * Note: Empty body is allowed (for testing validation scenarios)
     * @param requestBody Request body to validate
     * @throws IllegalArgumentException if body is null
     */
    private void validateRequestBody(Map<String, Object> requestBody) {
        if (requestBody == null) {
            throw new IllegalArgumentException("Request body cannot be null (use empty Map for empty body)");
        }
    }

    // ================= ADVANCED OPERATIONS =================

    /**
     * Searches categories by name (if supported by API)
     * GET /api/categories?name={searchTerm}
     * @param token Authentication token
     * @param searchTerm Search term
     * @return Response containing matching categories
     */
    public Response searchCategoriesByName(String token, String searchTerm) {
        validateToken(token);
        
        return getBaseRequest(token)
            .queryParam("name", searchTerm)
            .when()
            .get(Config.API_CATEGORIES_ALL)
            .then()
            .extract()
            .response();
    }

    /**
     * Gets categories with pagination (if supported by API)
     * GET /api/categories?page={page}&size={size}
     * @param token Authentication token
     * @param page Page number (0-based)
     * @param size Page size
     * @return Response containing paginated categories
     */
    public Response getCategoriesWithPagination(String token, int page, int size) {
        validateToken(token);
        
        return getBaseRequest(token)
            .queryParam("page", page)
            .queryParam("size", size)
            .when()
            .get(Config.API_CATEGORIES_ALL)
            .then()
            .extract()
            .response();
    }

    /**
     * Gets child categories of a parent category
     * GET /api/categories/{id}/children
     * @param token Authentication token
     * @param parentCategoryId Parent category ID
     * @return Response containing child categories
     */
    public Response getChildCategories(String token, String parentCategoryId) {
        validateToken(token);
        validateId(parentCategoryId);
        
        String endpoint = buildCategoryEndpoint(parentCategoryId) + "/children";
        
        return getBaseRequest(token)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
    }
}
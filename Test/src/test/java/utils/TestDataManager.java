package utils;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class TestDataManager {
    
    private static TestDataManager instance;
    private Response lastResponse;
    private String authToken;
    private String userToken;
    private String adminToken;
    private Map<String, Object> testData;
    
    private TestDataManager() {
        this.testData = new HashMap<>();
    }
    
    public static synchronized TestDataManager getInstance() {
        if (instance == null) {
            instance = new TestDataManager();
        }
        return instance;
    }
    
    // For backward compatibility
    public void setResponse(Response response) {
        this.lastResponse = response;
    }
    
    public Response getResponse() {
        return lastResponse;
    }
    
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    public String getAuthToken() {
        if (authToken == null) {
            // Generate default token if not set
            authToken = generateDefaultToken();
        }
        return authToken;
    }
    
    public void setUserToken(String token) {
        this.userToken = token;
    }
    
    public String getUserToken() {
        if (userToken == null) {
            userToken = generateUserToken();
        }
        return userToken;
    }
    
    public void setAdminToken(String token) {
        this.adminToken = token;
    }
    
    public String getAdminToken() {
        if (adminToken == null) {
            adminToken = generateAdminToken();
        }
        return adminToken;
    }
    
    public void setTestData(String key, Object value) {
        testData.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getTestData(String key) {
        return (T) testData.get(key);
    }
    
    public <T> T getTestData(String key, Class<T> type) {
        return type.cast(testData.get(key));
    }
    
    public void clearTestData() {
        testData.clear();
    }
    
    public void clearAll() {
        lastResponse = null;
        authToken = null;
        userToken = null;
        adminToken = null;
        testData.clear();
    }
    
    // Token generation methods - These should be implemented based on your authentication system
    private String generateDefaultToken() {
        // Implement based on your authentication
        // Example: Call your login API
        return "default_jwt_token";
    }
    
    private String generateUserToken() {
        // Example implementation - replace with actual authentication
        /*
        Response response = given()
            .contentType("application/json")
            .body("{\"username\":\"user\",\"password\":\"userpass\"}")
            .when()
            .post("/api/auth/login");
        
        return response.jsonPath().getString("token");
        */
        return "user_jwt_token";
    }
    
    private String generateAdminToken() {
        // Example implementation - replace with actual authentication
        /*
        Response response = given()
            .contentType("application/json")
            .body("{\"username\":\"admin\",\"password\":\"adminpass\"}")
            .when()
            .post("/api/auth/login");
        
        return response.jsonPath().getString("token");
        */
        return "admin_jwt_token";
    }
    
    // Helper method to store response for later assertions
    public void storeResponse(Response response) {
        setResponse(response);
        // You can also extract and store specific data from response
        if (response.getStatusCode() == 200) {
            // Extract and store relevant data
            // testData.put("responseData", response.jsonPath().get());
        }
    }
    
    // Method to get stored plant data
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPlantData() {
        return (Map<String, Object>) testData.get("plantData");
    }
    
    // Method to get initial plant count
    public Integer getInitialPlantCount() {
        return (Integer) testData.get("initialPlantCount");
    }
}
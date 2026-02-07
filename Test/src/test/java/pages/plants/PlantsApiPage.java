package pages.plants;

import io.restassured.response.Response;
import utils.ApiRequestBuilder;
import utils.TestDataManager;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class PlantsApiPage {
    
    private final TestDataManager testDataManager;
    
    public PlantsApiPage() {
        this.testDataManager = TestDataManager.getInstance();
    }
    
    // TC_USER_API_PMM_01: Get empty plant list
    public Response getPlantsAsUser() {
        Response response = given()
                .spec(ApiRequestBuilder.getUserRequest())
                .when()
                .get("/api/plants");
        
        testDataManager.setResponse(response);
        return response;
    }
    
    // TC_USER_API_PMM_02: Attempt to create plant as user (should fail)
    public Response attemptCreatePlantAsUser() {
        Map<String, Object> plantData = new HashMap<>();
        plantData.put("name", "Violet");
        plantData.put("categoryId", 3); // Assuming this exists
        plantData.put("price", 20);
        plantData.put("quantity", 10);
        
        Response response = given()
                .spec(ApiRequestBuilder.getUserRequest())
                .body(plantData)
                .when()
                .post("/api/plants");
        
        testDataManager.setResponse(response);
        testDataManager.setTestData("plantData", plantData);
        return response;
    }
    
    // Helper method: Create plant as admin (for setup)
    public Response createPlantAsAdmin(Map<String, Object> plantData) {
        Response response = given()
                .spec(ApiRequestBuilder.getAdminRequest())
                .body(plantData)
                .when()
                .post("/api/plants");
        
        testDataManager.setResponse(response);
        return response;
    }
    
    // Helper method: Get plant count
    public int getPlantCount() {
        Response response = given()
                .spec(ApiRequestBuilder.getAdminRequest())
                .when()
                .get("/api/plants");
        
        // Assuming response is array of plants
        return response.jsonPath().getList("$").size();
    }
    
    // Helper method: Delete all plants (admin only)
    public void deleteAllPlants() {
        // Implementation depends on your API
        // Option 1: If you have bulk delete endpoint
        given().spec(ApiRequestBuilder.getAdminRequest())
               .when().delete("/api/plants");
        
        // Option 2: Delete one by one
        Response response = given()
                .spec(ApiRequestBuilder.getAdminRequest())
                .when()
                .get("/api/plants");
        
        // Extract IDs and delete each
        // ... implementation based on your API
    }
    
    // Helper method: Search for plant by name
    public Response searchPlantByName(String name) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", name);
        
        Response response = given()
                .spec(ApiRequestBuilder.withQueryParams(
                    ApiRequestBuilder.getAdminRequest(), 
                    queryParams
                ))
                .when()
                .get("/api/plants/search");
        
        testDataManager.setResponse(response);
        return response;
    }
}
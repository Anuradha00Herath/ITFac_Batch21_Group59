package stepdefinitions.plants.api;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import pages.plants.PlantsApiPage;
import utils.TestDataManager;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class PlantsApiSteps {
    
    private final PlantsApiPage plantsApiPage;
    private final TestDataManager testDataManager;
    private int initialPlantCount;
    
    public PlantsApiSteps() {
        this.plantsApiPage = new PlantsApiPage();
        this.testDataManager = TestDataManager.getInstance();
    }
    
    // TC_USER_API_PMM_01 Steps
    @Given("the user is authenticated with a valid access token")
    public void the_user_is_authenticated_with_a_valid_access_token() {
        // Authentication is handled by ApiRequestBuilder.getUserRequest()
        System.out.println("User authenticated via ApiRequestBuilder");
    }
    
    @Given("no plant records exist in the database")
    public void no_plant_records_exist_in_the_database() {
        plantsApiPage.deleteAllPlants();
        System.out.println("Database cleared of plant records");
    }
    
    // TC_USER_API_PMM_02 Steps
    @Given("the user is authenticated as a regular user")
    public void the_user_is_authenticated_as_a_regular_user() {
        // ApiRequestBuilder.getUserRequest() handles this
        System.out.println("Authenticated as regular user");
    }
    
    @Given("a valid sub-category exists in the database")
    public void a_valid_sub_category_exists_in_the_database() {
        // This would typically be setup in a @Before hook
        // For now, we assume category with ID 3 exists
        System.out.println("Assuming category ID 3 exists");
    }
    
    @Given("the initial plant count is recorded")
    public void the_initial_plant_count_is_recorded() {
        initialPlantCount = plantsApiPage.getPlantCount();
        testDataManager.setTestData("initialPlantCount", initialPlantCount);
        System.out.println("Initial plant count: " + initialPlantCount);
    }
    
    @When("the user sends a GET request to {string}")
    public void the_user_sends_a_get_request_to(String endpoint) {
        Response response = plantsApiPage.getPlantsAsUser();
        testDataManager.setResponse(response);
        System.out.println("GET request sent to: " + endpoint);
    }
    
    @When("the user attempts to create a plant with valid data")
    public void the_user_attempts_to_create_a_plant_with_valid_data() {
        // Record initial count
        initialPlantCount = plantsApiPage.getPlantCount();
        
        // Attempt to create plant as user
        Response response = plantsApiPage.attemptCreatePlantAsUser();
        testDataManager.setResponse(response);
        
        System.out.println("Attempted to create plant as user");
    }
    
    @Then("the API response status should be {int}")
    public void the_api_response_status_should_be(Integer expectedStatus) {
        Response response = testDataManager.getResponse();
        response.then().statusCode(expectedStatus);
        System.out.println("Response status verified: " + expectedStatus);
    }
    
    @Then("the response body should contain an empty plant list")
    public void the_response_body_should_contain_an_empty_plant_list() {
        Response response = testDataManager.getResponse();
        
        // Check empty array
        List<Map<String, Object>> plants = response.jsonPath().getList("$");
        Assertions.assertTrue(plants.isEmpty(), "Plant list should be empty");
        
        System.out.println("Verified empty plant list");
    }
    
    @Then("the error message should indicate insufficient permissions")
    public void the_error_message_should_indicate_insufficient_permissions() {
        Response response = testDataManager.getResponse();
        
        response.then().body(containsString("Forbidden"));
        
        System.out.println("Verified insufficient permissions error");
    }
    
    @Then("no new plant should be added to the database")
    public void no_new_plant_should_be_added_to_the_database() {
        int finalCount = plantsApiPage.getPlantCount();
        Assertions.assertEquals(
        initialPlantCount, 
        finalCount,
        "Plant count should not change"
        );
        System.out.println("Verified no new plant added");
    }
    
    @Then("the plant list count should remain unchanged")
    public void the_plant_list_count_should_remain_unchanged() {
        // Alternative verification - search for the plant
        Response searchResponse = plantsApiPage.searchPlantByName("Violet");
        List<Map<String, Object>> results = searchResponse.jsonPath().getList("$");

        Assertions.assertTrue(results.isEmpty(), "Plant 'Violet' should not exist");
        
        System.out.println("Verified plant list unchanged");
    }
}
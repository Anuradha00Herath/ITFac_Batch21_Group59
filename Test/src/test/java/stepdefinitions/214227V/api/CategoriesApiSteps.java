package steps.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import support.ApiAuth;
import support.Config;
import support.TestContext;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Random;

import pages.api.CategoryApi;
import static org.junit.jupiter.api.Assertions.*;

public class CategoriesApiSteps {
    private static final ObjectMapper OM = new ObjectMapper();
    private final TestContext ctx;
    CategoryApi categoryApi;    
    Response response;

    public CategoriesApiSteps(TestContext ctx) {
        this.ctx = ctx;
        categoryApi = new CategoryApi(ctx);
    }

    public String getRandomString(int length) {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    Random random = new Random();
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < length; i++) {
        result.append(characters.charAt(random.nextInt(characters.length())));
    }

    return result.toString();
}

    @Given("I am authenticated as {string}")
    public void i_am_authenticated_as(String role) {
        role = role.trim().replace("\"", "");
        if (!"admin".equals(role) && !"user".equals(role)) {
            throw new IllegalArgumentException("Unknown role \"" + role + "\". Use \"admin\" or \"user\".");
        }

        String authHeader = ApiAuth.getAuthHeader(role);
        if ("admin".equals(role)) ctx.adminAuthHeader = authHeader;
        else ctx.userAuthHeader = authHeader;

        assertNotNull(authHeader);
        assertFalse(authHeader.isBlank());
    }

    //get sub-categories tests

    @When("I send a GET request to the sub-categories API as {string}")
    public void i_send_get_request_to_sub_categories_api(String role) {
        response  = categoryApi.viewAllSubCategories(role);
    }

    @Then("the api response status should be {int}")
    public void i_verify_sub_category_api_response_code(Integer intCode) {
        assertEquals(intCode, response.statusCode(), "Response body: " + response.asString());
    }

    // @Then("the response should contain sub-categories")
    // public void the_response_should_contain_sub_categories() {
    //     assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

    //     try {
    //         JsonNode json = OM.readTree(ctx.lastBodyText);
    //         JsonNode subCategoriesNode = json.get("subCategories");
    //         assertNotNull(subCategoriesNode, "Response does not contain 'subCategories'. Body: " + ctx.lastBodyText);
    //         assertTrue(subCategoriesNode.isArray(), "'subCategories' should be an array. Body: " + ctx.lastBodyText);
    //         assertTrue(subCategoriesNode.size() > 0, "Sub-categories array is empty. Body: " + ctx.lastBodyText);
    //     } catch (Exception e) {
    //         throw new RuntimeException("Response is not valid JSON. Body: " + ctx.lastBodyText, e);
    //     }
    // }
//     @Then("the response should contain sub-categories")
// public void the_response_should_contain_sub_categories() {
//     assertEquals(200, ctx.lastStatus, "Body: " + ctx.lastBodyText);

//     try {
//         JsonNode json = OM.readTree(ctx.lastBodyText);

//         assertTrue(json.isArray(), "Response is not an array. Body: " + ctx.lastBodyText);

//         // Check each object in the array
//         for (JsonNode obj : json) {
//             assertTrue(obj.has("subCategories"), "Object does not contain 'subCategories'. Body: " + obj.toString());
//             assertTrue(obj.get("subCategories").isArray(), "'subCategories' should be an array. Body: " + obj.toString());
//         }

//     } catch (Exception e) {
//         throw new RuntimeException("Response is not valid JSON. Body: " + ctx.lastBodyText, e);
//     }
// }

    //get main-categories tests

    @When("I send a GET request to the main categories API as {string}")
    public void i_send_get_request_to_main_categories_api(String role) {
        response  = categoryApi.viewAllMainCategories(role);
    }

    //add category tests

    @When("I send a POST request to the add main category API as {string}")
    public void i_send_post_request_to_add_categories_api(String role) {
        response  = categoryApi.addCategories(role, getRandomString(5));
    }

    @Then("the POST category API response status should be {int}")
    public void i_verify_post_categories_api_response_code(Integer intCode) {
        assertEquals(intCode, response.statusCode(), "Response body: " + response.asString());
    }

    @Then("the post api response body should indicate {string}")
    public void i_verify_post_categories_api_response_message(String message) {
        assertTrue(response.asString().contains(message),"Response body: " + response.asString());
    }

    //delete category tests
    
    @When("I send a DELETE request to the delete category API for id {int} as {string}")
    public void i_send_delete_request_to_add_categories_api(Integer id, String role) {
        response  = categoryApi.deleteCategories(id,role);
    }

    @Then("the delete API response status should be {int}")
    public void i_verify_delete_categories_api_response_code(Integer intCode) {
        assertEquals(intCode, response.statusCode(), "Response body: " + response.asString());
    }

    @Then("the delete api response body should indicate {string}")
    public void i_verify_delete_categories_api_response_message(String message) {
        assertTrue(response.asString().contains(message),"Response body: " + response.asString());
    }

    //add categories with more tha 10 characters 

    @When("I send a POST request to the add main category API with more than 10 characters as {string}")
    public void i_send_post_request_to_add_categories_more_than_max_char_api(String role) {
        response  = categoryApi.addCategories(role, getRandomString(11));
    }

    @Then("the POST category API response status for more than max character count should be {int}")
    public void i_verify_post_categories_api_response_code_for_more_than_max_char(Integer intCode) {
        assertEquals(intCode, response.statusCode(), "Response body: " + response.asString());
    }

    @Then("the POST api response code for more than max character count should indicate {string}")
    public void i_verify_post_categories_api_response_code_message_for_more_than_max_char(String message) {
        String actualError = response.jsonPath().getString("error");
        assertEquals(message, actualError,"Response body: " + response.asString());
    }

    @Then("the POST api response body for more than max character count should indicate {string}")
    public void i_verify_post_categories_api_response_message_for_more_than_max_char(String message) {
        assertTrue(response.asString().contains(message),"Response body: " + response.asString());
    }

    //admin add main category 

    @Then("the POST valid category API response status should be {int}")
    public void i_verify_post_categories_api_response_code_valid_category(Integer intCode) {
        assertEquals(intCode, response.statusCode(), "Response body: " + response.asString());
    }

    // @Then("the POST api valid main category response body should indicate {string}")
    // public void i_verify_post_categories_api_response_message_valid_min_category(String message) {
    //     String actualMessage = response.jsonPath().getString("message");

    //     assertEquals(message, actualMessage, "Response body: " + response.asString()+" Actual message: " + actualMessage);
    //     //assertTrue(response.asString().contains(message),"Response body: " + response.asString());
    // }

        //admin add sub category 


    @When("I send a POST request to the add sub category API as {string}")
    public void i_send_post_request_to_add__subcategories_api(String role) {
        response  = categoryApi.addSubCategories(role, getRandomString(11), "vQGKL");
    }

    //pagination

    @When("I send a GET request to the categories API with pagination as {string}")
    public void i_send_post_request_to_get__categories_with_api(String role) {
        response  = categoryApi.viewCategoriesWithPagination(role);
    }


}

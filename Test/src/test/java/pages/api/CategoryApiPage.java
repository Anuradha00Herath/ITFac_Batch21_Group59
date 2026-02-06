package pages.api;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import support.Config;
import java.util.Map;

public class CategoryApiPage {

    public Response getAllCategories(String token) {
        return given()
                .baseUri(Config.BASE_URL)
                .header("Authorization", "Bearer " + token)
                .when()
                .get(Config.API_CATEGORIES_ALL)
                .then()
                .extract()
                .response();
    }

    public Response getCategoryById(String token, String categoryId) {
        return given()
                .baseUri(Config.BASE_URL)
                .header("Authorization", "Bearer " + token)
                .when()
                .get(Config.API_CATEGORIES_ALL + "/" + categoryId)
                .then()
                .extract()
                .response();
    }

    public Response updateCategory(String token, String categoryId, Map<String, Object> requestBody) {
        return given()
                .baseUri(Config.BASE_URL)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .put(Config.API_CATEGORIES_ALL + "/" + categoryId)
                .then()
                .extract()
                .response();
    }

    public Response deleteCategory(String token, String categoryId) {
        return given()
                .baseUri(Config.BASE_URL)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(Config.API_CATEGORIES_ALL + "/" + categoryId)
                .then()
                .extract()
                .response();
    }
}
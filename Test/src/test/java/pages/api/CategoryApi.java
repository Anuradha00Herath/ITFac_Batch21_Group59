package pages.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import support.Config;
import support.TestContext;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

public class CategoryApi {

    private final TestContext ctx;

    public CategoryApi(TestContext ctx) {
        this.ctx = ctx;
    }

    private String authFor(String role) {

        if ("admin".equals(role)) {
            if (ctx.adminAuthHeader == null)
                throw new IllegalStateException("Admin not authenticated");
            return ctx.adminAuthHeader;
        }

        if ("user".equals(role)) {
            if (ctx.userAuthHeader == null)
                throw new IllegalStateException("User not authenticated");
            return ctx.userAuthHeader;
        }

        throw new IllegalArgumentException("Role must be admin or user");
    }

    public Response viewAllSubCategories(String role) {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SUB_CATEGORIES_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        return res;
    }

    public Response viewAllMainCategories(String role) {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_MAIN_CATEGORIES_ALL);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        return res;
    }

    public Response addCategories(String role, String catName) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", catName);
        // requestBody.put("parent", parent);
        // requestBody.put("subCategories", subCategories);

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post(Config.API_ADD_CATEGORY);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        return res;
    }

    public Response deleteCategories(Integer id, String role) {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .delete(Config.API_DELETE_CATEGORIES(id.toString()));

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        return res;
    }

    public Response addSubCategories(String role, String catName, String parent) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", catName);
        requestBody.put("parent", parent);

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .body(requestBody)
                        .when()
                        .post(Config.API_ADD_CATEGORY);

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        return res;
    }

    public Response viewCategoriesWithPagination(String role) {

        Response res =
                RestAssured.given()
                        .baseUri(Config.BASE_URL)
                        .header("Authorization", authFor(role))
                        .contentType("application/json")
                        .when()
                        .get(Config.API_SUB_CATEGORIES_ALL+"?page=2");

        ctx.lastResponse = res;
        ctx.lastStatus = res.statusCode();
        ctx.lastBodyText = res.asString();

        return res;
    }
}
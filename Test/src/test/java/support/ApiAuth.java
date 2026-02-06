package support;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiAuth {

    public static String loginAndGetToken(String role) {

        Config.Creds creds = Config.creds(role);

        Response response =
                given()
                        .baseUri(Config.BASE_URL)
                        .contentType("application/json")
                        .body("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(creds.username(), creds.password()))
                        .post(Config.API_LOGIN);

        return response.jsonPath().getString("token");
    }
}

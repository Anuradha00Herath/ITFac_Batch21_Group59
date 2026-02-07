package utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class AuthManager {
    private static String adminToken;
    private static final String BASE_URL = "http://localhost:8080";
    private static final ObjectMapper OM = new ObjectMapper();

    static String admin_username = EnvConfig.get("ADMIN_USERNAME");
    static String admin_password = EnvConfig.get("ADMIN_PASSWORD");


    public static String getAdminToken() {
        if (adminToken == null) {
            Response res = RestAssured.given()
                    .baseUri(BASE_URL)
                    .contentType("application/json")
                    .body(Map.of("username", admin_username, "password", admin_password))
                    .post("/api/auth/login");

            if (res.statusCode() == 200) {
                try {
                    JsonNode json = OM.readTree(res.asString());
                    String token = json.has("token") ? json.get("token").asText() :
                            json.has("accessToken") ? json.get("accessToken").asText() :
                                    json.get("jwt").asText();
                    adminToken = "Bearer " + token;
                } catch (Exception e) {
                    throw new RuntimeException("Auth parsing failed", e);
                }
            } else {
                throw new RuntimeException("Auth failed: " + res.statusCode());
            }
        }
        return adminToken;
    }
}
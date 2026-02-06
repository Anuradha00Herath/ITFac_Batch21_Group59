package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static utils.Config.*;

public class ApiAuth {
    private static final ObjectMapper OM = new ObjectMapper();

    /**
     * Returns the full Authorization header value:
     * - "Bearer <jwt>" if login returns a token
     * - or "Basic <base64>" if backend requires Basic Auth
     */
    public static String getAuthHeader(String role) {
        Creds c = creds(role);

        Response res =
                RestAssured.given()
                        .baseUri(BASE_URL)
                        .contentType("application/json")
                        .body(Map.of("username", c.username(), "password", c.password()))
                        .when()
                        .post(API_LOGIN);

        String text = res.asString();

        if (res.statusCode() >= 200 && res.statusCode() < 300) {
            try {
                JsonNode json = OM.readTree(text);
                String token = firstNonBlank(
                        json.path("token").asText(null),
                        json.path("accessToken").asText(null),
                        json.path("jwt").asText(null)
                );
                if (token == null) throw new RuntimeException("Login OK but token missing. Body: " + text);
                return "Bearer " + token;
            } catch (Exception e) {
                throw new RuntimeException("Login OK but response is not valid JSON. Body: " + text, e);
            }
        }

        // Basic fallback heuristic (matches TS)
        if (res.statusCode() == 401 && text != null && text.toLowerCase().matches(".*(basic auth|use basic|basic).*")) {
            String basic = Base64.getEncoder().encodeToString((c.username() + ":" + c.password()).getBytes(StandardCharsets.UTF_8));
            return "Basic " + basic;
        }

        throw new RuntimeException("Login failed (" + res.statusCode() + "): " + text);
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}

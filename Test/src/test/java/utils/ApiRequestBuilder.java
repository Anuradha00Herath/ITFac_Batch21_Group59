package utils;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

import static utils.sales.Config.BASE_URL;

public class ApiRequestBuilder {
    
    private static final Map<String, String> roleAuthHeaders = new HashMap<>();
    
    /**
     * Get base request specification (no auth)
     */
    public static RequestSpecification getBaseRequest() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(RestAssuredConfig.config()
                        .logConfig(LogConfig.logConfig()
                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }
    
    /**
     * Get request with authentication for a specific role
     * Uses your existing ApiAuth.getAuthHeader()
     */
    public static RequestSpecification getRequestForRole(String role) {
        // Cache auth headers to avoid repeated login calls
        String authHeader = roleAuthHeaders.computeIfAbsent(role, r -> {
            return ApiAuth.getAuthHeader(r);
        });
        
        return new RequestSpecBuilder()
                .addRequestSpecification(getBaseRequest())
                .addHeader("Authorization", authHeader)
                .build();
    }
    
    /**
     * Get request for ADMIN role
     */
    public static RequestSpecification getAdminRequest() {
        return getRequestForRole("ADMIN");
    }
    
    /**
     * Get request for USER role
     */
    public static RequestSpecification getUserRequest() {
        return getRequestForRole("USER");
    }
    
    /**
     * Get request with custom token
     */
    public static RequestSpecification getRequestWithToken(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(getBaseRequest())
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }
    
    /**
     * Clear cached auth headers (useful when tokens expire)
     */
    public static void clearAuthCache() {
        roleAuthHeaders.clear();
    }
    
    /**
     * Clear cached auth for specific role
     */
    public static void clearAuthCache(String role) {
        roleAuthHeaders.remove(role);
    }
    
    /**
     * Create request with query parameters
     */
    public static RequestSpecification withQueryParams(RequestSpecification spec, Map<String, Object> params) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addRequestSpecification(spec);
        params.forEach(builder::addQueryParam);
        return builder.build();
    }
    
    /**
     * Create request with path parameters
     */
    public static RequestSpecification withPathParams(RequestSpecification spec, Map<String, Object> params) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addRequestSpecification(spec);
        params.forEach(builder::addPathParam);
        return builder.build();
    }
    
    /**
     * Create request with custom headers
     */
    public static RequestSpecification withHeaders(RequestSpecification spec, Map<String, String> headers) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addRequestSpecification(spec);
        headers.forEach(builder::addHeader);
        return builder.build();
    }
    
    /**
     * Build a complete request with all parameters
     */
    public static RequestSpecification buildRequest(
            RequestSpecification baseSpec,
            Map<String, String> headers,
            Map<String, Object> queryParams,
            Map<String, Object> pathParams,
            Object body) {
        
        RequestSpecBuilder builder = new RequestSpecBuilder().addRequestSpecification(baseSpec);
        
        if (headers != null) {
            headers.forEach(builder::addHeader);
        }
        
        if (queryParams != null) {
            queryParams.forEach(builder::addQueryParam);
        }
        
        if (pathParams != null) {
            pathParams.forEach(builder::addPathParam);
        }
        
        if (body != null) {
            builder.setBody(body);
        }
        
        return builder.build();
    }
    
    /**
     * Helper method to add query params to any request
     */
    public static RequestSpecification addQueryParams(RequestSpecification spec, String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return withQueryParams(spec, params);
    }
    
    /**
     * Helper method to add single header
     */
    public static RequestSpecification addHeader(RequestSpecification spec, String key, String value) {
        Map<String, String> headers = new HashMap<>();
        headers.put(key, value);
        return withHeaders(spec, headers);
    }
}
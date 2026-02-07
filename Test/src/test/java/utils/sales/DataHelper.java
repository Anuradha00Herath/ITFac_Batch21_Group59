package utils.sales;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import utils.AuthManager;

public class DataHelper {
    private static final String BASE_URL = "http://localhost:8080";
    private static final ObjectMapper OM = new ObjectMapper();

    public static void seedSales(APIRequestContext request, int count) {
        String token = AuthManager.getAdminToken();

        for (int i = 1; i <= count; i++) {
            int plantId = 1;
            int qty = 1;

            String url = BASE_URL + "/api/sales/plant/" + plantId + "?quantity=" + qty;

            APIResponse res = request.post(
                    url,
                    RequestOptions.create()
                            .setHeader("Authorization", token)
            );

            if (res.status() < 200 || res.status() >= 300) {
                throw new RuntimeException(
                        "Seeding sale failed at i=" + i +
                                " status=" + res.status() +
                                " body=" + res.text()
                );
            }
        }
    }

    public static void deleteAllSales(APIRequestContext request) {
        String token = AuthManager.getAdminToken();
        String salesUrl = BASE_URL + "/api/sales";

        APIResponse getResponse = request.get(salesUrl,
                RequestOptions.create().setHeader("Authorization", token));

        if (getResponse.status() == 200) {
            try {
                JsonNode salesArray = OM.readTree(getResponse.text());

                // 2. Loop through each sale and delete it by ID
                for (JsonNode sale : salesArray) {
                    String id = sale.get("id").asText();
                    request.delete(salesUrl + "/" + id,
                            RequestOptions.create().setHeader("Authorization", token));
                }
                System.out.println("Clean up successful: Deleted all found sales.");
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse sales for cleanup", e);
            }
        } else {
            System.out.println("No sales found to delete or GET failed.");
        }
    }
}
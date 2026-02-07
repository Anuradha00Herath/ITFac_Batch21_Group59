package pages.sales;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PlantsPage {
    private final Page page;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String SEL_TABLE = "table";

    public PlantsPage(Page page) {
        this.page = page;
    }

    public void openPlantsList() {
        page.navigate(BASE_URL + "/ui/plants");
        page.locator(SEL_TABLE).first().waitFor();
    }

    // Reads stock for a given plant name by locating the row and extracting a number from it.
    // This avoids needing exact "stock column index" when you’re not sure.
    public int getStockForPlant(String plantName) {
        page.navigate(BASE_URL + "/ui/plants");
        page.locator("table").first().waitFor();

        int nameCol = getColumnIndexByHeader("name");   // or "plant" if your header is Plant Name
        int stockCol = getColumnIndexByHeader("stock");

        Locator rows = page.locator("table tbody tr");
        int rowCount = rows.count();

        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);
            String nameCell = row.locator("td").nth(nameCol).innerText().trim();

            if (nameCell.equalsIgnoreCase(plantName) || nameCell.contains(plantName)) {
                String stockText = row.locator("td").nth(stockCol).innerText().trim();
                // keep digits only
                stockText = stockText.replaceAll("[^0-9]", "");
                return Integer.parseInt(stockText);
            }
        }

        throw new RuntimeException("Plant not found in table: " + plantName);
    }

    public String findAnyPlantNameWithStockAtLeast(int minStock) {
        page.navigate(BASE_URL + "/ui/plants");
        page.locator("table").first().waitFor();

        int nameCol = getColumnIndexByHeader("name");
        int stockCol = getColumnIndexByHeader("stock");

        Locator rows = page.locator("table tbody tr");
        int rowCount = rows.count();

        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);

            String stockText = row.locator("td").nth(stockCol).innerText().trim();
            stockText = stockText.replaceAll("[^0-9]", "");
            int stock = Integer.parseInt(stockText);

            if (stock >= minStock) {
                return row.locator("td").nth(nameCol).innerText().trim();
            }
        }
        throw new RuntimeException("No plant found with stock >= " + minStock);
    }

    private int getColumnIndexByHeader(String headerText) {
        Locator headers = page.locator("table thead tr th");
        int count = headers.count();

        for (int i = 0; i < count; i++) {
            String h = headers.nth(i).innerText().trim().toLowerCase();
            if (h.contains(headerText.toLowerCase())) {
                return i;
            }
        }
        throw new RuntimeException("Could not find column with header: " + headerText);
    }

}

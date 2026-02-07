package pages.plants;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.List;
import java.util.ArrayList;

public class PlantsPage73 {
    
    private Page page;
    
    // Locators
    private final Locator nameHeader;
    private final Locator categoryHeader;
    private final Locator priceHeader;
    private final Locator stockHeader;
    private final Locator plantsTable;
    private final Locator tableRows;
    
    public PlantsPage73(Page page) {
        this.page = page;
        
        // Simple selectors - NO XPATH
        this.nameHeader = page.locator("th:has-text('Name')");
        this.categoryHeader = page.locator("th:has-text('Category')");
        this.priceHeader = page.locator("th:has-text('Price')");
        this.stockHeader = page.locator("th:has-text('Stock')");
        
        this.plantsTable = page.locator("table");
        this.tableRows = page.locator("table tbody tr");
    }
    
    // Navigation
    public void navigateToPlantsPage() {
        String fullUrl = "http://localhost:8080/ui/plants";
        System.out.println("Navigating to plants page: " + fullUrl);
        page.navigate(fullUrl);
        page.waitForLoadState();
        page.waitForTimeout(2000);
    }
    
    // Sorting methods
    public void sortByName() {
        System.out.println("Clicking Name header to sort");
        nameHeader.click();
        page.waitForTimeout(1000);
    }
    
    public void sortByPrice() {
        System.out.println("Clicking Price header to sort");
        priceHeader.click();
        page.waitForTimeout(1000);
    }
    
    public void sortByStock() {
        System.out.println("Clicking Stock header to sort");
        stockHeader.click();
        page.waitForTimeout(1000);
    }
    
    // Get column values
    public List<String> getColumnValues(String columnName) {
        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) {
            return new ArrayList<>();
        }
        
        return tableRows.locator("td:nth-child(" + columnIndex + ")")
                       .allInnerTexts();
    }
    
    public List<Integer> getStockValues() {
        List<String> stockTexts = getColumnValues("Stock");
        List<Integer> stocks = new ArrayList<>();
        
        for (String text : stockTexts) {
            try {
                String number = text.replaceAll("[^0-9]", "").trim();
                if (!number.isEmpty()) {
                    stocks.add(Integer.parseInt(number));
                }
            } catch (NumberFormatException e) {
                stocks.add(0);
            }
        }
        return stocks;
    }
    
    public List<Double> getPriceValues() {
        List<String> priceTexts = getColumnValues("Price");
        List<Double> prices = new ArrayList<>();
        
        for (String text : priceTexts) {
            try {
                String number = text.replaceAll("[^0-9.]", "").trim();
                if (!number.isEmpty()) {
                    prices.add(Double.parseDouble(number));
                }
            } catch (NumberFormatException e) {
                prices.add(0.0);
            }
        }
        return prices;
    }
    
    public List<String> getNameValues() {
        return getColumnValues("Name");
    }
    
    public int getPlantCount() {
        return tableRows.count();
    }
    
    // Helper methods
    private int getColumnIndex(String columnName) {
        switch(columnName.toLowerCase()) {
            case "name":
                return 1;
            case "category":
                return 2;
            case "price":
                return 3;
            case "stock":
                return 4;
            default:
                return -1;
        }
    }
    
    // Debug method
    public void printTableContents() {
        System.out.println("=== TABLE CONTENTS ===");
        System.out.println("Number of rows: " + getPlantCount());
        
        List<String> names = getNameValues();
        List<Double> prices = getPriceValues();
        List<Integer> stocks = getStockValues();
        
        for (int i = 0; i < names.size(); i++) {
            System.out.println("Row " + (i+1) + ": " + 
                             names.get(i) + " | " + 
                             prices.get(i) + " | " + 
                             stocks.get(i));
        }
        System.out.println("=====================");
    }
    
    // Simple method to check if page is loaded
    public boolean isPageLoaded() {
        return plantsTable.isVisible();
    }
    
    // ============ TC_USER_UI_PMM_02: LOW STOCK BADGE METHODS ============
    
    public boolean hasLowStockBadge(Locator plantElement) {
        try {
            String[] badgeSelectors = {
                "td .badge:has-text('Low')",
                "td .status-badge:has-text('Low')",
                "td .stock-badge:has-text('Low')",
                "td [class*='badge']:has-text('Low')",
                "td [class*='low']:has-text('Low')",
                "td .alert",
                "td .warning",
                "td .danger",
                "td:has-text('Low')"
            };
            
            for (String selector : badgeSelectors) {
                Locator badge = plantElement.locator(selector);
                if (badge.count() > 0) {
                    boolean isVisible = badge.isVisible();
                    System.out.println("Found badge with selector '" + selector + "': " + isVisible);
                    if (isVisible) {
                        return true;
                    }
                }
            }
            
            String rowText = plantElement.textContent().toLowerCase();
            if (rowText.contains(" low ") || rowText.contains("low stock")) {
                System.out.println("Found 'low' text in row: " + rowText);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            System.out.println("Error checking low stock badge: " + e.getMessage());
            return false;
        }
    }
    
    public String getBadgeText(Locator plantElement) {
        try {
            String[] badgeSelectors = {
                ".badge",
                ".status-badge",
                ".stock-badge",
                "[class*='badge']",
                "[class*='status']",
                ".alert",
                ".warning",
                ".danger",
                ".info"
            };
            
            for (String selector : badgeSelectors) {
                Locator badge = plantElement.locator(selector);
                if (badge.count() > 0 && badge.isVisible()) {
                    String text = badge.textContent().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
            
            return "";
        } catch (Exception e) {
            System.out.println("Error getting badge text: " + e.getMessage());
            return "";
        }
    }
    
    public List<Locator> getAllLowStockBadges() {
        List<Locator> allBadges = new ArrayList<>();
        
        try {
            String[] badgeSelectors = {
                ".badge:has-text('Low')",
                ".status-badge:has-text('Low')",
                ".stock-badge:has-text('Low')",
                "[class*='badge']:has-text('Low')",
                "[class*='low']:has-text('Low')"
            };
            
            for (String selector : badgeSelectors) {
                Locator badges = page.locator(selector);
                if (badges.count() > 0) {
                    allBadges.addAll(badges.all());
                }
            }
            
            System.out.println("Found " + allBadges.size() + " low stock badge(s)");
        } catch (Exception e) {
            System.out.println("Error getting all low stock badges: " + e.getMessage());
        }
        
        return allBadges;
    }
    
    public List<Locator> getAllPlantElements() {
        List<Locator> rows = tableRows.all();
        System.out.println("Found " + rows.size() + " plant rows");
        return rows;
    }
    
    public List<Locator> getAllStockElements() {
        List<Locator> stockElements = new ArrayList<>();
        
        try {
            Locator stockCells = page.locator("table tbody tr td:nth-child(4)");
            if (stockCells.count() > 0) {
                stockElements.addAll(stockCells.all());
            }
            
            System.out.println("Found " + stockElements.size() + " stock elements");
        } catch (Exception e) {
            System.out.println("Error getting stock elements: " + e.getMessage());
        }
        
        return stockElements;
    }
    
    public String getStockElementText(Locator plantElement) {
        try {
            Locator stockElement = plantElement.locator("td:nth-child(4)");
            if (stockElement.count() > 0) {
                String text = stockElement.textContent().trim();
                System.out.println("Stock text for plant: " + text);
                return text;
            }
            
            return null;
        } catch (Exception e) {
            System.out.println("Error getting stock element text: " + e.getMessage());
            return null;
        }
    }
    
    public boolean canSeeAllInformationColumns() {
        try {
            String[] expectedColumns = {"Name", "Category", "Price", "Stock"};
            
            for (String column : expectedColumns) {
                Locator columnHeader = page.locator("th:has-text('" + column + "')").first();
                if (columnHeader.count() == 0 || !columnHeader.isVisible()) {
                    System.out.println("Column '" + column + "' not visible");
                    return false;
                }
            }
            
            System.out.println("All information columns are visible");
            return true;
        } catch (Exception e) {
            System.out.println("Error checking columns visibility: " + e.getMessage());
            return false;
        }
    }
    
    public int getStockQuantityForPlant(String plantName) {
        try {
            List<String> names = getNameValues();
            List<Integer> stocks = getStockValues();
            
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i).equalsIgnoreCase(plantName)) {
                    return stocks.get(i);
                }
            }
            
            System.out.println("Plant '" + plantName + "' not found");
            return -1;
        } catch (Exception e) {
            System.out.println("Error getting stock quantity: " + e.getMessage());
            return -1;
        }
    }
    
    public boolean hasAnyLowStockPlants() {
        List<Integer> stocks = getStockValues();
        
        for (int stock : stocks) {
            if (stock < 5) {
                System.out.println("Found low stock plant with quantity: " + stock);
                return true;
            }
        }
        
        System.out.println("No low stock plants found (all quantities >= 5)");
        return false;
    }
    
    public void printLowStockPlants() {
        List<String> names = getNameValues();
        List<Integer> stocks = getStockValues();
        
        System.out.println("=== LOW STOCK PLANTS ===");
        boolean foundLowStock = false;
        
        for (int i = 0; i < names.size(); i++) {
            if (stocks.get(i) < 5) {
                foundLowStock = true;
                System.out.println("Low Stock: " + names.get(i) + " | Stock: " + stocks.get(i));
            }
        }
        
        if (!foundLowStock) {
            System.out.println("No low stock plants found (all quantities >= 5)");
        }
        System.out.println("======================");
    }
    
    public int countLowStockPlants() {
        List<Integer> stocks = getStockValues();
        int count = 0;
        
        for (int stock : stocks) {
            if (stock < 5) {
                count++;
            }
        }
        
        System.out.println("Low stock plants count: " + count);
        return count;
    }
}
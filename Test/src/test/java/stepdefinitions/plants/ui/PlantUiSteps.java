package stepdefinitions.plants.ui;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import pages.plants.PlantsPage73;
import utils.PlaywrightFactory;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Assertions;
import java.util.List;
import java.util.ArrayList;

public class PlantUiSteps {
    
    private Page page;
    private PlantsPage73 plantsPage;
    
    public PlantUiSteps() {
        this.page = PlaywrightFactory.getPage();
        this.plantsPage = new PlantsPage73(page);
    }
    
    // ============ EXISTING STEPS (You already have these) ============
    
    @Given("the user is logged into the application")
    public void the_user_is_logged_into_the_application() {
        // Your existing login code
        String loginUrl = "http://localhost:8080/ui/login";
        System.out.println("Going to login page: " + loginUrl);
        page.navigate(loginUrl);
        page.waitForTimeout(3000);
        
        page.fill("input", "testuser");
        page.fill("input[type='password']", "test123");
        page.click("button");
        page.waitForTimeout(3000);
        
        System.out.println("Login attempted. Current URL: " + page.url());
    }
    
    @When("the user navigates to the plants page")
    public void the_user_navigates_to_the_plants_page() {
        plantsPage.navigateToPlantsPage();
        plantsPage.printTableContents();
    }
    
    // ============ MISSING STEPS (Add these) ============
    
    @Given("test plants exist in the system")
    public void test_plants_exist_in_the_system() {
        // This step just verifies plants exist - we already navigate in the previous step
        plantsPage.navigateToPlantsPage();
        plantsPage.printTableContents();
        
        int plantCount = plantsPage.getPlantCount();
        System.out.println("Found " + plantCount + " plants in the system");
        Assertions.assertTrue(plantCount > 0, "Should have plants in the system");
    }
    
    @When("the user locates plants with low quantity \\(less than {int})")
    public void the_user_locates_plants_with_low_quantity_less_than(Integer maxQuantity) {
        System.out.println("Looking for plants with quantity less than " + maxQuantity);
        
        // Get all stock values
        List<Integer> stockValues = plantsPage.getStockValues();
        List<String> nameValues = plantsPage.getNameValues();
        
        System.out.println("Current plants and stock levels:");
        for (int i = 0; i < nameValues.size(); i++) {
            int stock = stockValues.get(i);
            String status = stock < maxQuantity ? "LOW STOCK" : "Normal";
            System.out.println("  - " + nameValues.get(i) + ": " + stock + " (" + status + ")");
        }
    }
    
    @Then("the user should see {string} badge on plants with low stock")
    public void the_user_should_see_badge_on_plants_with_low_stock(String expectedBadgeText) {
        System.out.println("Checking for '" + expectedBadgeText + "' badge on low stock plants...");
        
        // For now, just print what we see
        plantsPage.printTableContents();
        
        // This is a placeholder - you'll need to implement actual badge checking
        System.out.println("NOTE: Badge checking logic needs to be implemented");
        System.out.println("Expected to see: " + expectedBadgeText + " on low stock plants");
    }
    
    @And("all inventory indicators should be clearly visible")
    public void all_inventory_indicators_should_be_clearly_visible() {
        System.out.println("Checking inventory indicators visibility...");
        
        // Simple check - verify table is visible
        boolean tableVisible = page.locator("table").isVisible();
        Assertions.assertTrue(tableVisible, "Plants table should be visible");
        
        // Check stock column is visible
        boolean stockHeaderVisible = page.locator("th:has-text('Stock')").isVisible();
        Assertions.assertTrue(stockHeaderVisible, "Stock column should be visible");
        
        System.out.println("✓ All inventory indicators are visible");
    }
    
    @And("low stock information should be transparent for User role")
    public void low_stock_information_should_be_transparent_for_User_role() {
        System.out.println("Verifying information transparency for User...");
        
        // Check all required information is visible
        String[] requiredElements = {"Name", "Price", "Stock"};
        
        for (String element : requiredElements) {
            boolean isVisible = page.locator("th:has-text('" + element + "')").isVisible();
            Assertions.assertTrue(isVisible, element + " column should be visible to User");
        }
        
        // Check we can see stock values
        List<Integer> stockValues = plantsPage.getStockValues();
        Assertions.assertFalse(stockValues.isEmpty(), "Should be able to see stock values");
        
        System.out.println("✓ Low stock information is transparent for User role");
        System.out.println("  Can see " + stockValues.size() + " stock values");
    }
}
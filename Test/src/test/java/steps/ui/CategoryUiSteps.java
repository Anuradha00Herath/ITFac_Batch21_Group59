package steps.ui;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.en.*;
import pages.category.CategoryUiPage;
import support.PlaywrightManager;
import support.Config;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryUiSteps {

    private final Page page = PlaywrightManager.getPage();
    private final CategoryUiPage categoryUiPage = new CategoryUiPage(page);

    @Given("User is logged in as Admin")
    public void userIsLoggedInAsAdmin() {
        // Navigate to login page
        page.navigate(Config.UI_BASE_URL + Config.UI_LOGIN);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        // Get admin credentials from Config
        Config.Creds adminCreds = Config.creds("admin");
        
        // Wait for and fill username field using placeholder text
        page.waitForSelector("input[placeholder='Enter your username']", 
            new Page.WaitForSelectorOptions().setTimeout(10000));
        page.fill("input[placeholder='Enter your username']", adminCreds.username());
        
        // Fill password field
        page.fill("input[placeholder='Enter your password']", adminCreds.password());
        
        // Click Login button
        page.click("button:has-text('Login')");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        // Wait for successful navigation away from login page
        page.waitForURL(url -> !url.contains("/login"), 
            new Page.WaitForURLOptions().setTimeout(10000));
    }

    @Given("User is logged in as regular User")
    public void userIsLoggedInAsRegularUser() {
        // Navigate to login page
        page.navigate(Config.UI_BASE_URL + Config.UI_LOGIN);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        // Get regular user credentials from Config
        Config.Creds userCreds = Config.creds("user");
        
        // Wait for and fill username field
        page.waitForSelector("input[placeholder='Enter your username']", 
            new Page.WaitForSelectorOptions().setTimeout(10000));
        page.fill("input[placeholder='Enter your username']", userCreds.username());
        
        // Fill password field
        page.fill("input[placeholder='Enter your password']", userCreds.password());
        
        // Click Login button
        page.click("button:has-text('Login')");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        // Wait for successful navigation away from login page
        page.waitForURL(url -> !url.contains("/login"), 
            new Page.WaitForURLOptions().setTimeout(10000));
    }

    @When("Navigate to Categories page")
    public void navigateToCategoriesPage() {
        categoryUiPage.openCategoriesPage();
    }

    @When("Click Edit for category {string}")
    public void clickEditForCategory(String categoryName) {
        categoryUiPage.clickEditByCategoryName(categoryName);
    }

    @Then("Edit Category page is displayed for ID {string}")
    public void verifyEditPage(String id) {
        assertTrue(categoryUiPage.isOnEditPage(id));
    }

    @When("Change Category Name to {string}")
    public void changeCategoryName(String name) {
        categoryUiPage.setCategoryName(name);
    }

    @When("Click Save button")
    public void clickSaveButton() {
        categoryUiPage.clickSave();
    }

    @Then("Category name is updated to {string} in the list")
    public void verifyUpdatedName(String name) {
        assertTrue(categoryUiPage.isCategoryVisible(name));
    }

    @Then("Validation error message {string} is displayed")
    public void verifyValidationError(String message) {
        assertTrue(categoryUiPage.isValidationMessageVisible(message));
    }

    @When("Click Cancel button")
    public void clickCancelButton() {
        categoryUiPage.clickCancel();
    }

    @When("Click Delete for category {string}")
    public void clickDeleteCategory(String name) {
        categoryUiPage.clickDeleteByCategoryName(name);
        categoryUiPage.confirmDelete();
    }

    @Then("Category {string} is removed from the list")
    public void verifyCategoryDeleted(String name) {
        assertFalse(categoryUiPage.isCategoryVisible(name));
    }

    @Then("\"Add Category\" button is NOT visible")
    public void verifyAddCategoryButtonNotVisible() {
        // Optional: Debug to see what's on the page
        // categoryUiPage.debugPageElements();
        
        // Main assertion: No Add Category button should be visible
        assertFalse(categoryUiPage.isAddCategoryButtonVisible(), 
            "Regular users should NOT see 'Add Category' button");
        
        // Additional verification: User should still be able to view categories
        assertTrue(categoryUiPage.canViewCategories(),
            "Regular users should be able to view categories page");
        
        // Verify user has no admin privileges
        assertFalse(categoryUiPage.hasAdminPrivileges(),
            "Regular users should not have admin privileges");
    }
}
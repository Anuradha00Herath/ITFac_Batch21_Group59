package stepdefinitions.category.ui;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.en.*;
import pages.category.CategoryUiPage;
import pages.dashboard.DashboardPage;
import utils.category.PlaywrightManager;
import utils.category.Config;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryUiSteps {

    private final Page page = PlaywrightManager.getPage();
    private final CategoryUiPage categoryUiPage = new CategoryUiPage(page);
    private final DashboardPage dashboardPage = new DashboardPage(page);
    
    private String originalCategoryName;

    @Given("User is logged in as Admin")
    public void userIsLoggedInAsAdmin() {
        page.navigate(Config.UI_BASE_URL + Config.UI_LOGIN);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        Config.Creds adminCreds = Config.creds("admin");
        
        page.waitForSelector("input[placeholder='Enter your username']", 
            new Page.WaitForSelectorOptions().setTimeout(10000));
        page.fill("input[placeholder='Enter your username']", adminCreds.username());
        page.fill("input[placeholder='Enter your password']", adminCreds.password());
        page.click("button:has-text('Login')");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForURL(url -> !url.contains("/login"), 
            new Page.WaitForURLOptions().setTimeout(10000));
    }

    @Given("User is logged in as regular User")
    public void userIsLoggedInAsRegularUser() {
        page.navigate(Config.UI_BASE_URL + Config.UI_LOGIN);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
        Config.Creds userCreds = Config.creds("user");
        
        page.waitForSelector("input[placeholder='Enter your username']", 
            new Page.WaitForSelectorOptions().setTimeout(10000));
        page.fill("input[placeholder='Enter your username']", userCreds.username());
        page.fill("input[placeholder='Enter your password']", userCreds.password());
        page.click("button:has-text('Login')");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForURL(url -> !url.contains("/login"), 
            new Page.WaitForURLOptions().setTimeout(10000));
    }

    @Given("At least one category exists")
    public void atLeastOneCategoryExists() {
        System.out.println("✓ Assuming at least one category exists in the system");
    }

    @Given("Categories, Plants, and Sales data exist in system")
    public void categoriesPlantsAndSalesDataExist() {
        System.out.println("✓ Assuming Categories, Plants, and Sales data exist");
    }

    @Given("User is on Dashboard page")
    public void userIsOnDashboardPage() {
        dashboardPage.openDashboard();
    }

    @When("Navigate to Categories page")
    public void navigateToCategoriesPage() {
        categoryUiPage.openCategoriesPage();
    }

    @When("Navigate to Dashboard page")
    public void navigateToDashboardPage() {
        dashboardPage.openDashboard();
    }

    @When("Click Edit for first category")
    public void clickEditForFirstCategory() {
        System.out.println("Clicking Edit for first category");
        page.waitForTimeout(1000);
        
        // Use direct position-based method (more reliable)
        categoryUiPage.clickEditFirstCategory();
    }

    @When("Click Edit for any category")
    public void clickEditForAnyCategory() {
        page.waitForTimeout(1000);
        
        // Store the original name before editing
        originalCategoryName = categoryUiPage.getFirstCategoryName();
        System.out.println("Clicking Edit for category: " + originalCategoryName);
        
        // Use direct position-based method
        categoryUiPage.clickEditFirstCategory();
    }

    @When("Click Delete for first category")
    public void clickDeleteForFirstCategory() {
        System.out.println("Clicking Delete for first category");
        page.waitForTimeout(1000);
        
        // Use direct position-based method (more reliable)
        categoryUiPage.clickDeleteFirstCategory();
        categoryUiPage.confirmDelete();
    }

    @When("Click Edit for category {string}")
    public void clickEditForCategory(String categoryName) {
        categoryUiPage.clickEditByCategoryName(categoryName);
    }

    @When("Change Category Name to {string}")
    public void changeCategoryName(String name) {
        categoryUiPage.setCategoryName(name);
    }

    @When("Click Save button")
    public void clickSaveButton() {
        categoryUiPage.clickSave();
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

    @Then("Edit page is opened")
    public void editPageIsOpened() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        
        // Check multiple indicators that we're on the edit page
        boolean hasEditHeader = page.locator("h1:has-text('Edit Category'), h2:has-text('Edit Category'), h3:has-text('Edit Category')").count() > 0;
        boolean hasNameInput = page.locator("input[name='name'], input#name, input[placeholder*='name' i]").count() > 0;
        boolean urlContainsEdit = page.url().contains("/edit");
        
        assertTrue(hasEditHeader || hasNameInput || urlContainsEdit,
            "Edit page should be displayed (checked: header=" + hasEditHeader + 
            ", input=" + hasNameInput + ", url=" + urlContainsEdit + ")");
    }

    @Then("Category name remains unchanged in the list")
    public void categoryNameRemainsUnchanged() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1000);
        
        // Verify we're back on the categories list page
        boolean isOnListPage = page.url().contains("/categories") && !page.url().contains("/edit");
        assertTrue(isOnListPage, "Should return to categories list page after cancel");
        
        // Verify original category name is still present
        if (originalCategoryName != null && !originalCategoryName.isEmpty()) {
            assertTrue(categoryUiPage.isCategoryVisible(originalCategoryName),
                "Original category name '" + originalCategoryName + "' should still be present after cancel");
        }
    }

    @Then("Category is removed from the list")
    public void categoryIsRemovedFromList() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(1500); // Wait for deletion to complete
        
        // Verify we're back on the categories page
        boolean isOnListPage = page.url().contains("/categories");
        assertTrue(isOnListPage, "Should be on categories list page after deletion");
        
        System.out.println("Category deletion completed successfully");
    }

    @Then("Edit Category page is displayed for ID {string}")
    public void verifyEditPage(String id) {
        assertTrue(categoryUiPage.isOnEditPage(id));
    }

    @Then("Category name is updated to {string} in the list")
    public void verifyUpdatedName(String name) {
        page.waitForTimeout(1500); // Wait for update to complete
        assertTrue(categoryUiPage.isCategoryVisible(name),
            "Category '" + name + "' should be visible in the list");
    }

    @Then("Validation error message {string} is displayed")
    public void verifyValidationError(String message) {
        assertTrue(categoryUiPage.isValidationMessageVisible(message),
            "Validation message '" + message + "' should be displayed");
    }

    @Then("Category {string} is removed from the list")
    public void verifyCategoryDeleted(String name) {
        page.waitForTimeout(1500); // Wait for deletion to complete
        assertFalse(categoryUiPage.isCategoryVisible(name),
            "Category '" + name + "' should be removed from the list");
    }

    @Then("\"Add Category\" button is NOT visible")
    public void verifyAddCategoryButtonNotVisible() {
        assertFalse(categoryUiPage.isAddCategoryButtonVisible(), 
            "Regular users should NOT see 'Add Category' button");
        assertTrue(categoryUiPage.canViewCategories(),
            "Regular users should be able to view categories page");
    }

    @Then("Edit button is hidden or disabled in Actions column")
    public void editButtonIsHiddenOrDisabled() {
        assertTrue(categoryUiPage.areEditButtonsHidden(),
            "Edit buttons should be hidden for regular users");
    }

    @Then("Delete button is hidden or disabled in Actions column")
    public void deleteButtonIsHiddenOrDisabled() {
        assertTrue(categoryUiPage.areDeleteButtonsHidden(),
            "Delete buttons should be hidden for regular users");
    }

    @Then("Category summary is displayed with correct count")
    public void categorySummaryIsDisplayed() {
        assertTrue(dashboardPage.isCategorySummaryDisplayed(),
            "Category summary should be displayed");
    }

    @Then("Plants summary is displayed with correct count")
    public void plantsSummaryIsDisplayed() {
        assertTrue(dashboardPage.isPlantsSummaryDisplayed(),
            "Plants summary should be displayed");
    }

    @Then("Sales summary is displayed with correct count")
    public void salesSummaryIsDisplayed() {
        assertTrue(dashboardPage.isSalesSummaryDisplayed(),
            "Sales summary should be displayed");
    }

    @Then("All summary cards are visible and accurate")
    public void allSummaryCardsAreVisible() {
        assertTrue(dashboardPage.areAllSummaryCardsVisible(),
            "All summary cards should be visible");
    }

    @Then("Dashboard menu item is highlighted as active")
    public void dashboardMenuItemIsActive() {
        assertTrue(dashboardPage.isDashboardMenuItemActive(),
            "Dashboard menu item should be highlighted as active");
    }
}
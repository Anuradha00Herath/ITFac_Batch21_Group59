package stepdefinitions.category.ui;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.en.*;
import pages.category.CategoryUiPage;
import pages.dashboard.DashboardPage;
import utils.category.PlaywrightManager;
import utils.category.Config;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryUiSteps {

    // ================= CONSTANTS =================
    private static final String EMPTY_STRING = "";
    private static final int SHORT_WAIT = 500;
    private static final int MEDIUM_WAIT = 1000;
    private static final int LONG_WAIT = 1500;
    private static final String CATEGORY_PREFIX = "Cat";
    private static final int RANDOM_NUMBER_BOUND = 10000;

    // ================= STATE =================
    private final Page page = PlaywrightManager.getPage();
    private final CategoryUiPage categoryUiPage = new CategoryUiPage(page);
    private final DashboardPage dashboardPage = new DashboardPage(page);
    private final Random random = new Random();
    
    private String originalCategoryName;
    private String generatedCategoryName;

    // ================= AUTHENTICATION =================

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

    // ================= PRECONDITIONS =================

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

    // ================= NAVIGATION =================

    @When("Navigate to Categories page")
    public void navigateToCategoriesPage() {
        categoryUiPage.openCategoriesPage();
    }

    @When("Navigate to Dashboard page")
    public void navigateToDashboardPage() {
        dashboardPage.openDashboard();
    }

    // ================= EDIT OPERATIONS =================

    @When("Click Edit for first category")
    public void clickEditForFirstCategory() {
        System.out.println("Clicking Edit for first category");
        page.waitForTimeout(MEDIUM_WAIT);
        categoryUiPage.clickEditFirstCategory();
    }

    @When("Click Edit for any category")
    public void clickEditForAnyCategory() {
        page.waitForTimeout(MEDIUM_WAIT);
        originalCategoryName = categoryUiPage.getFirstCategoryName();
        System.out.println("Clicking Edit for category: " + originalCategoryName);
        categoryUiPage.clickEditFirstCategory();
    }

    @When("Click Edit for category {string}")
    public void clickEditForCategory(String categoryName) {
        categoryUiPage.clickEditByCategoryName(categoryName);
    }

    // ================= CATEGORY NAME OPERATIONS =================

    @When("Change Category Name")
    public void changeCategoryName() {
        generatedCategoryName = generateRandomCategoryName();
        categoryUiPage.setCategoryName(generatedCategoryName);
        System.out.println("✓ Generated random category name: " + generatedCategoryName);
    }

    @When("Change Category Name to {string}")
    public void changeCategoryNameTo(String name) {
        if (name.isEmpty() || name.equals(EMPTY_STRING)) {
            categoryUiPage.setCategoryName(EMPTY_STRING);
            generatedCategoryName = EMPTY_STRING;
            System.out.println("✓ Setting empty category name for validation test");
        } else {
            categoryUiPage.setCategoryName(name);
            generatedCategoryName = name;
            System.out.println("✓ Setting category name to: " + name);
        }
    }

    // ================= FORM ACTIONS =================

    @When("Click Save button")
    public void clickSaveButton() {
        categoryUiPage.clickSave();
    }

    @When("Click Cancel button")
    public void clickCancelButton() {
        categoryUiPage.clickCancel();
    }

    // ================= DELETE OPERATIONS =================

    @When("Click Delete for first category")
    public void clickDeleteForFirstCategory() {
        System.out.println("Preparing to delete first category");
        page.waitForTimeout(MEDIUM_WAIT);
        
        // Store the category name before deletion
        originalCategoryName = categoryUiPage.getFirstCategoryName();
        System.out.println("Category to be deleted: '" + originalCategoryName + "'");
    }

    @When("Confirm delete in confirmation dialog")
    public void confirmDeleteInConfirmationDialog() {
        System.out.println("Confirming deletion of category: '" + originalCategoryName + "'");
        
        // This method handles both clicking delete AND confirming the dialog
        categoryUiPage.clickDeleteFirstCategoryAndConfirm();
        
        System.out.println("✓ Delete confirmed and completed");
    }

    @When("Cancel delete in confirmation dialog")
    public void cancelDeleteInConfirmationDialog() {
        System.out.println("Cancelling deletion of category: '" + originalCategoryName + "'");
        
        // This method handles both clicking delete AND cancelling the dialog
        categoryUiPage.clickDeleteFirstCategoryAndCancel();
        
        System.out.println("✓ Delete cancelled");
    }

    @When("Click Delete for category {string}")
    public void clickDeleteCategory(String name) {
        System.out.println("Clicking Delete for category: '" + name + "'");
        originalCategoryName = name;
        categoryUiPage.clickDeleteByCategoryName(name);
    }

    // ================= VALIDATIONS - PAGE STATE =================

    @Then("Edit page is opened")
    public void editPageIsOpened() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(MEDIUM_WAIT);
        
        boolean hasEditHeader = page.locator("h1:has-text('Edit Category'), h2:has-text('Edit Category'), h3:has-text('Edit Category')").count() > 0;
        boolean hasNameInput = page.locator("input[name='name'], input#name, input[placeholder*='name' i]").count() > 0;
        boolean urlContainsEdit = page.url().contains("/edit");
        
        assertTrue(hasEditHeader || hasNameInput || urlContainsEdit,
            "Edit page should be displayed (checked: header=" + hasEditHeader + 
            ", input=" + hasNameInput + ", url=" + urlContainsEdit + ")");
    }

    @Then("Edit Category page is displayed for ID {string}")
    public void verifyEditPage(String id) {
        assertTrue(categoryUiPage.isOnEditPage(id));
    }

    // ================= VALIDATIONS - CATEGORY NAME =================

    @Then("Category name remains unchanged in the list")
    public void categoryNameRemainsUnchanged() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(MEDIUM_WAIT);
        
        boolean isOnListPage = page.url().contains("/categories") && !page.url().contains("/edit");
        assertTrue(isOnListPage, "Should return to categories list page after cancel");
        
        if (originalCategoryName != null && !originalCategoryName.isEmpty()) {
            assertTrue(categoryUiPage.isCategoryVisible(originalCategoryName),
                "Original category name '" + originalCategoryName + "' should still be present after cancel");
        }
    }

    @Then("Category name is updated in the list")
    public void verifyUpdatedName() {
        page.waitForTimeout(LONG_WAIT);
        
        assertNotNull(generatedCategoryName, "Generated category name should not be null");
        assertFalse(generatedCategoryName.isEmpty(), "Generated category name should not be empty");
        
        assertTrue(categoryUiPage.isCategoryVisible(generatedCategoryName),
            "Category '" + generatedCategoryName + "' should be visible in the list");
        
        System.out.println("✓ Category name successfully updated to: " + generatedCategoryName);
    }

    @Then("Category name is updated to {string} in the list")
    public void verifyUpdatedNameTo(String name) {
        page.waitForTimeout(LONG_WAIT);
        
        assertTrue(categoryUiPage.isCategoryVisible(name),
            "Category '" + name + "' should be visible in the list");
    }

    // ================= VALIDATIONS - DELETE =================

    @Then("Category is removed from the list")
    public void categoryIsRemovedFromList() {
                page.waitForTimeout(MEDIUM_WAIT);

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(LONG_WAIT);
        
        boolean isOnListPage = page.url().contains("/categories");
        assertTrue(isOnListPage, "Should be on categories list page after deletion");
        
        if (originalCategoryName != null && !originalCategoryName.isEmpty()) {
            assertFalse(categoryUiPage.isCategoryVisible(originalCategoryName),
                "Category '" + originalCategoryName + "' should be removed from the list");
            System.out.println("✓ Category '" + originalCategoryName + "' successfully deleted");
        } else {
            System.out.println("✓ Category deletion completed successfully");
        }
    }

    @Then("Category is NOT removed from the list")
    public void categoryIsNotRemovedFromList() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(LONG_WAIT);        
        boolean isOnListPage = page.url().contains("/categories");
        assertTrue(isOnListPage, "Should still be on categories list page after cancel");
        
        if (originalCategoryName != null && !originalCategoryName.isEmpty()) {
            assertTrue(categoryUiPage.isCategoryVisible(originalCategoryName),
                "Category '" + originalCategoryName + "' should NOT be removed from the list after cancel");
            System.out.println("✓ Category '" + originalCategoryName + "' still exists (deletion was cancelled)");
        }
    }

    @Then("Category {string} is removed from the list")
    public void verifyCategoryDeleted(String name) {
        page.waitForTimeout(LONG_WAIT);
        assertFalse(categoryUiPage.isCategoryVisible(name),
            "Category '" + name + "' should be removed from the list");
    }

    @Then("Category {string} is NOT removed from the list")
    public void verifyCategoryNotDeleted(String name) {
        assertTrue(categoryUiPage.isCategoryVisible(name),
            "Category '" + name + "' should NOT be removed from the list");
    }

    // ================= VALIDATIONS - ERROR MESSAGES =================

    @Then("Validation error message {string} is displayed")
    public void verifyValidationError(String message) {
        assertTrue(categoryUiPage.isValidationMessageVisible(message),
            "Validation message '" + message + "' should be displayed");
    }

    // ================= VALIDATIONS - USER PERMISSIONS =================

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

    // ================= VALIDATIONS - DASHBOARD =================

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

    // ================= UTILITY METHODS =================

    private String generateRandomCategoryName() {
        int randomNumber = random.nextInt(RANDOM_NUMBER_BOUND);
        return CATEGORY_PREFIX + String.format("%04d", randomNumber);
    }
}
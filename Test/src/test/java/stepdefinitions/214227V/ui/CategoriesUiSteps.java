package steps.ui;

import support.Config;
import support.TestContext;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import io.cucumber.java.en.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Random;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.microsoft.playwright.options.SelectOption;

import pages.AuthPage;
import pages.CategoryPage;


public class CategoriesUiSteps {

    AuthPage loginPage;
    CategoryPage categoryPage;
    private final TestContext ctx;

    public CategoriesUiSteps(TestContext ctx) {
        this.ctx = ctx;
        loginPage = new AuthPage(page());
        categoryPage = new CategoryPage(page());
    }

    private static final String UI_LOGIN_PATH = "/ui/login";
    private static final String UI_CATEGORIES_PATH = "/ui/categories";

    private Page page() {
        if (ctx.page == null) throw new IllegalStateException("UI page not initialized. Add @ui tag and Hooks.");
        return ctx.page;
    }

    @Given("I am logged in as {string}")
    public void i_am_logged_in_as(String role) {
        Page page = page();
        page.navigate(Config.BASE_URL + UI_LOGIN_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        String username = "user".equals(role) ? "testuser" : "admin".equals(role) ? "admin" : "";
        String password = "user".equals(role) ? "test123" : "admin".equals(role) ? "admin123" : "";
        
        loginPage.loginWithValidCredentials(username, password);
        loginPage.verifyLogin();
    }

    @When("I open the Categories page")
    public void i_open_the_categories_page() {
        Page page = page();
        page.navigate(Config.BASE_URL + UI_CATEGORIES_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    @Then("Categories page should load")
    public void categories_page_should_load() {
        categoryPage.verifyCategoryTableIsAvailable();
    }

    @Then("I should see the categories table with columns {string}, {string}, {string}")
    public void i_should_see_categories_table_with_columns(String col1, String col2, String col3) {
        categoryPage.verifyCategoryTableColumns(col1, col2, col3);
        
    }

    @Then("I should see a search bar")
    public void i_should_see_search_bar() {
        categoryPage.verifyCategorySearchBarAvailable();
    }

    @Then("I should see the Parents drop-down with default value {string}")
    public void i_should_see_parents_dropdown(String defaultValue) {
        categoryPage.verifyCategoryParentsDropdownAvailable(defaultValue);
    }   

    @Then("I should see a Search button next to drop-down")
    public void i_should_see_search_button() {
        categoryPage.verifyCategorySearchButtonAvailable();
    }

    @When("I input a valid category name in the search bar {string}")
    public void i_input_valid_category_name(String categoryName) {
        categoryPage.searchWithValidCategory(categoryName);
    }

    @When("I click the search button")
    public void i_click_search_button() {
        categoryPage.clickOnSearchButton();
    }

    @Then("I should see search results containing {string}")
    public void i_should_see_search_results(String expectedCategory) {
        categoryPage.verifySearchWithValidCategoryName(expectedCategory);
    }

//filter by parent name

    @When("I select the parent category {string} from the drop-down")
    public void i_select_parent_category_from_dropdown(String parentCategory) {
        categoryPage.selectParentCategory(parentCategory);
    }

    @Then("I should see search results filtered by parent category {string}")
    public void i_should_see_search_results_filtered_by_parent_category(String parentCategory) {
        categoryPage.verifyParentCategoryResults(parentCategory);
    }

    //Sorting table by ID
    @When("I sort the category table by {string}")
    public void i_sort_category_table_by_id_(String column) {
            switch (column.toLowerCase()) {
        case "id":
            categoryPage.sortByIdColumn();
            break;

        case "name":
            categoryPage.sortByNameColumn();
            break;

        case "parent":
            categoryPage.sortByParentColumn();
            break;

        default:
            throw new IllegalArgumentException("Invalid column name: " + column);
    }
    }


    @Then("the table should be sortable in ascending and descending order")
    public void the_table_should_be_sortable_in_both_orders() {

        // Verify ascending
        List<Integer> idsAsc = categoryPage.getCategoryIds();
        assertTrue(categoryPage.isAscending(idsAsc));

        // Click again → descending
        categoryPage.sortByIdColumn();

        List<Integer> idsDesc = categoryPage.getCategoryIds();
        assertTrue(categoryPage.isDescending(idsDesc));
    }


        @Then("the table name column should be in alphabetical order order")
        public void the_table_name_column_should_be_sortable_in_alphabetical_order() {

        // Verify ascending
        List<String> idsAsc = categoryPage.getNameColumnTexts().stream()
                    .map(String::trim)               
                    .collect(Collectors.toList());
        assertTrue(categoryPage.isAscendingText(idsAsc));

        // Click again → descending
        categoryPage.sortByNameColumn();

        List<String> idsDesc = categoryPage.getNameColumnTexts().stream()
                    .map(String::trim)               
                    .collect(Collectors.toList());
        assertTrue(categoryPage.isDescendingText(idsDesc));
    }

        @Then("the table parent column should be in alphabetical order order")
        public void the_table_parent_column_should_be_sortable_in_alphabetical_order() {

        // Verify ascending
        List<String> idsAsc = categoryPage.getParentColumnTexts();
        assertTrue(categoryPage.isAscendingText(idsAsc));

        // Click again → descending
        categoryPage.sortByParentColumn();

        List<String> idsDesc = categoryPage.getParentColumnTexts();
        assertTrue(categoryPage.isDescendingText(idsDesc));
    }

//--------------------------------Invalid Search------------------------------------
// ===== INPUT INVALID CATEGORY =====
    @When("I input a non existing category name in the search bar {string}")
    public void i_input_invalid_category(String categoryName) {
        categoryPage.searchInvalidText(categoryName);         // small wait to let input register
    }

// ===== CLICK SEARCH BUTTON =====
    @When("I click the search button for invalid search")
    public void i_click_search_button_invalid() {
        categoryPage.clickOnSearchButton();              // wait for results or message to appear
    }

// ===== VERIFY "NO CATEGORY FOUND" MESSAGE =====
    @Then("I should see {string} error message")
    public void i_should_see_no_category_message(String expectedMessage) {
        categoryPage.verifyInvalidSearchMessage(expectedMessage);    
    }

//-------------Admin Tests cases--------------------------

// ===== ADD CATEGORY PAGE =====
    @When("I click the Add a category button")
    public void i_click_add_category_button() {
        categoryPage.clickAddCategoryButton();
    }

    @Then("I should be navigated to the Add Category page")
    public void verify_add_category_page_loaded() {
        page().waitForURL(url -> url.contains("/ui/categories/add"));
    }

    @Then("The URL should contain {string}")
    public void verify_url_contains(String expectedUrl) {
        String currentUrl = page().url();
        if (!currentUrl.contains(expectedUrl)) {
            throw new RuntimeException("URL mismatch. Expected: " + expectedUrl + " Found: " + currentUrl);
        }
    }

    @Then("I should see category name label and input field")
    public void verify_category_name_label_and_input() {
        categoryPage.verifyCategoryFieldAvailability();
    }

    @Then("I should see parent category label drop-down")
    public void verify_parent_category_label() {
        categoryPage.verifyParentCategoryFieldAvailability();
    }

    @Then("I should see Save and Cancel buttons")
    public void verify_save_cancel_buttons() {
        categoryPage.verifyButtonsAvailability();
    }

//------Admin creates a main category-------
    @When("I enter a valid category name {string}")
    public void i_enter_valid_category_name(String categoryName) {
        if (categoryName.equals("<RANDOM>")) {
            categoryName = categoryPage.generateAndEnterRandomCategoryName();
        }
    }

    @When("I keep parent category empty")
    public void i_keep_parent_category_empty() {
        categoryPage.verifyParentCategoryFieldAvailability();
    }

    @When("I click the Save button")
    public void i_click_save_button() {
        categoryPage.saveAddedCategory();
    }

    @Then("I should see {string} success message")
    public void i_should_see_success_message(String expectedMessage) {
        categoryPage.verifySuccessMessage(expectedMessage);
    }

    @Then("The created category should appear in the category table")
    public void verify_category_added_to_table() {
        String categoryName = categoryPage.getLastGeneratedCategoryName();
        categoryPage.searchAddedCategory(categoryName);
    }

// ---------- Create Sub Category ----------
    @Given("A parent category {string} added")
    public void parent_category_exists(String categoryName) {
        if (categoryName.equals("<RANDOMPARENT>")) {
            categoryName = categoryPage.generateAndEnterRandomCategoryName();
        }
        categoryPage.saveAddedCategory();
        categoryPage.clickAddCategoryButton();
    }

    // ---------- SELECT PARENT ----------
    @When("I select the exsting parent category")
    public void select_parent_category() {
        String categoryName = categoryPage.getLastGeneratedCategoryName();
        categoryPage.selectParentCategoryWithValue(categoryName);        
    }

    // ---------- ENTER CATEGORY NAME ----------
    @When("I enter a valid sub category name {string}")
    public void enter_category_name(String categoryName) {
        if (categoryName.equals("<RANDOM>")) {
            categoryName = categoryPage.generateAndEnterRandomCategoryName();
        }
    }
    
    // ---------- VERIFY CATEGORY WITH PARENT ----------
//     @Then("The created category {string} should appear under parent")
//     public void verify_category_with_parent(String categoryName) {

//         Locator row = page().locator("table tbody tr")
//         .filter(new Locator.FilterOptions().setHasText(categoryName));

//         assertThat(row).isVisible();

//         String parentText = row.locator("td:nth-child(3)").textContent().trim();

//         if (!parentText.equals(parentName)) {
//             throw new RuntimeException("Parent category mismatch");
//         }
// }

//---------- VERIFY INVALID CATEGORY NAME ----------
    @When("I enter a sub category name with more than 10 characters {string}")
    public void verify_max_characters_for_category_name(String categoryName) {
        categoryPage.enterValidCategoryName(categoryName);       
    }

    @Then("I should see {string} inline error message")
    public void verify_error_message_for_category_name(String errorMessage) {
        categoryPage.checkMaxCharacters(errorMessage);
    }

    //---------- VERIFY CANCEL CHANGES ----------
    @When("I click on the cancel button")
    public void verify_click_on_cancel_button() {
        categoryPage.clickCancelButton();      
    }    

}

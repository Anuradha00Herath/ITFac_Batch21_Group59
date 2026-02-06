package stepdefinitions.sales.ui;

import com.microsoft.playwright.Locator;
import hooks.UiHooks;
import io.cucumber.java.en.*;
import pages.SalesPage;
import pages.LoginPage;
import pages.PlantsPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SalesSteps {
    // Hooks.page provides the active Playwright page instance
    SalesPage salesPage = new SalesPage(UiHooks.page);
    LoginPage loginPage = new LoginPage(UiHooks.page);
    PlantsPage plantsPage = new PlantsPage(UiHooks.page);

    private String chosenPlantName;
    private int beforeStock;
    private int sellQty;


    @Given("the user is logged in as {string}")
    public void userIsLoggedInAs(String role) {
        if (role.equalsIgnoreCase("Admin")) {
            loginPage.login("admin", "admin123");
        } else {
            loginPage.login("testuser", "test123");
        }
    }

    @Given("at least one sale record exists in the database")
    public void recordExists() {
        // Verification or API data seeding logic
        utils.sales.DataHelper.deleteAllSales(UiHooks.page.context().request());
        utils.sales.DataHelper.seedSales(UiHooks.page.context().request(),11);
    }

    @When("the user navigates to the sales list page")
    public void navigateToSales() {
        salesPage.navigate();
    }

    @When("the user clicks the next page on pagination controls")
    public void clickNext() {
        salesPage.clickNextPage();
    }

    @Then("the sales records for the next page should be displayed successfully")
    public void verifySalesLoaded() {
        boolean isRendered = salesPage.waitForContainer();
        assertTrue(isRendered, "Sales table should be visible after pagination");
    }

    @Given("at least 11 sale records exist with different sold dates")
    public void seedSalesWithDifferentDates() {
        // Use your DataHelper to seed records.
        // Note: Your backend should automatically assign different 'soldAt' times.
        utils.sales.DataHelper.seedSales(UiHooks.page.context().request(), 11);
    }
    @Then("the latest sold date should appear at the top of the table")
    public void verifyDateSorting() {
        List<String> dateStrings = salesPage.getSoldDates();

        // Convert Strings to LocalDateTime for comparison
        // Update the pattern to match your UI format (e.g., "yyyy-MM-dd HH:mm")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<LocalDateTime> dates = dateStrings.stream()
                .map(s -> LocalDateTime.parse(s, formatter))
                .collect(Collectors.toList());

        // Verify that each date is after or equal to the next date (Descending)
        for (int i = 0; i < dates.size() - 1; i++) {
            assertTrue(dates.get(i).isAfter(dates.get(i + 1)) || dates.get(i).isEqual(dates.get(i + 1)),
                    "Sorting failed at row " + i + ". Date " + dates.get(i) + " is not after " + dates.get(i + 1));
        }
    }

    @Given("multiple sales exist with different plant names")
    public void seedSalesWithNames() {
        // Use your DataHelper to ensure variety in names
        utils.sales.DataHelper.seedSales(UiHooks.page.context().request(), 5);
    }

    @When("the user clicks the {string} column header")
    public void clickHeader(String headerName) {
        if (headerName.equalsIgnoreCase("Plant Name")) {
            salesPage.sortByName("ascending");
        }
        // Add a small wait for the table rows to re-render
        UiHooks.page.waitForTimeout(1000);
    }
    @When("the user clicks the {string} column header again")
    public void clickHeaderAgain(String headerName) {
        salesPage.sortByName("descending");
    }
    @Then("the records should be sorted by plant name in ascending order")
    public void verifyAscending() {
        List<String> actualNames = salesPage.getPlantNames();
        List<String> expectedNames = new ArrayList<>(actualNames);
        Collections.sort(expectedNames); // Sorts A -> Z

        assertTrue(actualNames.equals(expectedNames),
                "A-Z sorting failed! Expected: " + expectedNames + " but got: " + actualNames);
    }
    @Then("the records should be sorted by plant name in descending order")
    public void verifyDescending() {
        List<String> actualNames = salesPage.getPlantNames();
        List<String> expectedNames = new ArrayList<>(actualNames);
        expectedNames.sort(Collections.reverseOrder()); // Sorts Z -> A

        assertTrue(actualNames.equals(expectedNames),
                "Z-A sorting failed! Expected: " + expectedNames + " but got: " + actualNames);
    }

    @Given("the admin has cleared all sales records via API")
    public void clearSales() {
        utils.sales.DataHelper.deleteAllSales(UiHooks.page.context().request());
    }
    @Then("a message {string} should be displayed")
    public void verifyEmptyMessage(String expectedMessage) {
        String actual = salesPage.getEmptyMessageText();
        assertTrue(actual.contains(expectedMessage), "Expected empty message not found!");
    }
    @Then("the sales table should not be visible")
    public void verifyTableHidden() {
        // We use isHidden() to ensure the table isn't just empty, but gone from the view
        assertTrue(UiHooks.page.locator("table.sales-list").isHidden());
    }
    @Then("the {string} button should be visible")
    public void verifyButtonVisible(String buttonName) {
        assertTrue(salesPage.isAddButtonVisible(),
                "The '" + buttonName + "' button should be visible for this role.");
    }
    @Then("the {string} button should not be visible")
    public void verifyButtonNotVisible(String buttonName) {
        // isHidden() is the direct opposite of isVisible()
        assertTrue(UiHooks.page.locator("text=" + buttonName).isHidden(),
                "Security Breach: The '" + buttonName + "' button is visible to a non-admin!");
    }
    @And("a plant with ID 1 exists in the inventory")
    public void ensurePlantExists() {
        // Pro-Tip: Use your API logic to seed a plant if it's missing
        // This prevents foreign key constraint errors in your DB
    }
    @And("the user clicks the {string} button")
    public void clickAddBtn(String btnName) {
        salesPage.clickAddSale();
    }
    @Then("the system should redirect to the {string} form page")
    public void verifyUrlChange(String pageName) {
        // Verify that the URL now ends with the creation path
        // e.g., http://localhost:8080/ui/sales/add
        assertTrue(UiHooks.page.url().contains("/sales/new"),
                "Navigation failed! Current URL is: " + UiHooks.page.url());
    }

    @And("the form title should be {string}")
    public void verifyFormTitle(String expectedTitle) {
        String actualTitle = salesPage.getPageTitle();
        // Use contains() to be safe with extra spaces or icons in the header
        assertTrue(actualTitle.contains(expectedTitle),
                "Expected title '" + expectedTitle + "' but found '" + actualTitle + "'");
    }
    @Then("the new sale should be visible at the top of the sales list")
    public void verifyNewSale() {
        // Since your default sort is 'Date Desc', the newest sale is always the 1st row
        String firstRowPlant = UiHooks.page.locator("table.sales-list tbody tr td:nth-child(2)").first().innerText();
        // You can also check if the quantity matches
        String firstRowQty = UiHooks.page.locator("table.sales-list tbody tr td:nth-child(3)").first().innerText();

        assertTrue(firstRowQty.contains("5"), "The newly created sale quantity was not found at the top!");
    }

    @When("the user navigates directly to the Sell Plant page")
    public void navigateDirectlyToSellPlantPage() {
        salesPage.navigateToSellPlantForm();
    }

    @When("the user opens the plant dropdown")
    public void openPlantDropdown() {
        salesPage.openPlantDropdown();
    }

    @Then("the plant dropdown should show available plants")
    public void dropdownShouldShowAvailablePlants() {
        List<String> options = salesPage.getPlantDropdownOptions();
        // remove placeholder options like "Select..."
        List<String> plants = options.stream()
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .filter(t -> !t.toLowerCase().contains("select"))
                .collect(Collectors.toList());

        assertTrue(plants.size() > 0, "Expected at least one plant in dropdown");
    }

    @Then("out-of-stock plants should be hidden or disabled in the dropdown")
    public void outOfStockHiddenOrDisabled() {
        // Pass if: (A) no out-of-stock options appear
        // OR (B) if they appear, they must be disabled or clearly marked
        boolean ok = salesPage.outOfStockHiddenOrDisabled();
        assertTrue(ok, "Out-of-stock plants must be hidden OR disabled/marked as unavailable");
    }

    @When("the user selects a plant from the dropdown")
    public void selectAPlantFromDropdown() {
        salesPage.selectAnyPlant();
    }

    @When("the user leaves quantity empty")
    public void leaveQuantityEmpty() {
        salesPage.setQuantityToZero();
    }

    @When("the user clicks Sell")
    public void clickSellSubmit() {
        salesPage.clickSubmit();
    }

    @Then("a quantity required validation message should be shown")
    public void quantityRequiredValidationShown() {
        String msg = salesPage.getQuantityValidationMessage();
        assertTrue(salesPage.isQuantityValidationShown(),
                "Expected quantity validation, but validationMessage was: " + msg);
    }

    @Then("the sale should not be created")
    public void saleNotCreated() {
        String url = salesPage.currentUrl();
        assertTrue(url.contains("/ui/sales/new"),
                "Expected to remain on /ui/sales/new when validation fails, but was: " + url);
    }
    @Given("an available plant with stock at least {int} is chosen")
    public void choosePlantWithMinStock(int minStock) {
        chosenPlantName = plantsPage.findAnyPlantNameWithStockAtLeast(minStock);
        beforeStock = plantsPage.getStockForPlant(chosenPlantName);
    }

    @When("the user selects the chosen plant in the dropdown")
    public void selectChosenPlantInDropdown() {
        salesPage.selectPlantByName(chosenPlantName);
    }

    @When("the user enters sell quantity {int}")
    public void enterSellQuantity(int qty) {
        sellQty = qty;
        salesPage.fillQuantity(qty);
    }

    @Then("the user should be redirected to the sales list page")
    public void redirectedToSalesList() {
        salesPage.waitForRedirectToSalesList();
        assertTrue(UiHooks.page.url().contains("/ui/sales"),
                "Expected redirect to /ui/sales but was " + UiHooks.page.url());
    }

    @Then("the chosen plant stock should be reduced by {int}")
    public void verifyStockReducedBy(int reducedBy) {
        // Go back to plants list and re-check
        plantsPage.openPlantsList();
        int afterStock = plantsPage.getStockForPlant(chosenPlantName);

        assertEquals(beforeStock - reducedBy, afterStock,
                "Stock did not reduce correctly for plant " + chosenPlantName);
    }
    @When("the user clicks Cancel on the Sell Plant form")
    public void clickCancelOnSellPlantForm() {
        salesPage.clickCancel();
    }
    @Then("no sale should be submitted on cancel")
    public void noSaleSubmittedOnCancel() {
        // On cancel, you MUST be on /ui/sales, so don't check /ui/sales/new
        String url = UiHooks.page.url();
        org.junit.jupiter.api.Assertions.assertTrue(url.contains("/ui/sales"),
                "Expected to be on /ui/sales after cancel, but was: " + url);

        // Optional: ensure we didn't see a success message
        Locator success = UiHooks.page.locator("text=/sold successfully|sale created|success/i");
        if (success.count() > 0) {
            org.junit.jupiter.api.Assertions.assertFalse(success.first().isVisible(),
                    "Unexpected success message after cancel.");
        }
    }

}
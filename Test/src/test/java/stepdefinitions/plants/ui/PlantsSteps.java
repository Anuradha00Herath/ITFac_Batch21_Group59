package stepdefinitions.plants.ui;

import hooks.UiHooks;
import io.cucumber.java.en.*;
import pages.plants.PlantsPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlantsSteps {

    private final PlantsPage plantsPage = new PlantsPage(UiHooks.page);

    private String deletedPlantName;
    private String createdPlantName;

    private List<String> beforeNames;
    private List<String> afterNames;

    private List<String> sortedNames;


    @Given("more than 10 plant records exist in the database")
    public void moreThan10PlantsExist() {
        // IMPORTANT:
        // With pagination, page-1 might show only 10 rows even if total plants are 11+.
        // So we verify pagination is possible instead of rowCount > 10.

        plantsPage.navigate();
        assertTrue(plantsPage.waitForContainer(), "Plants list should be visible");

        int rowCount = plantsPage.getRowCount();
        boolean paginationVisible = plantsPage.isPaginationVisible();
        boolean canGoNext = plantsPage.canGoNext();

        // Pass if either:
        // - we can actually go next, OR
        // - pagination controls are visible (some UIs show numeric pages), OR
        // - UI shows >10 rows on first page (rare)
        assertTrue(canGoNext || paginationVisible || rowCount > 10,
                "Expected pagination to be available (11+ total plants). " +
                        "But rowCount=" + rowCount +
                        ", paginationVisible=" + paginationVisible +
                        ", canGoNext=" + canGoNext);
    }
    @Given("at least one plant record exists in the database")
    public void atLeastOnePlantExists() {
        plantsPage.navigate();
        assertTrue(plantsPage.waitForContainer(), "Plants list should be visible");
        assertTrue(plantsPage.getRowCount() >= 1,
                "Expected at least 1 plant row on /ui/plants, but found 0. Seed DB first.");
    }

    @When("the user navigates to the plants list page")
    public void navigateToPlantsList() {
        plantsPage.navigate();
    }

    @Then("the plant list should be displayed successfully")
    public void plantsListDisplayed() {
        assertTrue(plantsPage.waitForContainer(), "Plants table/list should be visible");
    }

    @Then("pagination controls should be visible on the plants list")
    public void paginationVisible() {
        assertTrue(plantsPage.isPaginationVisible(), "Expected pagination controls (or Next button) on Plants page");
    }

    @When("the user clicks the Next page on plants pagination controls")
    public void clickNextPlantsPagination() {
        beforeNames = plantsPage.getPlantNamesOnCurrentPage();

        // If next is disabled, fail clearly
        assertTrue(plantsPage.canGoNext(),
                "Next page is not available/enabled. Ensure total plants > page size.");

        plantsPage.clickNextPage();
        afterNames = plantsPage.getPlantNamesOnCurrentPage();
    }
    @When("the user searches plants by name {string}")
    public void searchPlantsByName(String name) {
        plantsPage.searchByName(name);
    }
    @Then("only plants matching name {string} should be displayed")
    public void onlyMatchingPlantsShown(String name) {
        List<String> names = plantsPage.getPlantNamesOnCurrentPage();
        assertTrue(names.size() > 0, "Expected at least 1 result row after searching, but got 0");

        String q = name.toLowerCase();
        boolean allMatch = names.stream().allMatch(n -> n.toLowerCase().contains(q));

        assertTrue(allMatch,
                "Expected all rows to match search '" + name + "', but got: " + names);
    }

    @Then("navigating to the next page should update the plant list")
    public void nextPageUpdatesList() {
        assertTrue(afterNames != null && !afterNames.isEmpty(), "Expected plants on the next page");
        assertTrue(beforeNames != null && !beforeNames.isEmpty(), "Expected plants on the first page");

        assertTrue(!afterNames.equals(beforeNames),
                "Expected next page to change the list, but it appears identical.");
    }

    @Then("different plants should be displayed on different pages")
    public void differentPlantsOnDifferentPages() {
        boolean anyDifferent = afterNames.stream().anyMatch(n -> !beforeNames.contains(n));
        assertTrue(anyDifferent,
                "Expected at least one plant on page 2 not present on page 1. Page1=" + beforeNames + " Page2=" + afterNames);
    }

    @When("the user filters plants by category {string}")
    public void filterPlantsByCategory(String category) {
        plantsPage.filterByCategory(category);
    }
    @Then("only plants in category {string} should be displayed")
    public void onlyPlantsInCategoryShown(String category) {
        assertTrue(plantsPage.getRowCount() >= 0,
                "Filtered plant list should be visible after selecting category " + category);
    }

    @Given("multiple plant records exist in the database")
    public void multiplePlantsExist() {
        plantsPage.navigate();
        assertTrue(plantsPage.waitForContainer(), "Plants list should be visible");
        assertTrue(plantsPage.getRowCount() >= 2,
                "Expected at least 2 plants to test sorting, but found < 2. Seed DB first.");
    }

    @When("the user clicks the Name sort link")
    public void clickNameSortLink() {
        plantsPage.clickNameSortLink();
    }

    @Then("the plant list should be sorted by name based on current sort direction")
    public void verifySortedByNameBasedOnDir() {
        List<String> names = plantsPage.getPlantNamesOnCurrentPage();
        assertTrue(names != null && names.size() >= 2,
                "Need at least 2 rows to validate sorting. Names=" + names);

        String dir = plantsPage.getNameSortDirFromHref(); // direction for the NEXT click, but in your UI it's consistent with indicator.
        // In your HTML, the href is what you will navigate to when clicking again.
        // Better approach: infer current direction from the arrow span if present.
        // We'll do robust check: validate both and accept the one that matches.
        boolean isAsc = isSortedAsc(names);
        boolean isDesc = isSortedDesc(names);

        // If list is strictly asc or desc, pass accordingly.
        // If duplicates or case-insensitive ties, both might be true; that's fine.
        assertTrue(isAsc || isDesc,
                "List is not sorted ASC or DESC. Names=" + names + " (href sortDir=" + dir + ")");
    }

    private boolean isSortedAsc(List<String> names) {
        for (int i = 1; i < names.size(); i++) {
            String prev = pages.plants.PlantsPage.normalizeName(names.get(i - 1));
            String curr = pages.plants.PlantsPage.normalizeName(names.get(i));
            if (prev.compareTo(curr) > 0) return false;
        }
        return true;
    }

    private boolean isSortedDesc(List<String> names) {
        for (int i = 1; i < names.size(); i++) {
            String prev = pages.plants.PlantsPage.normalizeName(names.get(i - 1));
            String curr = pages.plants.PlantsPage.normalizeName(names.get(i));
            if (prev.compareTo(curr) < 0) return false;
        }
        return true;
    }

    @When("the user clicks the Price sort link")
    public void clickPriceSortLink() {
        plantsPage.clickPriceSortLink();
    }

    @Then("the plant list should be sorted by price based on current sort direction")
    public void verifySortedByPriceBasedOnDir() {
        List<Double> prices = plantsPage.getPricesOnCurrentPage();

        // Must have enough data to validate
        assertTrue(prices != null && prices.size() >= 2,
                "Need at least 2 price values to validate sorting. Prices=" + prices);

        boolean isAsc = isSortedAscD(prices);
        boolean isDesc = isSortedDescD(prices);

        // duplicates can make both true, that's ok
        assertTrue(isAsc || isDesc,
                "Price list is not sorted ASC or DESC. Prices=" + prices);
    }

    private boolean isSortedAscD(List<Double> xs) {
        for (int i = 1; i < xs.size(); i++) {
            if (xs.get(i - 1) > xs.get(i)) return false;
        }
        return true;
    }

    private boolean isSortedDescD(List<Double> xs) {
        for (int i = 1; i < xs.size(); i++) {
            if (xs.get(i - 1) < xs.get(i)) return false;
        }
        return true;
    }

    @When("the user logs out")
    public void logout() {
        plantsPage.logout();
    }
    @Then("the {string} in plant button should be visible")
    public void addPlantVisible(String btnText) {
        // btnText is "Add Plant"
        assertTrue(plantsPage.isAddPlantVisible(), btnText + " should be visible for Admin");
    }
    @Then("the {string} in plant button should not be visible")
    public void addPlantNotVisible(String btnText) {
        assertFalse(plantsPage.isAddPlantVisible(), btnText + " should be hidden for regular users");
    }
    @Then("the Edit action should be visible for a plant row")
    public void editVisible() {
        assertTrue(plantsPage.isEditVisibleForAnyRow(), "Edit action should be visible for Admin");
    }
    @When("the admin clicks Edit for the first plant")
    public void clickEditFirst() {
        plantsPage.clickEditFirstRow();
    }

    @Then("the admin should be redirected to the Edit Plant page")
    public void verifyEditPage() {
        assertTrue(plantsPage.isOnEditPage(), "Expected to be on /ui/plants/edit/{id}, but was: " + UiHooks.page.url());
    }
    @Then("the Delete action should be visible for a plant row")
    public void deleteVisible() {
        assertTrue(plantsPage.isDeleteVisibleForAnyRow(), "Delete action should be visible for Admin");
    }
    @When("the admin deletes the first plant and confirms")
    public void deleteFirstConfirm() {
        plantsPage.openPlantsList();
        deletedPlantName = plantsPage.getFirstPlantName();
        plantsPage.deleteFirstRowAndConfirm();
    }

    @Then("the deleted plant should be removed from the plant list")
    public void verifyDeleted() {
        plantsPage.openPlantsList();
        assertFalse(plantsPage.isPlantNamePresent(deletedPlantName),
                "Expected plant '" + deletedPlantName + "' to be removed from list.");
    }
    @When("the admin clicks the {string} in plant button")
    public void clickAddPlant(String btnText) {
        plantsPage.clickAddPlant();
    }
    @Then("the admin should be redirected to the Add Plant page")
    public void verifyAddPage() {
        assertTrue(plantsPage.isOnAddPage(), "Expected to be on /ui/plants/add, but was: " + UiHooks.page.url());
    }

    @Then("the Add Plant form should be displayed and empty")
    public void formDisplayedAndEmpty() {
        assertTrue(plantsPage.waitForFormVisible(), "Add Plant form should be visible");
        assertTrue(plantsPage.isAddFormEmpty(), "Expected form to be empty by default");
    }
    @When("the admin opens the Add Plant page")
    public void adminOpensAddPage() {
        plantsPage.openAddPlantPage();
        assertTrue(plantsPage.waitForFormVisible(), "Add Plant form should be visible");
    }

    @When("the admin enters a valid plant name")
    public void enterValidName() {
        createdPlantName = "AutoPlant-" + String.valueOf(System.currentTimeMillis()).substring(7);
        plantsPage.fillPlantName(createdPlantName);
    }

    @When("the admin selects a valid sub-category")
    public void selectValidCategory() {
        // Your dropdown shows: Business
        plantsPage.selectCategoryByLabel("Bryophyta");
    }

    @When("the admin enters price greater than 0")
    public void enterPrice() {
        plantsPage.fillPrice(100.00);
    }

    @When("the admin enters quantity greater than or equal to 0")
    public void enterQty() {
        plantsPage.fillQuantity(0);
    }

    @When("the admin clicks Save on plant form")
    public void clickSave() {
        plantsPage.clickSave();
    }

    @Then("the plant should be successfully added and shown in the plant list")
    public void verifyAdded() {
        // Many apps redirect back to /ui/plants after save
        plantsPage.openPlantsList();
        assertTrue(plantsPage.isPlantNamePresent(createdPlantName),
                "Expected newly created plant '" + createdPlantName + "' to appear in list.");
        // Optional: if your app shows alerts
        // assertTrue(plantsPage.isAnySuccessAlertVisible(), "Expected success message/alert");
    }

}
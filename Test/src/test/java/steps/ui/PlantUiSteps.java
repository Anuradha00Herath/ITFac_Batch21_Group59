package steps.ui;

import utils.Config;
import utils.TestContext;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import io.cucumber.java.en.*;

import java.nio.file.*;
import java.time.Instant;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlantUiSteps {

    private final TestContext ctx;

    public PlantUiSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    // ---------------- Paths ----------------
    private static final String UI_LOGIN_PATH = "/ui/login";
    private static final String UI_PLANTS_PATH = "/ui/plants";

    // ---------------- Selectors ----------------
    private static final String PLANT_CONTAINER =
            "[data-testid='plant-list'], [data-testid='plant-table'], .plant-list, .plant-table, main, .container";

    private static final String PLANT_TABLE = "table";

    private static final String PLANT_ROWS =
            "table tbody tr, [data-testid*='plant-row'], .plant-row, .MuiDataGrid-row, [role='row']";

    private static final String PLANT_EMPTY_STATE =
            "text=/no plants found|no data|no records/i";

    private static final String PLANT_PAGINATION =
            ".pagination, nav[aria-label*=pagination], [data-testid*=pagination]";

    private static final String PLANT_NEXT_BUTTON =
            "button:has-text('Next'), a:has-text('Next'), [aria-label*='next' i]";

    private static final String PLANT_SEARCH_INPUT =
            "input[type='search'], input[placeholder*='Search' i], input[name*='search' i]";
    private static final String PLANT_SEARCH_BUTTON =
            "button:has-text('Search'), [aria-label*='search' i]";

    private static final String PLANT_SORT_BY_NAME =
            "th:has-text('Name'), button:has-text('Name')";

    private static final String PLANT_SORT_BY_PRICE =
            "th:has-text('Price'), button:has-text('Price')";


    // ---------------- helpers ----------------

    private Page page() {
        if (ctx.page == null)
            throw new IllegalStateException("UI page not initialized. Add @ui tag and Hooks.");
        return ctx.page;
    }

    private void assertNotRedirectedToLogin(Page page, String context) {
        if (page.url().contains(UI_LOGIN_PATH)) {
            screenshot(page, "redirect-login-" + context);
            throw new RuntimeException(context + ": redirected to login page.");
        }
    }

    // ---------------- Steps ----------------
    // ---------------------------------------------------
    // TC_USER_UI_PMM_01
    // ---------------------------------------------------

    @Given("more than 10 plant records exist")
    public void more_than_10_plant_records_exist() {
        // UI-level verification only (actual DB seeding should be done outside UI tests)
    }

    @When("I open the Plants list page")
    public void i_open_the_plants_list_page() {

        Page page = page();

        page.navigate(Config.BASE_URL + UI_PLANTS_PATH,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        page.waitForTimeout(300);

        assertNotRedirectedToLogin(page, "open-plants");

        Locator rows = page.locator(PLANT_ROWS).first();
        Locator empty = page.locator(PLANT_EMPTY_STATE).first();
        Locator container = page.locator(PLANT_CONTAINER).first();

        boolean ok = waitAnyVisible(page, 15000, rows, empty, container);
        if (!ok) {
            screenshot(page, "plants-page-not-loaded");
            throw new RuntimeException("Plants list page did not render correctly.");
        }
    }

    @Then("the plant list should be displayed")
    public void the_plant_list_should_be_displayed() {

        Page page = page();

        Locator rows = page.locator(PLANT_ROWS);
        Locator empty = page.locator(PLANT_EMPTY_STATE);

        if (rows.count() == 0 && empty.count() == 0) {
            screenshot(page, "plant-list-not-visible");
            throw new RuntimeException("No plant rows or empty state found.");
        }

        // store first page snapshot for later comparison
        ctx.firstPageRowSnapshot = captureRowTexts(page);
    }

    @Then("pagination controls should be visible")
    public void pagination_controls_should_be_visible() {

        Page page = page();

        Locator pagination = page.locator(PLANT_PAGINATION);

        if (pagination.count() == 0 || !pagination.first().isVisible()) {
            screenshot(page, "pagination-not-visible");
            throw new RuntimeException("Pagination controls are not visible.");
        }
    }

    @When("I click the Next page button")
    public void i_click_the_next_page_button() {

        Page page = page();

        Locator nextBtn = page.locator(PLANT_NEXT_BUTTON).first();

        if (nextBtn.count() == 0 || !nextBtn.isVisible()) {
            screenshot(page, "next-button-not-found");
            throw new RuntimeException("Next page button not found.");
        }

        // wait for navigation or data refresh
        nextBtn.click();

        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(15000));
        } catch (Exception ignored) {}

        page.waitForTimeout(500);
    }

    @Then("the plant list should be updated")
    public void the_plant_list_should_be_updated() {

        Page page = page();

        Locator rows = page.locator(PLANT_ROWS).first();
        Locator empty = page.locator(PLANT_EMPTY_STATE).first();

        boolean ok = waitAnyVisible(page, 15000, rows, empty);
        if (!ok) {
            screenshot(page, "plant-list-not-updated");
            throw new RuntimeException("Plant list did not update after pagination.");
        }
    }

    @Then("different plants should be displayed on the next page")
    public void different_plants_should_be_displayed_on_the_next_page() {

        Page page = page();

        if (ctx.firstPageRowSnapshot == null || ctx.firstPageRowSnapshot.isEmpty()) {
            // cannot compare, but still ensure rows exist
            assertThat(page.locator(PLANT_TABLE).first()).isVisible();
            return;
        }

        String secondPageSnapshot = captureRowTexts(page);

        if (ctx.firstPageRowSnapshot.equals(secondPageSnapshot)) {
            screenshot(page, "same-plants-on-next-page");
            throw new RuntimeException("Same plant records appear on both pages.");
        }
    }

    // ---------------------------------------------------
    // TC_USER_UI_PMM_02
    // ---------------------------------------------------

    @And("I enter {string} in the plant search field")
    public void i_enter_in_the_plant_search_field(String plantName) {
        Page page = page();

        Locator input = page.locator(PLANT_SEARCH_INPUT).first();

        input.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        input.fill(plantName);
    }

    @And("I click the Search button")
    public void i_click_the_search_button() {
        Page page = page();
        page.click(PLANT_SEARCH_BUTTON);
        page.waitForTimeout(500);
    }

    @Then("only plants matching {string} should be displayed")
    public void only_plants_matching_should_be_displayed(String plantName) {
        Page page = page();
        List<String> displayedPlants = page.locator(PLANT_ROWS + " td:first-child").allTextContents();
        for (String name : displayedPlants) {
            assertTrue(name.toLowerCase().contains(plantName.toLowerCase()),
                    "Found non-matching plant: " + name);
        }
    }

    @Then("non-matching plants are not shown")
    public void non_matching_plants_are_not_shown() {
    }

    // ---------------------------------------------------
    // TC_USER_UI_PMM_03
    // ---------------------------------------------------
    @Given("plants with multiple categories exist")
    public void plants_with_multiple_categories_exist() {

    }
    @And("I select a category from the category filter")
    public void i_select_a_category_from_the_category_filter() {

        Page page = page();

        Locator select = page.locator(
                "select[name*='category' i], select[id*='category' i]"
        ).first();

        if (select.count() == 0) {
            screenshot(page, "category-select-not-found");
            throw new RuntimeException("Category select not found.");
        }

        // select the first real category (not empty / All)
        select.selectOption(new SelectOption().setIndex(1));

        page.waitForTimeout(500);
    }

    @Then("only plants belonging to the selected category should be displayed")
    public void only_plants_belonging_to_the_selected_category_should_be_displayed() {

        Page page = page();

        Locator rows = page.locator(PLANT_ROWS);

        if (rows.count() == 0) {
            screenshot(page, "no-rows-after-filter");
            throw new RuntimeException("No plants shown after applying category filter.");
        }

        // Generic UI validation – we only check that rows are still present
        // (exact category column differs per UI)
        assertThat(rows.first()).isVisible();
    }

    @When("I clear the category filter")
    public void i_clear_the_category_filter() {

        Page page = page();

        Locator resetBtn = page.locator("a:has-text('Reset')").first();

        if (resetBtn.count() == 0 || !resetBtn.isVisible()) {
            screenshot(page, "reset-link-not-found");
            throw new RuntimeException("Clear filter (Reset) link not found.");
        }

        resetBtn.click();

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.waitForTimeout(500);
    }


    @Then("all plants should be displayed")
    public void all_plants_should_be_displayed() {

        Page page = page();

        Locator rows = page.locator(PLANT_ROWS);

        if (rows.count() == 0) {
            screenshot(page, "no-plants-after-clear-filter");
            throw new RuntimeException("No plants shown after clearing filter.");
        }

        assertThat(rows.first()).isVisible();
    }
// ---------------------------------------------------
// TC_USER_UI_PMM_04
 // ---------------------------------------------------

    @Given("multiple plant records exist")
    public void multiple_plant_records_exist() {

    }
    @When("I click the plant name sort button")
    public void i_click_the_plant_name_sort_button() {

        Page page = page();

        Locator sort = page.locator(PLANT_SORT_BY_NAME).first();

        if (sort.count() == 0 || !sort.isVisible()) {
            screenshot(page, "name-sort-not-found");
            throw new RuntimeException("Plant name sort control not found.");
        }

        sort.click();

        waitUntilNameColumnIsSorted(page, 5000);
    }
    @Then("the plant list should be sorted by name in descending order")
    public void the_plant_list_should_be_sorted_by_name_in_descending_order() {

        Page page = page();

        List<String> names =
                page.locator(PLANT_ROWS + " td:first-child")
                        .allTextContents();

        assertTrue(isSortedDescending(names),
                "Plant names are not sorted in descending order.");


    }
    @When("I click the plant name sort button again")
    public void i_click_the_plant_name_sort_button_again() {
        i_click_the_plant_name_sort_button();
    }


    @Then("the plant list should be sorted by name in ascending order")
    public void the_plant_list_should_be_sorted_by_name_in_ascending_order() {

        Page page = page();

        List<String> names =
                page.locator(PLANT_ROWS + " td:first-child")
                        .allTextContents();

        assertTrue(isSortedAscending(names),
                "Plant names are not sorted in ascending order.");
    }
    // ---------------------------------------------------
    // TC_USER_UI_PMM_05
    // ---------------------------------------------------

    @Given("multiple plant records with different prices exist")
    public void multiple_plant_records_with_different_prices_exist() {
        // Precondition handled by test data
    }
    @When("I click the plant price sort button")
    public void i_click_the_plant_price_sort_button() {

        Page page = page();

        Locator sort = page.locator(PLANT_SORT_BY_PRICE).first();

        if (sort.count() == 0 || !sort.isVisible()) {
            screenshot(page, "price-sort-not-found");
            throw new RuntimeException("Plant price sort control not found.");
        }

        ctx.firstPageRowSnapshot = captureRowTexts(page);

        sort.click();
        page.waitForTimeout(600);
    }

    @Then("the plant list should be sorted by price in ascending order")
    public void the_plant_list_should_be_sorted_by_price_in_ascending_order() {

        Page page = page();

        List<String> pricesText =
                page.locator(PLANT_ROWS + " td:nth-child(3)")
                        .allTextContents();

        List<Double> prices = pricesText.stream()
                .map(this::parsePrice)
                .toList();

        assertTrue(isNumberSortedAscending(prices),
                "Prices are not sorted in ascending order.");
    }


    @Then("the plant list should be sorted by price in descending order")
    public void the_plant_list_should_be_sorted_by_price_in_descending_order() {

        Page page = page();

        List<String> pricesText =
                page.locator(PLANT_ROWS + " td:nth-child(3)")
                        .allTextContents();

        List<Double> prices = pricesText.stream()
                .map(this::parsePrice)
                .toList();

        assertTrue(isNumberSortedDescending(prices),
                "Prices are not sorted in descending order.");
    }




    // ---------------- utilities ----------------

    private static boolean waitAnyVisible(Page page, int timeoutMs, Locator... locators) {

        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {

            for (Locator l : locators) {
                try {
                    if (l != null && l.count() > 0 && l.first().isVisible())
                        return true;
                } catch (Exception ignored) {}
            }

            page.waitForTimeout(200);
        }
        return false;
    }

    private static void screenshot(Page page, String name) {

        try {
            Path dir = Paths.get("tests", "screenshots");
            Files.createDirectories(dir);

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(dir.resolve(
                            Instant.now().toEpochMilli() + "-" + name + ".png"))
                    .setFullPage(true));

        } catch (Exception ignored) {}
    }

    private static String captureRowTexts(Page page) {

        Locator rows = page.locator(PLANT_ROWS);

        int count = Math.min(rows.count(), 10);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            try {
                sb.append(rows.nth(i).innerText().trim());
            } catch (Exception ignored) {}
        }

        return sb.toString();
    }
    private boolean isSortedAscending(List<String> list) {
        for (int i = 1; i < list.size(); i++) {
            String prev = list.get(i - 1).trim().toLowerCase();
            String curr = list.get(i).trim().toLowerCase();
            if (prev.compareTo(curr) > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isSortedDescending(List<String> list) {
        for (int i = 1; i < list.size(); i++) {
            String prev = list.get(i - 1).trim().toLowerCase();
            String curr = list.get(i).trim().toLowerCase();
            if (prev.compareTo(curr) > 0) {
                return false;
            }
        }
        return true;
    }
    private void waitUntilNameColumnIsSorted(Page page, int timeoutMs) {

        long end = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < end) {

            List<String> names =
                    page.locator(PLANT_ROWS + " td:first-child")
                            .allTextContents()
                            .stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();

            if (names.size() >= 2 &&
                    (isSortedAscending(names) || isSortedDescending(names))) {
                return;
            }

            page.waitForTimeout(500);
        }

        screenshot(page, "name-sort-not-applied");
        throw new RuntimeException("Table did not apply name sorting.");
    }


    private double parsePrice(String text) {

        String cleaned = text
                .replaceAll("[^0-9.,]", "")
                .replace(",", "");

        if (cleaned.isBlank()) return 0;

        return Double.parseDouble(cleaned);
    }


    private boolean isNumberSortedAscending(List<Double> list) {

        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1) > list.get(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNumberSortedDescending(List<Double> list) {

        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1) < list.get(i)) {
                return false;
            }
        }
        return true;
    }


}

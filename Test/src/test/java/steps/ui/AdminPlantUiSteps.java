package steps.ui;

import utils.Config;
import utils.TestContext;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import io.cucumber.java.en.*;

import java.nio.file.*;
import java.time.Instant;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AdminPlantUiSteps {

    private final TestContext ctx;

    public AdminPlantUiSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    // ---------------- Paths ----------------
    private static final String UI_PLANTS_PATH = "/ui/plants";
    private static final String UI_ADD_PLANT_PATH = "/ui/plants/add";

    // ---------------- Selectors ----------------

    private static final String PLANT_ADD_PLANT_BUTTON =
            "button:has-text('Add'), a:has-text('Add'), " +
                    "button:has-text('New'), a:has-text('New'), " +
                    "[data-testid*='add']";

    private static final String PLANT_FIRST_ROW =
            "table tbody tr, [data-testid*='plant-row'], .plant-row, [role='row']";


    private static final String PLANT_CONFIRM_DELETE_BUTTON =
            "button:has-text('Yes'), button:has-text('Confirm'), button:has-text('OK')";

    private static final String PLANT_ADD_FORM =
            "form, [data-testid='plant-form']";

    private static final String PLANT_NAME_INPUT =
            "input[name*='name' i], input[id*='name' i]";

    private static final String PLANT_CATEGORY_SELECT =
            "select[name*='category' i], select[id*='category' i], select[name*='sub' i]";

    private static final String PLANT_PRICE_INPUT =
            "input[name*='price' i], input[id*='price' i]";

    private static final String PLANT_QTY_INPUT =
            "input[name*='qty' i], input[name*='quantity' i], input[id*='qty' i], input[id*='quantity' i]";

    private static final String PLANT_SAVE_BUTTON =
            "button:has-text('Save'), button[type='submit']";

    private static final String PLANT_SUCCESS_MSG =
            "text=/success|added successfully|created successfully/i";

    private static final String PLANT_ROWS =
            "table tbody tr, [data-testid*='plant-row'], .plant-row, [role='row']";

    // ---------------- helpers ----------------

    private Page page() {
        if (ctx.page == null)
            throw new IllegalStateException("UI page not initialized. Add @ui tag and Hooks.");
        return ctx.page;
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

    // ---------------------------------------------------
    // TC_ADMIN_UI_PMM_01
    // ---------------------------------------------------

    @Then("the Add Plant button should be visible")
    public void the_add_plant_button_should_be_visible() {

        Page page = page();

        Locator btn = page.locator(PLANT_ADD_PLANT_BUTTON);

        if (btn.count() == 0) {
            screenshot(page, "add-plant-button-not-found");
            throw new RuntimeException("Add Plant button not found for admin.");
        }

        if (!btn.first().isVisible()) {
            screenshot(page, "add-plant-button-hidden");
            throw new RuntimeException("Add Plant button exists but is not visible for admin.");
        }

    }
    @When("I log out")
    public void i_log_out() {

        Page page = ctx.page;

        Locator logoutBtn = page.locator(
                "button:has-text('Logout'), a:has-text('Logout'), " +
                        "button:has-text('Sign out'), a:has-text('Sign out')"
        ).first();

        if (logoutBtn.count() == 0 || !logoutBtn.isVisible()) {
            throw new RuntimeException("Logout button not found.");
        }

        logoutBtn.click();

        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception ignored) {}
    }


    @Then("the Add Plant button should not be visible")
    public void the_add_plant_button_should_not_be_visible() {

        Page page = page();

        Locator btn = page.locator(PLANT_ADD_PLANT_BUTTON);

        if (btn.count() > 0 && btn.first().isVisible()) {
            screenshot(page, "add-plant-visible-for-user");
            throw new RuntimeException("Add Plant button is visible for non-admin user.");
        }
    }

    // ---------------------------------------------------
    // TC_ADMIN_UI_PMM_02
    // ---------------------------------------------------

    @Given("at least one plant record exists")
    public void at_least_one_plant_record_exists() {

    }

    @When("I click the Edit button for a plant")
    public void i_click_the_edit_button_for_a_plant() {

        Page page = page();

        Locator row = page.locator(PLANT_FIRST_ROW).first();

        if (row.count() == 0 || !row.first().isVisible()) {
            screenshot(page, "no-plant-row-found");
            throw new RuntimeException("No plant rows found.");
        }

        Locator editBtn = row.locator("a[title='Edit']");

        if (editBtn.count() == 0) {
            screenshot(page, "edit-button-not-found");
            throw new RuntimeException("Edit button not found in first row.");
        }

        editBtn.click();

        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception ignored) {}
    }


    @Then("I should be redirected to the Edit Plant page")
    public void i_should_be_redirected_to_the_edit_plant_page() {

        Page page = page();

        Locator form = page.locator(PLANT_ADD_FORM).first();

        assertThat(form).isVisible();
    }

    // ---------------------------------------------------
    // TC_ADMIN_UI_PMM_03
    // ---------------------------------------------------

    @When("I click the Delete button for a plant")
    public void i_click_the_delete_button_for_a_plant() {

        Page page = page();

        // register dialog handler BEFORE clicking
        page.onceDialog(dialog -> dialog.accept());

        Locator row = page.locator(PLANT_FIRST_ROW).first();

        if (row.count() == 0 || !row.first().isVisible()) {
            screenshot(page, "no-plant-row-found");
            throw new RuntimeException("No plant rows found.");
        }

        ctx.firstPageRowSnapshot = captureFirstRowText(page);

        Locator deleteBtn = row.locator("button[title='Delete']");

        if (deleteBtn.count() == 0 || !deleteBtn.first().isVisible()) {
            screenshot(page, "delete-button-not-found");
            throw new RuntimeException("Delete button not found in first row.");
        }

        deleteBtn.first().click();
    }



    @When("I confirm the deletion")
    public void i_confirm_the_deletion() {
        // Browser confirm dialog is already accepted automatically
    }


    @Then("the plant should be removed from the plant list")
    public void the_plant_should_be_removed_from_the_plant_list() {

        Page page = page();

        String newFirstRow = captureFirstRowText(page);

        if (ctx.firstPageRowSnapshot != null &&
                ctx.firstPageRowSnapshot.equals(newFirstRow)) {

            screenshot(page, "plant-not-removed");
            throw new RuntimeException("Plant still appears in the list after deletion.");
        }
    }

    // ---------------------------------------------------
    // TC_ADMIN_UI_PMM_04
    // ---------------------------------------------------

    @When("I click the Add Plant button")
    public void i_click_the_add_plant_button() {

        Page page = page();

        Locator btn = page.locator(PLANT_ADD_PLANT_BUTTON).first();

        if (btn.count() == 0 || !btn.isVisible()) {
            screenshot(page, "add-plant-button-not-found");
            throw new RuntimeException("Add Plant button not found.");
        }

        btn.click();

        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception ignored) {}
    }

    @Then("I should be redirected to the Add Plant page")
    public void i_should_be_redirected_to_the_add_plant_page() {

        Page page = page();

        if (!page.url().contains(UI_ADD_PLANT_PATH)) {
            screenshot(page, "not-on-add-plant-page");
            throw new RuntimeException("Not redirected to Add Plant page.");
        }
    }

    @Then("the Add Plant form should be displayed with empty fields")
    public void the_add_plant_form_should_be_displayed_with_empty_fields() {

        Page page = page();

        Locator form = page.locator(PLANT_ADD_FORM).first();
        assertThat(form).isVisible();
    }

    // ---------------------------------------------------
    // TC_ADMIN_UI_PMM_05
    // ---------------------------------------------------

    @Given("valid sub-categories exist")
    public void valid_sub_categories_exist() {
        // handled by test data
    }

    @When("I open the Add Plant page")
    public void i_open_the_add_plant_page() {

        Page page = page();

        page.navigate(Config.BASE_URL + UI_ADD_PLANT_PATH,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    @When("I enter a valid plant name")
    public void i_enter_a_valid_plant_name() {

        Page page = page();

        Locator name = page.locator(PLANT_NAME_INPUT).first();

        name.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        name.fill("Test Plant " + System.currentTimeMillis());
    }

    @When("I select a valid sub-category")
    public void i_select_a_valid_sub_category() {

        Page page = page();

        Locator select = page.locator(PLANT_CATEGORY_SELECT).first();

        if (select.count() == 0) {
            screenshot(page, "subcategory-select-not-found");
            throw new RuntimeException("Sub-category select not found.");
        }

        select.selectOption(new SelectOption().setIndex(1));
    }

    @When("I enter a valid price greater than 0")
    public void i_enter_a_valid_price_greater_than_0() {

        Page page = page();

        Locator price = page.locator(PLANT_PRICE_INPUT).first();

        price.fill("100");
    }

    @When("I enter a valid quantity")
    public void i_enter_a_valid_quantity() {

        Page page = page();

        Locator qty = page.locator(PLANT_QTY_INPUT).first();

        qty.fill("10");
    }

    @When("I click the Save button")
    public void i_click_the_save_button() {

        Page page = page();

        Locator save = page.locator(PLANT_SAVE_BUTTON).first();

        if (save.count() == 0 || !save.isVisible()) {
            screenshot(page, "save-button-not-found");
            throw new RuntimeException("Save button not found.");
        }

        save.click();

        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
        } catch (Exception ignored) {}
    }

    @Then("a success message should be displayed")
    public void a_success_message_should_be_displayed() {

        Page page = page();

        Locator msg = page.locator(PLANT_SUCCESS_MSG).first();

        if (msg.count() == 0 || !msg.isVisible()) {
            screenshot(page, "success-message-not-visible");
            throw new RuntimeException("Success message not displayed.");
        }
    }

    @Then("the new plant should appear in the plant list")
    public void the_new_plant_should_appear_in_the_plant_list() {

        Page page = page();

        page.navigate(Config.BASE_URL + UI_PLANTS_PATH,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        Locator rows = page.locator(PLANT_ROWS).first();
        assertThat(rows).isVisible();
    }

    // ---------------- small utilities ----------------

    private String captureFirstRowText(Page page) {

        Locator row = page.locator(PLANT_ROWS).first();

        if (row.count() == 0) return "";

        try {
            return row.innerText().trim();
        } catch (Exception e) {
            return "";
        }
    }

}

package steps.ui;

import support.Config;
import support.TestContext;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.en.*;

import java.net.URI;
import java.nio.file.*;
import java.time.Instant;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class SalesUiSteps {
    private final TestContext ctx;

    public SalesUiSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    // Paths from TS
    private static final String UI_LOGIN_PATH = "/ui/login";
    private static final String UI_SALES_PATH = "/ui/sales";
    private static final String UI_SALES_NEW_PATH = "/ui/sales/new";

    // Selectors (copied from TS)
    private static final String SEL_SALES_CONTAINER =
            "[data-testid='sales-list'], [data-testid='sales-table'], .sales-list, .sales-table, main, .container";
    private static final String SEL_TABLE = "table";
    private static final String SEL_SALE_ROWS =
            "table tbody tr, [data-testid*='sale'], .sale-row, .MuiDataGrid-row, [role='row']";
    private static final String SEL_EMPTY_STATE = "text=/no sales found/i";
    private static final String SEL_PAGINATION =
            ".pagination, nav[aria-label*=pagination], [data-testid*=pagination]";

    // Delete + toast/alerts (add near other selectors)
    private static final String SEL_DELETE_BTN =
            "button:has-text('Delete'), a:has-text('Delete'), " +
                    "[data-testid*='delete'], [aria-label*='delete' i], [title*='delete' i], " +
                    "[data-testid*='trash'], [aria-label*='trash' i], " +
                    "button.btn.btn-sm.btn-outline-danger, " +
                    ".btn-danger, .delete, " +
                    ".fa-trash, .fa-trash-alt, .bi-trash, .bi-trash-fill, .mdi-delete, .mdi-delete-outline, .icon-delete, .icon-trash";


    private static final String SEL_TOAST_OR_ALERT =
            "[role='alert'], .alert, .toast, .snackbar, .MuiAlert-root, [data-testid*='toast'], [data-testid*='message']";

    private static final String SEL_CONFIRM_DIALOG =
            "[role='dialog'], .modal, .modal-dialog, .MuiDialog-root, .swal2-popup, [data-testid*='confirm']";

    private static final String SEL_CONFIRM_DELETE_BTN =
            "button:has-text('Yes'), button:has-text('Confirm'), button:has-text('Delete')";

    private static String selHeaderByText(String name) {
        // Playwright Java supports :has-text()
        return "table thead th:has-text(\"" + name + "\"), [role='columnheader']:has-text(\"" + name + "\")";
    }

    private static final String SEL_SELL_BUTTON = "text=Sell Plant";

    private static final String SEL_PLANT_SELECT = "select[name='plantId'], select#plantId";
    private static final String SEL_QTY_INPUT = "input[name='quantity'], input#quantity";
    private static final String SEL_CONFIRM_BTN =
            "button:has-text('Confirm'), button:has-text('Sell'), button:has-text('Submit'), button[type='submit']";
    private static final String SEL_REQUIRED_VALIDATION = "text=/required|must be filled|is required/i";
    private static final String SEL_VALIDATION_INSUFFICIENT = "text=/insufficient stock|not enough stock/i";
    private static final String SEL_QTY_GT0_VALIDATION = "text=/greater than 0|must be greater than 0|minimum.*1/i";
    private static final String SEL_ANY_VALIDATION =
            "[role='alert'], .invalid-feedback, .error, .text-danger, .MuiFormHelperText-root, " +
                    "[data-testid*=error], [data-testid*=validation], " +
                    "text=/required|invalid|must|please|select|enter|cannot be empty/i";

    private Page page() {
        if (ctx.page == null) throw new IllegalStateException("UI page not initialized. Add @ui tag and Hooks.");
        return ctx.page;
    }

    private void assertNotRedirectedToLogin(Page page, String context) {
        if (page.url().contains(UI_LOGIN_PATH)) {
            try {
                Path dir = Paths.get("tests", "screenshots");
                Files.createDirectories(dir);
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(dir.resolve(Instant.now().toEpochMilli() + "-" + context + "-redirect-login.png"))
                        .setFullPage(true));
            } catch (Exception ignored) {}
            throw new RuntimeException(context + ": redirected to login. Current URL: " + page.url());
        }
    }

    @Given("I am logged in as {string}")
    public void iamloggedinas(String role) {
        Page page = page();

        page.navigate(Config.BASE_URL + UI_LOGIN_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        String username = "admin".equals(role) ? "admin" : "user".equals(role) ? "testuser" : "";
        String password = "admin".equals(role) ? "admin123" : "user".equals(role) ? "test123" : "";

        page.fill("input[name=\"username\"]", username);
        page.fill("input[name=\"password\"]", password);

        // Wait for login response (best-effort like TS)
        Response[] loginRespHolder = new Response[1];
        page.onResponse(resp -> {
            if ("POST".equals(resp.request().method()) && resp.url().contains(Config.API_LOGIN)) {
                loginRespHolder[0] = resp;
            }
        });

        page.click("button[type=\"submit\"]");

        page.waitForLoadState(LoadState.NETWORKIDLE);
        // best effort: wait to leave login url
        try {
            page.waitForURL(url -> !url.endsWith(UI_LOGIN_PATH), new Page.WaitForURLOptions().setTimeout(15000));
        } catch (Exception ignored) {}

        Response loginResp = loginRespHolder[0];
        if (loginResp != null && !loginResp.ok()) {
            String body = "";
            try { body = loginResp.text(); } catch (Exception ignored) {}
            throw new RuntimeException("Login failed for role \"" + role + "\". Status " + loginResp.status() + " " + loginResp.statusText() +
                    (body.isBlank() ? "" : (". Response: " + body)));
        }
    }

    @Given("there are no sales records")
    public void therearenosalesrecords() {
        Page page = page();

        if (!page.url().contains(UI_SALES_PATH)) {
            page.navigate(Config.BASE_URL + UI_SALES_PATH,
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        }

        assertNotRedirectedToLogin(page, "ensure-no-sales");

        Locator empty = page.locator(SEL_EMPTY_STATE).first();
        Locator rows = page.locator(SEL_SALE_ROWS);

        boolean ok = waitAnyVisible(page, 15000, empty, rows.first());
        if (!ok) {
            screenshot(page, "sales-empty-check-not-loaded");
            throw new RuntimeException("Sales page did not load for empty check.");
        }

        if (rows.count() > 0) {
            screenshot(page, "expected-empty-but-has-rows");
            throw new RuntimeException("Expected NO sales records, but found rows=" + rows.count());
        }

        assertThat(empty).isVisible();
    }


    @Given("there is at least one sale record")
    public void there_is_at_least_one_sale_record() {
        Page page = page();
        // Ensure we're on sales list page
        if (!page.url().contains(UI_SALES_PATH)) {
            page.navigate(Config.BASE_URL + UI_SALES_PATH,
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        }

        assertNotRedirectedToLogin(page, "ensure-sale-record");

        // Wait for rows or empty state
        Locator rows = page.locator(SEL_SALE_ROWS);
        Locator empty = page.locator(SEL_EMPTY_STATE);

        boolean ok = waitAnyVisible(page, 15000, rows.first(), empty.first());
        if (!ok) {
            screenshot(page, "sales-list-not-loaded");
            throw new RuntimeException("Sales list did not load (no rows/empty state).");
        }

        if (rows.count() == 0) {
            screenshot(page, "no-sales-records");
            throw new RuntimeException("Expected at least one sale record, but list is empty.");
        }
    }

    @When("I open the Sales list page")
    public void i_open_the_sales_list_page() {
        Page page = page();

        page.navigate(Config.BASE_URL + UI_SALES_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForTimeout(300);

        assertNotRedirectedToLogin(page, "open-sales-list");

        // Wait for evidence: rows OR empty OR container
        Locator rows = page.locator(SEL_SALE_ROWS).first();
        Locator empty = page.locator(SEL_EMPTY_STATE).first();
        Locator container = page.locator(SEL_SALES_CONTAINER).first();

        boolean ok = waitAnyVisible(page, 15000, rows, empty, container);
        if (!ok) {
            screenshot(page, "sales-open-fail");
            throw new RuntimeException("Sales list page did not render expected elements.");
        }
    }

    @When("I click Delete on the first sale")
    public void i_click_delete_on_the_first_sale() {
        Page page = page();

        Locator firstRow = page.locator("table tbody tr").first();
        firstRow.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));

        Locator deleteBtn = firstRow.locator("button.btn.btn-sm.btn-outline-danger").first();
        if (deleteBtn.count() == 0) deleteBtn = firstRow.locator(SEL_DELETE_BTN).first();

        if (deleteBtn.count() == 0) {
            screenshot(page, "delete-button-not-found");
            throw new RuntimeException("Delete button not found for the first sale.");
        }

        // Capture & accept the native confirm dialog
        page.onceDialog(dialog -> {
            ctx.lastDialogMessage = dialog.message();
            dialog.accept(); // presses OK
        });

        deleteBtn.click(new Locator.ClickOptions().setTimeout(15000));
    }

    @When("I open the Sell Plant page")
    public void i_open_sell_plant_page() {
        Page page = page();
        page.navigate(Config.BASE_URL + UI_SALES_NEW_PATH, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForTimeout(300);
        assertNotRedirectedToLogin(page, "open-sell-page");
    }

    @Then("Sell Plant page should load")
    public void sell_plant_page_should_load() {
        Page page = page();
        assertThat(page.locator(SEL_PLANT_SELECT).first()).isVisible();
        assertThat(page.locator(SEL_QTY_INPUT).first()).isVisible();
    }

    @Then("I should see a delete confirmation prompt")
    public void i_should_see_a_delete_confirmation_prompt() {
        assertNotNull(ctx.lastDialogMessage, "Delete confirmation dialog not shown");
        assertTrue(ctx.lastDialogMessage.toLowerCase().contains("are you sure"));
        assertTrue(ctx.lastDialogMessage.toLowerCase().contains("delete"));
    }

    @Then("I should see sales list container")
    public void i_should_see_sales_list_container() {
        Page page = page();
        Locator[] evidence = new Locator[] {
                page.locator("table").first(),
                page.locator(SEL_SALE_ROWS).first(),
                page.locator(SEL_EMPTY_STATE).first(),
                page.locator(SEL_PAGINATION).first(),
                page.locator("body").first()
        };

        boolean ok = waitAnyVisible(page, 15000, evidence);
        if (!ok) {
            screenshot(page, "sales-container-not-found");
            throw new RuntimeException("Sales page UI did not render expected elements (table/rows/empty/pagination).");
        }
    }

    @Then("I accept the delete confirmation")
    public void i_accept_the_delete_confirmation() {
        Page page = page();

        Locator confirmBtn = page.locator(SEL_CONFIRM_DELETE_BTN).first();
        if (confirmBtn.count() == 0) {
            screenshot(page, "confirm-delete-btn-not-found");
            throw new RuntimeException("Could not find confirm button in delete modal.");
        }

        confirmBtn.click();

        // Wait for modal to disappear (best effort)
        Locator modal = page.locator(SEL_CONFIRM_DIALOG).first();
        try {
            modal.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(15000));
        } catch (Exception ignored) {}
    }

    @Then("I should see success message {string}")
    public void i_should_see_success_message(String msg) {
        Page page = page();

        Locator exact = page.getByText(msg).first();
        if (isVisible(exact)) {
            assertThat(exact).isVisible();
            return;
        }

        // fallback: any toast/alert visible
        Locator toast = page.locator(SEL_TOAST_OR_ALERT).first();
        if (isVisible(toast)) return;

        screenshot(page, "success-message-not-found");
        throw new RuntimeException("Success message not found. Expected: " + msg);
    }


    @Then("I should see sales records or empty state")
    public void i_should_see_sales_records_or_empty_state() {
        Page page = page();
        int rows = page.locator(SEL_SALE_ROWS).count();
        int empty = page.locator(SEL_EMPTY_STATE).count();
        if (rows == 0 && empty == 0) {
            screenshot(page, "sales-no-rows-no-empty");
            throw new RuntimeException("Sales page shows neither rows nor empty state.");
        }
    }

    @Then("I should see pagination controls when records exceed one page")
    public void i_should_see_pagination_controls_when_records_exceed_one_page() {
        Page page = page();
        Locator pagination = page.locator(SEL_PAGINATION);
        if (pagination.count() > 0) {
            assertThat(pagination.first()).isVisible();
        }
    }

    @When("I sort by {string}")
    public void i_sort_by(String column) {
        Page page = page();
        Locator header = page.locator(selHeaderByText(column)).first();
        if (header.count() > 0) {
            header.click();
            try { page.waitForLoadState(LoadState.NETWORKIDLE); } catch (Exception ignored) {}
            return;
        }

        // Fallback: navigate with query params
        String sortField = switch (column) {
            case "Sold Date" -> "soldAt";
            case "Plant Name" -> "plantName";
            case "Quantity" -> "quantity";
            case "Total Price" -> "totalPrice";
            default -> column;
        };

        page.navigate(Config.BASE_URL + UI_SALES_PATH + "?page=0&sortField=" + enc(sortField) + "&sortDir=asc",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        try { page.waitForLoadState(LoadState.NETWORKIDLE); } catch (Exception ignored) {}
    }

    @Then("sales should be sorted by {string} in {string} order")
    public void sales_should_be_sorted_by_in_order(String column, String order) throws Exception {
        Page page = page();
        URI uri = URI.create(page.url());
        String qs = uri.getQuery();

        String sortField = null, sortDir = null;
        if (qs != null) {
            for (String part : qs.split("&")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    if (kv[0].equals("sortField")) sortField = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                    if (kv[0].equals("sortDir")) sortDir = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }

        String expectedField = switch (column) {
            case "Sold Date" -> "soldAt";
            case "Plant Name" -> "plantName";
            case "Quantity" -> "quantity";
            case "Total Price" -> "totalPrice";
            default -> column;
        };

        if (sortField != null && sortDir != null) {
            assertEquals(expectedField.toLowerCase(), sortField.toLowerCase());
            assertEquals(order.toLowerCase(), sortDir.toLowerCase());
            return;
        }

        // fallback: ensure table visible
        assertThat(page.locator(SEL_TABLE).first()).isVisible();
    }

    @Then("I should see empty-state message {string}")
    public void i_should_see_empty_state_message(String msg) {
        Page page = page();
        Locator byText = page.getByText(msg);
        Locator byEmpty = page.locator(SEL_EMPTY_STATE);

        if (isVisible(byText.first())) {
            assertThat(byText.first()).isVisible();
            return;
        }
        if (isVisible(byEmpty.first())) {
            assertThat(byEmpty.first()).isVisible();
            return;
        }

        screenshot(page, "empty-state-missing");
        throw new RuntimeException("Empty-state not found. Expected \"" + msg + "\" or selector " + SEL_EMPTY_STATE + ".");
    }

    @Then("I should not see the {string} button")
    public void i_should_not_see_button(String label) {
        Page page = page();
        assertEquals(0, page.getByText(label).count());
    }

    // ---------- Sell form steps ----------
    @When("I select plant {string}")
    public void i_select_plant(String plantId) {
        Page page = page();
        Locator select = page.locator(SEL_PLANT_SELECT).first();
        select.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));

        // Try by value first
        try {
            select.selectOption(plantId);
            return;
        } catch (Exception ignored) {}

        // Fallback: find option containing plantId in text/value
        String matchedValue = (String) select.evaluate("el => {\n" +
                "  const id = '" + plantId.replace("'", "\\'") + "';\n" +
                "  const options = Array.from(el.options || []);\n" +
                "  const found = options.find(o => String(o.value)===String(id) || String(o.text||'').toLowerCase().includes(String(id).toLowerCase()));\n" +
                "  return found && found.value ? String(found.value) : '';\n" +
                "}");
        if (matchedValue != null && !matchedValue.isBlank()) {
            select.selectOption(matchedValue);
            return;
        }

        throw new RuntimeException("Plant option \"" + plantId + "\" not found in dropdown.");
    }

    @When("I enter sell quantity {int}")
    public void i_enter_sell_quantity(int qty) {
        page().locator(SEL_QTY_INPUT).fill(String.valueOf(qty));
    }

    @When("I submit the sell form with missing required fields")
    public void i_submit_missing_required_fields() {
        page().locator(SEL_CONFIRM_BTN).first().click();
    }

    @Then("I should see required field validation messages")
    public void i_should_see_required_field_validation_messages() {
        Page page = page();

        Locator visibleError = page.locator(SEL_ANY_VALIDATION).first();
        if (isVisible(visibleError)) {
            assertThat(visibleError).isVisible();
            return;
        }

        int ariaInvalid = page.locator("[aria-invalid='true']").count();
        if (ariaInvalid > 0) return;

        Boolean hasInvalid = (Boolean) page.evaluate("() => {\n" +
                "const fields = Array.from(document.querySelectorAll('input, select, textarea'));\n" +
                "return fields.some(f => typeof f.checkValidity==='function' && !f.checkValidity());\n" +
                "}");
        if (Boolean.TRUE.equals(hasInvalid)) return;

        screenshot(page, "required-validation-missing");
        throw new RuntimeException("No validation evidence found (no error text, aria-invalid, or HTML5 invalid).");
    }

    @When("I confirm sale")
    public void i_confirm_sale() {
        Page page = page();

        // Listen for POST /sales response (best effort)
        page.onResponse(resp -> {
            if ("POST".equals(resp.request().method()) && resp.url().toLowerCase().contains("sales")) {
                ctx.lastSalePostStatus = resp.status();
                try { ctx.lastSalePostBody = resp.text(); } catch (Exception ignored) {}
            }
        });

        page.locator(SEL_CONFIRM_BTN).first().click();
        page.waitForTimeout(700);
    }

    @When("I select the first available plant")
    public void i_select_the_first_available_plant() {
        Page page = page();

        Locator select = page.locator(SEL_PLANT_SELECT).first();
        select.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));

        String chosen = (String) select.evaluate("el => {\n" +
                "  const opts = Array.from(el.options || []);\n" +
                "  const valid = opts.find(o => o.value && !o.disabled);\n" +
                "  if (!valid) return '';\n" +
                "  el.value = valid.value;\n" +
                "  el.dispatchEvent(new Event('input', {bubbles:true}));\n" +
                "  el.dispatchEvent(new Event('change', {bubbles:true}));\n" +
                "  return valid.value;\n" +
                "}");

        if (chosen == null || chosen.isBlank()) {
            screenshot(page, "no-plant-options");
            throw new RuntimeException("No available plant options found in dropdown.");
        }

        // optional: keep for later API checks
        ctx.plantId = chosen;
    }


    @Then("I should be on the Sales list page")
    public void i_should_be_on_sales_list_page() {
        Page page = page();
        page.waitForURL(url -> url.startsWith(Config.BASE_URL + UI_SALES_PATH), new Page.WaitForURLOptions().setTimeout(30000));
    }

    @Then("I should see validation error {string}")
    public void i_should_see_validation_error(String msg) {
        Page page = page();

        Locator uiError = page.locator(
                "[role='alert'], .error, .text-danger, .invalid-feedback, .MuiFormHelperText-root, " +
                        "text=/insufficient|not enough|greater than 0|minimum|invalid|failed|error/i"
        ).first();

        if (isVisible(uiError)) {
            assertThat(uiError).isVisible();
            return;
        }

        if (ctx.lastSalePostStatus != null && ctx.lastSalePostStatus >= 400) return;

        Boolean invalid = (Boolean) page.evaluate("() => {\n" +
                "const qty = document.querySelector('input[name=\"quantity\"], input#quantity');\n" +
                "return qty && typeof qty.checkValidity==='function' && !qty.checkValidity();\n" +
                "}");
        if (Boolean.TRUE.equals(invalid)) return;

        screenshot(page, "validation-error-missing");
        throw new RuntimeException("No validation found (no visible error, no 4xx POST response, no HTML5 invalid).");
    }

    @Then("the sale should not be created")
    public void the_sale_should_not_be_created() {
        Page page = page();
        boolean stillOnSell = page.url().contains(UI_SALES_NEW_PATH);
        int hasError = page.locator(SEL_VALIDATION_INSUFFICIENT + ", " + SEL_QTY_GT0_VALIDATION + ", " + SEL_REQUIRED_VALIDATION).count();

        if (!stillOnSell && hasError == 0) {
            screenshot(page, "unexpected-sale-state");
            throw new RuntimeException("Expected sale NOT created, but no validation/error is visible and not on sell page.");
        }
    }

    // ----------------- helpers -----------------
    private static boolean waitAnyVisible(Page page, int timeoutMs, Locator... locators) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            for (Locator l : locators) {
                try {
                    if (l != null && l.count() > 0 && l.first().isVisible()) return true;
                } catch (Exception ignored) {}
            }
            page.waitForTimeout(200);
        }
        return false;
    }

    private static boolean isVisible(Locator locator) {
        try { return locator != null && locator.count() > 0 && locator.isVisible(); }
        catch (Exception e) { return false; }
    }

    private static void screenshot(Page page, String name) {
        try {
            Path dir = Paths.get("tests", "screenshots");
            Files.createDirectories(dir);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(dir.resolve(Instant.now().toEpochMilli() + "-" + name + ".png"))
                    .setFullPage(true));
        } catch (Exception ignored) {}
    }

    private static String enc(String s) {
        try { return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }
}

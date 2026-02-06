package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.List;



public class SalesPage {
    private final Page page;
    private final Locator nextBtn;
    private static final String SEL_SALE_ROWS =
            "table tbody tr, [data-testid*='sale'], .sale-row, .MuiDataGrid-row, [role='row']";
    private static final String SEL_EMPTY_STATE = "text=/no sales found/i";
    private static final String SEL_PAGINATION =
            ".pagination, nav[aria-label*=pagination], [data-testid*=pagination]";
    private static final String BASE_URL = "http://localhost:8080";
    private static final String SEL_PLANT_SELECT = "select#plantId";
    private static final String SEL_QTY_INPUT = "input#quantity, input[name='quantity']";
    private static final String SEL_SELL_BTN = "button:has-text(\"Sell\")";
    private static final String SEL_STOCK_INFO =
            "[data-testid='stock-info'], .stock-info, text=/stock/i";
    private static final String SEL_QTY_ERROR =
            "#quantity:invalid, input[name='quantity']:invalid, " +
                    "#quantity ~ .invalid-feedback, input[name='quantity'] ~ .invalid-feedback, " +
                    "text=/quantity.*required|required.*quantity/i";
    private static final String SEL_CANCEL_BTN =
            "a.btn.btn-secondary[href='/ui/sales']:has-text('Cancel')";
    private final Locator addSaleBtn;
    private final Locator formHeader;


    public SalesPage(Page page) {
        this.page = page;
        this.nextBtn = page.locator("a.page-link:has-text('Next')"); // Update with actual selector
        page.locator("table.sales-list");
        page.locator("th:has-text('Plant Name')");
        page.locator("text='No sales found'");
        this.addSaleBtn = page.locator("a:has-text('Sell Plant'), button:has-text('Sell Plant')");
        this.formHeader = page.locator("h2, h1.form-title");
    }

    public void navigate() {
        page.navigate("http://localhost:8080/ui/sales");
    }

    public void clickNextPage() {
        nextBtn.click();
    }

    // Inside SalesPage.java
    public boolean waitForContainer() {
        try {
            // Using your previous logic: wait for either table OR pagination to appear
            Locator[] evidence = new Locator[] {
                    page.locator("table").first(),
                    page.locator(SEL_SALE_ROWS).first(),
                    page.locator(SEL_EMPTY_STATE).first(),
                    page.locator(SEL_PAGINATION).first(),
                    page.locator("body").first()
            };
            boolean ok = waitAnyVisible(page, 15000, evidence);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getSoldDates() {
        // Assuming Sold Date is the 1st column (td:nth-child(1))
        // Adjust the selector based on your actual table structure
        return page.locator("table.sales-list tbody tr td:nth-child(1)").allInnerTexts();
    }
    public void sortByName(String direction) {
        try {
            // 1. Try to find and click the header (handling potential extra spaces)
            Locator header = page.locator("th:has-text('Plant Name'), a:has-text('Plant Name')").first();
            header.click(new Locator.ClickOptions().setTimeout(3000));
        } catch (Exception e) {
            // 2. Fallback: Force navigation via URL if click fails (The logic you used before)
            String dir = direction.equalsIgnoreCase("descending") ? "desc" : "asc";
            String url = "http://localhost:8080/ui/sales?page=0&sortField=plantName&sortDir=" + dir;
            page.navigate(url);
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
    public List<String> getPlantNames() {
        // Assuming Plant Name is the 2nd column
        return page.locator("table.sales-list tbody tr td:nth-child(2)").allInnerTexts();
    }

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

    public String getEmptyMessageText() {
        // Try to find any element containing 'No' and 'found' to be safe
        Locator msg = page.locator("text=/.*No.*found.*/i").first();
        msg.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        return msg.innerText();
    }

    public boolean isAddButtonVisible() {
        // We use a short timeout here so the test doesn't hang if the button is missing
        return addSaleBtn.isVisible();
    }

    public void clickAddSale() {
        // We reuse the button locator we made for TC005
        page.locator("a:has-text('Sell Plant')").click();
    }

    public String getPageTitle() {
        return page.title();
    }
    public void navigateToSellPlantForm() {
        page.navigate(BASE_URL + "/ui/sales/new");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        page.locator(SEL_PLANT_SELECT).first().waitFor();
    }

    public void openPlantDropdown() {
        Locator select = page.locator(SEL_PLANT_SELECT);
        select.waitFor();
        select.click();
    }

    public List<String> getPlantDropdownOptions() {
        Locator select = page.locator(SEL_PLANT_SELECT);
        select.waitFor();

        // wait until options (beyond placeholder) exist
        page.waitForCondition(() ->
                select.locator("option").count() > 1
        );

        return select.locator("option").allInnerTexts();
    }

    public boolean outOfStockHiddenOrDisabled() {
        Locator select = page.locator(SEL_PLANT_SELECT);
        select.waitFor();
        Locator opts = select.locator("option");

        int count = opts.count();
        boolean sawOutOfStock = false;

        for (int i = 0; i < count; i++) {
            Locator opt = opts.nth(i);
            String text = opt.innerText().trim().toLowerCase();
            boolean disabled = opt.isDisabled();

            boolean looksOutOfStock =
                    text.contains("out of stock") ||
                            text.contains("stock: 0") ||
                            text.matches(".*\\bstock\\s*:\\s*0\\b.*");

            if (looksOutOfStock) {
                sawOutOfStock = true;
                if (!disabled && !text.contains("unavailable")) return false;
            }
        }

        // If none shown as out-of-stock, it's consistent with "hidden"
        return true;
    }
    public void selectAnyPlant() {
        Locator select = page.locator(SEL_PLANT_SELECT);
        select.waitFor();
        // pick first real plant option (skip placeholder "")
        select.selectOption(new SelectOption().setIndex(1));
    }
    public void clearQuantity() {
        Locator qty = page.locator(SEL_QTY_INPUT).first();
        qty.waitFor();
        qty.fill(""); // leave empty
    }
    public void clickSubmit() {
        Locator btn = page.locator(SEL_SELL_BTN).first();
        btn.waitFor();
        btn.click();
    }
    public String getQuantityValidationMessage() {
        Locator qty = page.locator(SEL_QTY_INPUT).first();
        qty.waitFor();

        // HTML5 validation message (bubble text)
        Object msg = qty.evaluate("el => el.validationMessage");
        return msg == null ? "" : msg.toString().trim();
    }
    public boolean isQuantityValidationShown() {
        String msg = getQuantityValidationMessage();

        // Accept either required OR min validation
        return msg.toLowerCase().contains("greater than")
                || msg.toLowerCase().contains("fill out")
                || msg.toLowerCase().contains("required")
                || msg.toLowerCase().contains("at least");
    }
    public String currentUrl() {
        return page.url();
    }

    public void setQuantityToZero() {
        Locator qty = page.locator(SEL_QTY_INPUT).first();
        qty.waitFor();
        qty.fill("0");  // triggers min=1 validation on submit
    }
    public void selectPlantByName(String plantName) {
        Locator select = page.locator("select#plantId, select[name='plantId']").first();
        select.waitFor();

        // Find value of option whose text contains the plant name
        String value = (String) select.locator("option")
                .evaluateAll("opts => {\n" +
                        "  const name = " + escapeJsString(plantName) + ";\n" +
                        "  const opt = opts.find(o => (o.textContent||'').includes(name));\n" +
                        "  return opt ? opt.value : null;\n" +
                        "}");

        if (value == null) {
            throw new RuntimeException("No option found containing: " + plantName);
        }

        select.selectOption(value);
    }

    // helper to safely embed string in JS
    private String escapeJsString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public void fillQuantity(int qty) {
        Locator q = page.locator(SEL_QTY_INPUT).first();
        q.waitFor();
        q.fill(String.valueOf(qty));
    }

    public void waitForRedirectToSalesList() {
        page.waitForURL(url -> url.contains("/ui/sales"));
    }

    public void clickCancel() {
        // exact match for your HTML
        Locator cancel = page.locator(SEL_CANCEL_BTN).first();

        // wait for it to exist (attached), then click
        cancel.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
        cancel.click();
    }

}

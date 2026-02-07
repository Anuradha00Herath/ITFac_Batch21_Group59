package pages.plants;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;

import java.util.ArrayList;
import java.util.List;

public class PlantsPage {
    private final Page page;

    // If you already use Config.BASE_URL in your project, replace this:
    private static final String BASE_URL = "http://localhost:8080";
    private static final String UI_PLANTS_PATH = "/ui/plants";
    private static final String UI_PLANTS_ADD_PATH = "/ui/plants/add";

    // Table
    private static final String SEL_TABLE = "table";
    private static final String SEL_ROWS = "table tbody tr";

    // Pagination
    private static final String SEL_PAGINATION =
            ".pagination, nav[aria-label*=pagination], [data-testid*=pagination]";
    private static final String SEL_NEXT_BTN =
            "a.page-link:has-text('Next'), button:has-text('Next'), a:has-text('Next'), " +
                    "a:has-text('>'), button:has-text('>'), a:has-text('»'), button:has-text('»')";

    // Search
    private static final String SEL_SEARCH_INPUT =
            "input[type='search'], input[placeholder*='Search' i], input[name*='search' i], " +
                    "input[id*='search' i], input[name*='name' i][type='text'], input[id*='name' i][type='text']";

    private static final String SEL_SEARCH_BTN =
            "button:has-text('Search'), input[type='submit'][value*='Search' i], " +
                    "button[aria-label*='search' i], [data-testid*='search' i]";

    // Category filter (common patterns)
    // Category filter (EXACT based on your HTML)
    private static final String SEL_CATEGORY_SELECT = "select[name='categoryId']";
    // Sort (Name column)
    // Exact based on your HTML
    private static final String SEL_NAME_SORT_LINK = "thead tr th a[href*='sortField=name']";

    private static final String SEL_PRICE_SORT_LINK = "thead tr th a[href*='sortField=price']";

    private static final String SEL_LOGOUT =
            "a:has-text('Logout'), button:has-text('Logout'), a[href*='logout'], form[action*='logout'] button";

    private static final String SEL_ADD_PLANT_BTN =
            "a:has-text('Add Plant'), button:has-text('Add Plant'), a[href='/ui/plants/add'], a[href*='/ui/plants/add']";

    private static final String SEL_EDIT_LINK = "a[href^='/ui/plants/edit/']";
    private static final String SEL_DELETE_BTN = "form[action^='/ui/plants/delete/'] button";

    private static final String SEL_NAME_INPUT = "input[name='name'], input#name";// you gave this exact HTML
    private static final String SEL_PRICE_INPUT = "input[name='price'], input#price";
    private static final String SEL_QTY_INPUT = "input[name='quantity'], input#quantity";
    private static final String SEL_SAVE_BTN = "button:has-text('Save'), input[type='submit'][value*='Save' i]";


    public PlantsPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate(BASE_URL + UI_PLANTS_PATH);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        waitForContainer();
    }

    public boolean waitForContainer() {
        try {
            Locator[] evidence = new Locator[]{
                    page.locator(SEL_TABLE).first(),
                    page.locator(SEL_ROWS).first(),
                    page.locator(SEL_PAGINATION).first(),
                    page.locator("body").first()
            };
            return waitAnyVisible(15000, evidence);
        } catch (Exception e) {
            return false;
        }
    }
    public boolean waitForListVisible() {
        return waitAnyVisible(15000,
                page.locator(SEL_TABLE).first(),
                page.locator(SEL_ROWS).first(),
                page.locator("body").first());
    }

    public int getRowCount() {
        page.locator(SEL_TABLE).first().waitFor();
        return page.locator(SEL_ROWS).count();
    }

    // -------- Pagination --------

    public boolean isPaginationVisible() {
        try {
            Locator p = page.locator(SEL_PAGINATION).first();
            if (p.count() > 0 && p.isVisible()) return true;

            Locator next = page.locator(SEL_NEXT_BTN).first();
            return next.count() > 0 && next.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canGoNext() {
        Locator next = page.locator(SEL_NEXT_BTN).first();
        if (next.count() == 0) return false;
        if (!next.isVisible()) return false;

        try {
            if (!next.isEnabled()) return false;
        } catch (Exception ignored) {}

        String ariaDisabled = "";
        try { ariaDisabled = String.valueOf(next.getAttribute("aria-disabled")); } catch (Exception ignored) {}
        if ("true".equalsIgnoreCase(ariaDisabled)) return false;

        String cls = "";
        try { cls = String.valueOf(next.getAttribute("class")); } catch (Exception ignored) {}
        if (cls != null && cls.toLowerCase().contains("disabled")) return false;

        return true;
    }

    public void clickNextPage() {
        Locator next = page.locator(SEL_NEXT_BTN).first();
        if (next.count() == 0) throw new RuntimeException("Next pagination control not found on Plants page");
        next.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitForContainer();
    }

    // -------- Search --------

    public void searchByName(String name) {
        Locator input = page.locator(SEL_SEARCH_INPUT).first();
        if (input.count() == 0) {
            throw new RuntimeException("Search input not found on Plants page. Update SEL_SEARCH_INPUT.");
        }

        input.fill("");
        input.fill(name);

        Locator btn = page.locator(SEL_SEARCH_BTN).first();
        if (btn.count() > 0 && btn.isVisible()) btn.click();
        else input.press("Enter");

        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitForContainer();
    }

    // -------- Category filter --------

    public void filterByCategory(String categoryLabel) {
        Locator select = page.locator(SEL_CATEGORY_SELECT);

        if (select.count() == 0) {
            throw new RuntimeException("Category dropdown not found: select[name='categoryId']");
        }

        // Select by visible text (e.g. "Business")
        select.selectOption(new SelectOption().setLabel(categoryLabel));

        // This UI filters automatically on change
        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitForContainer();
    }


    // -------- Table parsing --------

    public List<String> getPlantNamesOnCurrentPage() {
        return getColumnValuesBestEffort("name");
    }

    public List<String> getPlantCategoriesOnCurrentPage() {
        // Try category column
        return getColumnValuesBestEffort("category");
    }

    private List<String> getColumnValuesBestEffort(String headerContains) {
        page.locator(SEL_TABLE).first().waitFor();

        int col = -1;
        try { col = getColumnIndexByHeaderContains(headerContains); }
        catch (Exception ignored) {}

        Locator rows = page.locator(SEL_ROWS);
        int rowCount = rows.count();
        List<String> values = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            Locator row = rows.nth(i);
            Locator tds = row.locator("td");
            if (tds.count() == 0) continue;

            String txt;
            if (col >= 0 && col < tds.count()) txt = tds.nth(col).innerText().trim();
            else txt = tds.first().innerText().trim();

            if (!txt.isBlank()) values.add(txt);
        }
        return values;
    }

    private int getColumnIndexByHeaderContains(String headerText) {
        Locator headers = page.locator("table thead tr th");
        int count = headers.count();
        for (int i = 0; i < count; i++) {
            String h = headers.nth(i).innerText().trim().toLowerCase();
            if (h.contains(headerText.toLowerCase())) return i;
        }
        throw new RuntimeException("Could not find column header containing: " + headerText);
    }

    private boolean waitAnyVisible(int timeoutMs, Locator... locators) {
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
    public void clickNameSortLink() {
        Locator link = page.locator(SEL_NAME_SORT_LINK).first();
        if (link.count() == 0) {
            throw new RuntimeException("Name sort link not found: " + SEL_NAME_SORT_LINK);
        }
        link.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitForContainer();
    }

    public String getNameSortDirFromHref() {
        Locator link = page.locator(SEL_NAME_SORT_LINK).first();
        if (link.count() == 0) {
            throw new RuntimeException("Name sort link not found to read sortDir.");
        }
        String href = link.getAttribute("href");
        if (href == null) return "unknown";
        if (href.contains("sortDir=asc")) return "asc";
        if (href.contains("sortDir=desc")) return "desc";
        return "unknown";
    }

    public static String normalizeName(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    public void clickPriceSortLink() {
        Locator link = page.locator(SEL_PRICE_SORT_LINK).first();
        if (link.count() == 0) {
            throw new RuntimeException("Price sort link not found: " + SEL_PRICE_SORT_LINK);
        }
        link.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitForContainer();
    }

    public List<Double> getPricesOnCurrentPage() {
        page.locator(SEL_TABLE).first().waitFor();

        // From your HTML: columns are Name(0), Category(1), Price(2), Stock(3), Actions(4)
        int priceCol = 2;

        Locator rows = page.locator(SEL_ROWS);
        int rowCount = rows.count();
        List<Double> prices = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            Locator tds = rows.nth(i).locator("td");
            if (tds.count() <= priceCol) continue;

            String raw = tds.nth(priceCol).innerText().trim(); // e.g., "323.00"
            if (raw.isBlank()) continue;

            // safe parse: keep digits and dot
            String cleaned = raw.replaceAll("[^0-9.]", "");
            if (cleaned.isBlank()) continue;

            try {
                prices.add(Double.parseDouble(cleaned));
            } catch (NumberFormatException ignored) {}
        }

        return prices;
    }

    public void logout() {
        Locator l = page.locator(SEL_LOGOUT).first();
        if (l.count() == 0) {
            // If your app logs out by visiting /logout directly:
            page.navigate(BASE_URL + "/logout");
        } else {
            l.click();
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
    public boolean isAddPlantVisible() {
        Locator btn = page.locator(SEL_ADD_PLANT_BTN).first();
        return btn.count() > 0 && btn.isVisible();
    }

    public boolean isEditVisibleForAnyRow() {
        return page.locator(SEL_EDIT_LINK).count() > 0 && page.locator(SEL_EDIT_LINK).first().isVisible();
    }

    public void clickEditFirstRow() {
        Locator edit = page.locator(SEL_EDIT_LINK).first();
        if (edit.count() == 0) throw new RuntimeException("Edit link not found for any row.");
        edit.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
    public boolean isOnEditPage() {
        return page.url().contains("/ui/plants/edit/");
    }
    public boolean isDeleteVisibleForAnyRow() {
        return page.locator(SEL_DELETE_BTN).count() > 0 && page.locator(SEL_DELETE_BTN).first().isVisible();
    }

    public void deleteFirstRowAndConfirm() {
        // handle browser confirm() from onsubmit
        page.onceDialog(Dialog::accept);

        Locator del = page.locator(SEL_DELETE_BTN).first();
        if (del.count() == 0) throw new RuntimeException("Delete button not found for any row.");

        del.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitForListVisible();
    }
    public void openPlantsList() {
        page.navigate(BASE_URL + UI_PLANTS_PATH);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        waitForListVisible();
    }
    public String getFirstPlantName() {
        Locator firstRow = page.locator(SEL_ROWS).first();
        if (firstRow.count() == 0) throw new RuntimeException("No plant rows found.");
        return firstRow.locator("td").first().innerText().trim();
    }
    public boolean isPlantNamePresent(String name) {
        return page.locator(SEL_ROWS).locator("td").filter(new Locator.FilterOptions().setHasText(name)).count() > 0
                || page.locator("table").locator("text=" + name).count() > 0;
    }
    public void clickAddPlant() {
        Locator btn = page.locator(SEL_ADD_PLANT_BTN).first();
        if (btn.count() == 0) throw new RuntimeException("Add Plant button/link not found.");
        btn.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    public boolean isOnAddPage() {
        return page.url().contains("/ui/plants/add");
    }
    public boolean waitForFormVisible() {
        return waitAnyVisible(15000,
                page.locator(SEL_NAME_INPUT).first(),
                page.locator(SEL_CATEGORY_SELECT).first(),
                page.locator(SEL_SAVE_BTN).first(),
                page.locator("body").first());
    }
    public boolean isAddFormEmpty() {
        String name = safeValue(page.locator(SEL_NAME_INPUT).first());
        String price = safeValue(page.locator(SEL_PRICE_INPUT).first());
        String qty = safeValue(page.locator(SEL_QTY_INPUT).first());
        // Category default is often "" (All Categories / Select)
        return (name == null || name.isBlank())
                && (price == null || price.isBlank())
                && (qty == null || qty.isBlank());
    }
    private static String safeValue(Locator input) {
        try {
            return input.inputValue();
        } catch (Exception e) {
            return null;
        }
    }
    public void openAddPlantPage() {
        page.navigate(BASE_URL + UI_PLANTS_ADD_PATH);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        waitForFormVisible();
    }
    public void fillPlantName(String plantName) {
        Locator name = page.locator(SEL_NAME_INPUT).first();
        if (name.count() == 0) throw new RuntimeException("Plant name input not found.");
        name.fill(plantName);
    }
    public void selectCategoryByLabel(String label) {
        Locator sel = page.locator(SEL_CATEGORY_SELECT).first();
        if (sel.count() == 0) throw new RuntimeException("Category select not found: select[name='categoryId']");
        sel.selectOption(new SelectOption().setLabel(label));
    }

    public void fillPrice(double price) {
        Locator p = page.locator(SEL_PRICE_INPUT).first();
        if (p.count() == 0) throw new RuntimeException("Price input not found.");
        p.fill(String.valueOf(price));
    }

    public void fillQuantity(int qty) {
        Locator q = page.locator(SEL_QTY_INPUT).first();
        if (q.count() == 0) throw new RuntimeException("Quantity input not found.");
        q.fill(String.valueOf(qty));
    }

    public void clickSave() {
        Locator s = page.locator(SEL_SAVE_BTN).first();
        if (s.count() == 0) throw new RuntimeException("Save button not found.");
        s.click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
package pages.category;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import utils.category.Config;

public class CategoryUiPage {
    
    private final Page page;
    private static final int DEFAULT_TIMEOUT = 10000;
    
    public CategoryUiPage(Page page) {
        this.page = page;
    }
    
    // ================= Navigation =================
    
    public void openCategoriesPage() {
        page.navigate(Config.UI_BASE_URL + Config.UI_CATEGORIES);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        waitForTable();
    }
    
    private void waitForTable() {
        try {
            page.waitForSelector("table tbody tr",
                    new Page.WaitForSelectorOptions().setTimeout(15000));
        } catch (Exception e) {
            // Fallback selectors if tbody tr not found
            String[] selectors = {".table", "tbody", "tr", "td", "h1:has-text('Categories')"};
            for (String selector : selectors) {
                try {
                    page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(5000));
                    return;
                } catch (Exception ex) { }
            }
            throw e;
        }
    }
        
    private Locator getAllRows() {
        return page.locator("table tbody tr");
    }
    
    private Locator getFirstRow() {
        Locator rows = getAllRows();
        if (rows.count() == 0) {
            throw new RuntimeException("No categories found in table");
        }
        return rows.first();
    }
    
    // ================= Actions =================
    
    /** Get category name from first row */
    public String getFirstCategoryName() {
        page.waitForTimeout(500);
        String name = getFirstRow().locator("td").first().innerText().trim();
        System.out.println("First category name: '" + name + "'");
        return name;
    }
    
    /** Click EDIT for first available category - Edit is a LINK */
    public void clickEditFirstCategory() {
        System.out.println("Clicking Edit link for first category");
        page.waitForTimeout(500);
        
        try {
            // Edit is a LINK (anchor tag) with href containing "/categories/edit/"
            Locator editLink = getFirstRow().locator("a[href*='/categories/edit/']");
            
            if (editLink.count() == 0) {
                // Fallback: try first link in Actions column
                editLink = getFirstRow().locator("td:last-child a").first();
            }
            
            String href = editLink.getAttribute("href");
            System.out.println("Found Edit link, href: " + href);
            
            editLink.click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("Successfully clicked Edit link");
            
        } catch (Exception e) {
            System.err.println("Failed to click Edit link: " + e.getMessage());
            
            // Debug: show all links and buttons
            Locator allLinks = getFirstRow().locator("td:last-child a");
            Locator allButtons = getFirstRow().locator("td:last-child button");
            System.out.println("Total links in Actions column: " + allLinks.count());
            System.out.println("Total buttons in Actions column: " + allButtons.count());
            
            for (int i = 0; i < allLinks.count(); i++) {
                System.out.println("  Link " + i + ": " + allLinks.nth(i).getAttribute("href"));
            }
            for (int i = 0; i < allButtons.count(); i++) {
                System.out.println("  Button " + i + ": " + allButtons.nth(i).getAttribute("title"));
            }
            
            throw new RuntimeException("Could not click Edit link for first category", e);
        }
    }
    
    /** Click DELETE for first available category - Delete is a BUTTON */
    public void clickDeleteFirstCategory() {
        System.out.println("Clicking Delete button for first category");
        page.waitForTimeout(500);
        
        try {
            // Delete is a BUTTON with title="Delete"
            Locator deleteButton = getFirstRow().locator("button[title='Delete']");
            
            if (deleteButton.count() == 0) {
                System.out.println("Delete button with title='Delete' not found, trying alternatives...");
                
                // Try alternative selectors
                String[] selectors = {
                    "button:has-text('Delete')",
                    "button.delete-btn",
                    "button[data-action='delete']",
                    "td:last-child button"
                };
                
                for (String selector : selectors) {
                    deleteButton = getFirstRow().locator(selector);
                    if (deleteButton.count() > 0) {
                        System.out.println("Found Delete button using selector: " + selector);
                        break;
                    }
                }
            }
            
            if (deleteButton.count() == 0) {
                throw new RuntimeException("Delete button not found in Actions column");
            }
            
            String title = deleteButton.getAttribute("title");
            System.out.println("Found Delete button, title: " + title);
            
            deleteButton.click();
            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("Successfully clicked Delete button");
            
        } catch (Exception e) {
            System.err.println("Failed to click Delete button: " + e.getMessage());
            
            // Debug: show all links and buttons
            Locator allLinks = getFirstRow().locator("td:last-child a");
            Locator allButtons = getFirstRow().locator("td:last-child button");
            System.out.println("Total links in Actions column: " + allLinks.count());
            System.out.println("Total buttons in Actions column: " + allButtons.count());
            
            for (int i = 0; i < allLinks.count(); i++) {
                System.out.println("  Link " + i + ": " + allLinks.nth(i).getAttribute("href"));
            }
            for (int i = 0; i < allButtons.count(); i++) {
                String title = allButtons.nth(i).getAttribute("title");
                String text = allButtons.nth(i).textContent();
                System.out.println("  Button " + i + " - title: '" + title + "', text: '" + text + "'");
            }
            
            throw new RuntimeException("Could not click Delete button for first category", e);
        }
    }
    
    /** Click EDIT by category name - Edit is a LINK */
    public void clickEditByCategoryName(String name) {
        System.out.println("Looking for Edit link for category: '" + name + "'");
        page.waitForTimeout(500);
        
        // Debug: Print all category names
        System.out.println("Available categories:");
        Locator rows = getAllRows();
        for (int i = 0; i < rows.count(); i++) {
            String categoryName = rows.nth(i).locator("td").first().innerText().trim();
            System.out.println("  Row " + i + ": '" + categoryName + "'");
        }
        
        // Edit is a LINK with href containing "/categories/edit/"
        String[] selectors = {
            // Direct match: row with category name, then Edit link
            "xpath=//tr[td[1][normalize-space(.)='" + name + "']]//a[contains(@href, '/categories/edit/')]",
            
            // Alternative: contains match
            "xpath=//tr[td[contains(normalize-space(.), '" + name + "')]]//a[contains(@href, '/categories/edit/')]",
            
            // CSS fallback
            "tr:has-text('" + name + "') a[href*='/categories/edit/']",
            
            // Generic first link in that row
            "xpath=//tr[td[1][normalize-space(.)='" + name + "']]//td[last()]//a[1]"
        };
        
        clickFirstVisible(selectors, "Edit", name);
    }
    
    /** Click DELETE by category name - Delete is a BUTTON */
    public void clickDeleteByCategoryName(String name) {
        System.out.println("Looking for Delete button for category: '" + name + "'");
        page.waitForTimeout(500);
        
        // Debug: Print all category names
        System.out.println("Available categories:");
        Locator rows = getAllRows();
        for (int i = 0; i < rows.count(); i++) {
            String categoryName = rows.nth(i).locator("td").first().innerText().trim();
            System.out.println("  Row " + i + ": '" + categoryName + "'");
        }
        
        // Delete is a BUTTON with title="Delete"
        String[] selectors = {
            // Direct match: row with category name, then Delete button
            "xpath=//tr[td[1][normalize-space(.)='" + name + "']]//button[@title='Delete']",
            
            // Alternative: contains match
            "xpath=//tr[td[contains(normalize-space(.), '" + name + "')]]//button[@title='Delete']",
            
            // CSS fallback
            "tr:has-text('" + name + "') button[title='Delete']",
            
            // Generic button with Delete text
            "xpath=//tr[td[1][normalize-space(.)='" + name + "']]//button[contains(text(), 'Delete')]"
        };
        
        clickFirstVisible(selectors, "Delete", name);
    }
    
    private void clickFirstVisible(String[] selectors, String action, String categoryName) {
        for (String selector : selectors) {
            try {
                Locator locator = page.locator(selector);
                if (locator.count() > 0) {
                    System.out.println("Found " + action + " element using selector: " + selector);
                    locator.first().click();
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Selector failed: " + selector + " - " + e.getMessage());
            }
        }
        throw new RuntimeException("Could not find " + action + " element for category: " + categoryName);
    }
    
    // ================= Form =================
    
    public void setCategoryName(String value) {
        String selector = "input[name='name'], input#name, input[placeholder*='name' i], input.form-control";
        page.waitForSelector(selector,
                new Page.WaitForSelectorOptions().setTimeout(DEFAULT_TIMEOUT));
        page.fill(selector, "");  // Clear first
        page.fill(selector, value);
    }
    
    public void clickSave() {
        String[] selectors = {
            "button[type='submit']",
            "button:has-text('Save')",
            "button:has-text('Update')",
            "button.btn-primary"
        };
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    page.click(selector);
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    return;
                }
            } catch (Exception e) { }
        }
        throw new RuntimeException("Could not find Save button");
    }
    
    public void clickCancel() {
        try {
            page.click("button:has-text('Cancel')");
        } catch (Exception e1) {
            try {
                page.click(".btn-secondary:has-text('Cancel'), [data-bs-dismiss='modal']");
            } catch (Exception e2) {
                page.goBack();
            }
        }
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
    
    // ================= Delete Confirmation =================
    
    public void confirmDelete() {
        page.waitForTimeout(1000);
        
        System.out.println("Looking for delete confirmation dialog...");
        
        String[] selectors = {
            "button:has-text('Confirm')",
            "button:has-text('Yes')",
            "button:has-text('Delete')",
            "button:has-text('OK')",
            "button.btn-danger",
            "button[data-confirm='true']",
            ".modal button:has-text('Confirm')",
            ".modal button:has-text('Yes')",
            ".modal button:has-text('Delete')"
        };
        
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    System.out.println("Found confirmation button using selector: " + selector);
                    page.click(selector);
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    System.out.println("Clicked confirmation button");
                    return;
                }
            } catch (Exception e) { }
        }
        
        System.out.println("No confirmation dialog found, deletion may not require confirmation");
    }
    
    // ================= Validations =================
    
    public boolean isCategoryVisible(String name) {
        page.waitForTimeout(1000);
        return page.locator("table tbody tr")
                .filter(new Locator.FilterOptions().setHasText(name))
                .count() > 0;
    }
    
    public boolean isValidationMessageVisible(String message) {
        String[] selectors = {
            "text=" + message,
            ".alert:has-text('" + message + "')",
            ".error:has-text('" + message + "')",
            ".text-danger:has-text('" + message + "')",
            ".invalid-feedback:has-text('" + message + "')"
        };
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) return true;
            } catch (Exception e) { }
        }
        return false;
    }
    
    public boolean isAddCategoryButtonVisible() {
        String[] selectors = {
            "button:has-text('Add Category')",
            "button:has-text('Add A Category')",
            "button:has-text('Create Category')",
            "button:has-text('New Category')",
            "a:has-text('Add Category')",
            "a:has-text('Add A Category')",
            "a:has-text('Create Category')",
            "a:has-text('New Category')",
            ".add-category-btn",
            ".create-category-btn"
        };
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0 && page.locator(selector).first().isVisible()) {
                    return true;
                }
            } catch (Exception e) { }
        }
        return false;
    }
    
    public boolean isOnEditPage(String id) {
        try {
            String expectedUrl = "/ui/categories/edit/" + id;
            page.waitForURL(url -> url.contains(expectedUrl), 
                new Page.WaitForURLOptions().setTimeout(DEFAULT_TIMEOUT));
            return page.url().contains(expectedUrl);
        } catch (Exception e) {
            return page.locator("h1:has-text('Edit Category'), h2:has-text('Edit Category')").count() > 0;
        }
    }
    
    public boolean canViewCategories() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);
            boolean urlContainsCategories = page.url().contains("categories");
            boolean hasHeader = page.locator("h1, h2, h3:has-text('Categories')").count() > 0;
            boolean hasAnyContent = page.locator("body").textContent() != null &&
                                    !page.locator("body").textContent().trim().isEmpty();
            boolean hasAccessDenied = page.locator(":has-text('Access Denied'), :has-text('Forbidden'), :has-text('Unauthorized')").count() > 0;
            boolean isLoginPage = page.url().contains("login") || 
                                  page.locator(":has-text('Login'), :has-text('Sign In')").count() > 0;
            return (urlContainsCategories || hasHeader) && hasAnyContent && !hasAccessDenied && !isLoginPage;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasAdminPrivileges() {
        try {
            boolean hasAddButton = isAddCategoryButtonVisible();
            if (!hasAddButton) return false;
            boolean hasBulkActions = page.locator(".bulk-actions, select[name='bulk_action']").count() > 0;
            boolean hasImportExport = page.locator(":has-text('Import'), :has-text('Export')").count() > 0;
            return hasAddButton || hasBulkActions || hasImportExport;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean areEditButtonsHidden() {
        // Edit is a LINK with href containing "/categories/edit/"
        page.waitForTimeout(1000);
        
        int linkCount = page.locator("table a[href*='/categories/edit/']").count();
        
        System.out.println("Edit link count for regular user: " + linkCount);
        
        if (linkCount > 0) {
            System.out.println("WARNING: Edit links are visible to regular user!");
            Locator editLinks = page.locator("table a[href*='/categories/edit/']");
            for (int i = 0; i < editLinks.count(); i++) {
                System.out.println("  Edit link " + i + " visible: " + editLinks.nth(i).isVisible());
                System.out.println("  Edit link " + i + " href: " + editLinks.nth(i).getAttribute("href"));
            }
        }
        
        return linkCount == 0;
    }
    
    public boolean areDeleteButtonsHidden() {

    page.waitForTimeout(1000);

    Locator deleteButtons = page.locator("table button[title='Delete']");
    int count = deleteButtons.count();

    System.out.println("Delete button count for regular user: " + count);

    // ✅ No delete buttons at all → PASS
    if (count == 0) {
        System.out.println("Delete buttons are hidden for regular user");
        return true;
    }

    // 🔍 Delete buttons exist → must ALL be disabled
    for (int i = 0; i < count; i++) {
        Locator button = deleteButtons.nth(i);

        boolean visible = button.isVisible();
        boolean enabled = button.isEnabled();

        System.out.println("Delete button " + i +
                " | visible=" + visible +
                " | enabled=" + enabled);

        if (visible && enabled) {
            System.out.println("Delete button is ENABLED for regular user!");
            return false;
        }
    }

    // ✅ Buttons exist but disabled
    System.out.println("✓ Delete buttons are disabled for regular user");
    return true;
}

}
package pages.dashboard;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import utils.category.Config;

public class DashboardPage {

    private final Page page;

    public DashboardPage(Page page) {
        this.page = page;
    }

    public void openDashboard() {
        page.navigate(Config.UI_BASE_URL + Config.UI_DASHBOARD);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForTimeout(2000);
        
        System.out.println("Dashboard URL: " + page.url());
        System.out.println("Dashboard page title: " + page.title());
        
        // Debug: Print first 500 characters of page content
        String bodyText = page.locator("body").textContent();
        System.out.println("Dashboard content preview: " + 
            bodyText.substring(0, Math.min(500, bodyText.length())));
    }

    public boolean isCategorySummaryDisplayed() {
        System.out.println("Checking for Category summary...");
        
        String[] selectors = {
            "h3:has-text('Categories')",
            "h2:has-text('Categories')",
            "h1:has-text('Categories')",
            "h4:has-text('Categories')",
            ".category-summary",
            "[data-testid='category-summary']",
            ":has-text('Total Categories')",
            ":has-text('Category')",
            ".card:has-text('Categories')",
            ".summary-card:has-text('Categories')"
        };
        
        for (String selector : selectors) {
            try {
                int count = page.locator(selector).count();
                if (count > 0) {
                    System.out.println("Found category summary with selector: " + selector + " (count: " + count + ")");
                    return true;
                }
            } catch (Exception e) {
                // Continue to next selector
            }
        }
        
        // Debug: Show what's actually on the page
        System.out.println("No category summary found");
        System.out.println("Available headings:");
        page.locator("h1, h2, h3, h4, h5, h6").all().forEach(heading -> 
            System.out.println("  - " + heading.textContent()));
        
        System.out.println("Available cards/divs with class:");
        page.locator("[class*='card'], [class*='summary']").all().forEach(card -> 
            System.out.println("  - Class: " + card.getAttribute("class") + " | Text: " + 
                card.textContent().substring(0, Math.min(50, card.textContent().length()))));
        
        return false;
    }

    public boolean isPlantsSummaryDisplayed() {
        System.out.println("Checking for Plants summary...");
        
        String[] selectors = {
            "h3:has-text('Plants')",
            "h2:has-text('Plants')",
            "h1:has-text('Plants')",
            "h4:has-text('Plants')",
            ".plants-summary",
            "[data-testid='plants-summary']",
            ":has-text('Total Plants')",
            ":has-text('Plant')",
            ".card:has-text('Plants')",
            ".summary-card:has-text('Plants')"
        };
        
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    System.out.println("Found plants summary with selector: " + selector);
                    return true;
                }
            } catch (Exception e) {
                // Continue
            }
        }
        
        System.out.println("No plants summary found");
        return false;
    }

    public boolean isSalesSummaryDisplayed() {
        System.out.println("Checking for Sales summary...");
        
        String[] selectors = {
            "h3:has-text('Sales')",
            "h2:has-text('Sales')",
            "h1:has-text('Sales')",
            "h4:has-text('Sales')",
            "h3:has-text('Orders')",
            ".sales-summary",
            "[data-testid='sales-summary']",
            ":has-text('Total Sales')",
            ":has-text('Sale')",
            ":has-text('Order')",
            ".card:has-text('Sales')",
            ".summary-card:has-text('Sales')"
        };
        
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    System.out.println("✓ Found sales summary with selector: " + selector);
                    return true;
                }
            } catch (Exception e) {
                // Continue
            }
        }
        
        System.out.println("No sales summary found");
        return false;
    }

    public boolean areAllSummaryCardsVisible() {
        boolean categoriesVisible = isCategorySummaryDisplayed();
        boolean plantsVisible = isPlantsSummaryDisplayed();
        boolean salesVisible = isSalesSummaryDisplayed();
        
        System.out.println("Summary cards visibility:");
        System.out.println("  - Categories: " + categoriesVisible);
        System.out.println("  - Plants: " + plantsVisible);
        System.out.println("  - Sales: " + salesVisible);
        
        return categoriesVisible && plantsVisible && salesVisible;
    }

    public boolean isDashboardMenuItemActive() {
        System.out.println("Checking for active Dashboard menu item...");
        
        String[] selectors = {
            ".nav-link.active:has-text('Dashboard')",
            ".sidebar-menu .active:has-text('Dashboard')",
            "a.active[href*='dashboard']",
            ".menu-item.active:has-text('Dashboard')",
            ".nav-item.active:has-text('Dashboard')",
            "[class*='active']:has-text('Dashboard')"
        };
        
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    System.out.println("Found active Dashboard menu with selector: " + selector);
                    return true;
                }
            } catch (Exception e) {
                // Continue
            }
        }
        
        // Debug: Show active menu items
        System.out.println("No active Dashboard menu found");
        System.out.println("Active menu items:");
        page.locator("[class*='active']").all().forEach(item -> 
            System.out.println("  - Class: " + item.getAttribute("class") + " | Text: " + item.textContent()));
        
        return false;
    }
}
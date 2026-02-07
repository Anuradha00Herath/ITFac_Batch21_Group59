package pages;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;


public class CategoryPage {

    private Page page;
    private String lastGeneratedCategoryName; 

    // Locators
    private static final String CATEGORIES_TABLE = "table";
    private static final String CATEGORY_SEARCH_BAR = "input[name='name'], input#search";
    private static final String CATEGORY_PARENTS_DROPDOWN = "select[name='parentId'], select#parentId";
    private static final String CATEGORY_DROPDOWN_OPTION = "option:checked";
    private static final String CATEGORY_SEARCH_BUTTON = "button:has-text('Search')";


        // Locator for table headers
    private static final String CAT_ID_HEADER = "a:has-text('ID')";
    private static final String CAT_NAME_HEADER = "//a[normalize-space(text())='Name']";
    private static final String CAT_PARENT_HEADER = "a:has-text('Parent')";

        //Admin locators
    private static final String ADD_CATEGORY_BUTTON = "a[href='/ui/categories/add']";
    private static final String CATEGORY_NAME_LABEL = "label:has-text('Category Name')";
    private static final String CATEGORY_NAME_INPUT = "input[name='name']";
    private static final String PARENT_CATEGORY_LABEL = "label:has-text('Parent Category')";
    private static final String PARENT_CATEGORY_DROPDOWN = "select[name='parentId']";
    private static final String SAVE_BUTTON = "button:has-text('Save')";
    private static final String CANCEL_BUTTON = "a.btn.btn-secondary:has-text('Cancel')";
    private static final String ERROR_MESSAGE = ".invalid-feedback";

    // ID column header
    private static final String ID_COLUMN_HEADER = "a[href*='sortField=id']";

    // Table ID column cells (adjust selector if needed)
    private static final String ID_COLUMN_CELLS = "table tbody tr td:first-child";

    // Table Name column cells (adjust selector if needed)
    private static final String NAME_COLUMN_CELLS = "table tbody tr td:nth-child(2)";

     // Table Name column cells (adjust selector if needed)
    private static final String PARENT_COLUMN_CELLS = "table tbody tr td:nth-child(3)";

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    public CategoryPage(Page page) {
        this.page = page;
        
    }

    //sort by ID
    public void sortByIdColumn() {
        page.locator(CAT_ID_HEADER).click();
    }

    //sort by Name
    public void sortByNameColumn() {
        page.locator(CAT_NAME_HEADER).click();
    }

    //sort by Parent
    public void sortByParentColumn() {
        page.locator(CAT_PARENT_HEADER).click();
    }

    public List<Integer> getCategoryIds() {
        List<String> idsText = page.locator(ID_COLUMN_CELLS).allTextContents();

        return idsText.stream()
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public boolean isAscending(List<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) > list.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean isDescending(List<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) < list.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public List<String> getNameColumnTexts() {
        return page.locator(NAME_COLUMN_CELLS)
            .allTextContents()
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
    }

    public List<String> getParentColumnTexts() {
        return page.locator(PARENT_COLUMN_CELLS)
            .allTextContents()
            .stream()
            .map(String::trim)
            .collect(Collectors.toList());
    }

    public boolean isAscendingText(List<String> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).compareToIgnoreCase(list.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isDescendingText(List<String> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).compareToIgnoreCase(list.get(i + 1)) < 0) {
                return false;
            }
        }
        return true;
    }

    public String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }


    public void verifyCategoryTableIsAvailable() {
        assertThat(page.locator(CATEGORIES_TABLE).first()).isVisible();
    }

    public void verifyCategoryTableColumns(String col1, String col2, String col3) {
        String tableText = page.locator(CATEGORIES_TABLE).textContent();
        if (!tableText.contains(col1) || !tableText.contains(col2) || !tableText.contains(col3)) {
            throw new RuntimeException("Categories table columns missing");
        }
    }

    public void verifyCategorySearchBarAvailable() {
        assertThat(page.locator(CATEGORY_SEARCH_BAR).first()).isVisible();
    }

    public void verifyCategoryParentsDropdownAvailable(String defaultValue) {

        Locator dropdown = page.locator(CATEGORY_PARENTS_DROPDOWN).first();
        assertThat(dropdown).isVisible();

        String selectedText = dropdown.locator(CATEGORY_DROPDOWN_OPTION).textContent().trim();

        if (!selectedText.equals(defaultValue)) {
            throw new RuntimeException("Parents drop-down default value is not " + defaultValue + ". Found: " + selectedText);
        }
    }

    public void verifyCategorySearchButtonAvailable() {
        assertThat(page.locator(CATEGORY_SEARCH_BUTTON).first()).isVisible();
    }

    public void searchWithValidCategory(String categoryName) {
        Locator searchBar = page.locator(CATEGORY_SEARCH_BAR).first();
        assertThat(searchBar).isVisible();
        searchBar.fill(categoryName);
    }

    public void clickOnSearchButton() {
        Locator searchButton = page.locator(CATEGORY_SEARCH_BUTTON).first();
        assertThat(searchButton).isVisible();
        searchButton.click();
    }

    public void verifySearchWithValidCategoryName(String expectedCategory) {
        Locator table = page.locator(CATEGORIES_TABLE).first();
        assertThat(table).isVisible();

        String tableText = table.textContent();
        if (!tableText.contains(expectedCategory)) {
            throw new RuntimeException("Search results do not contain: " + expectedCategory);
        }
    }

    public void selectParentCategory(String parentCategory){
        Locator dropdown = page.locator(CATEGORY_PARENTS_DROPDOWN).first();
        assertThat(dropdown).isVisible(); // Ensure the drop-down is visible
        dropdown.selectOption(parentCategory); // Select the parent category by value/text
    }

    public void verifyParentCategoryResults(String parentCategory){
        Locator rows = page.locator(CATEGORIES_TABLE + " tbody tr");
        int rowCount = rows.count();

        if (rowCount == 0) {
            throw new RuntimeException("No categories found after filtering by " + parentCategory);
        }

    // Check each row belongs to the selected parent category
        for (int i = 0; i < rowCount; i++) {
            String parentText = rows.nth(i).locator("td:nth-child(3)").textContent().trim(); // Assuming 3rd column is parent
            if (!parentText.equals(parentCategory)) {
                throw new RuntimeException("Row " + (i+1) + " does not match parent category: " + parentCategory);
            }
        }
    }

    public void sortByColumn(String column) {

        switch (column) {
            case "ID":
                page.click(CAT_ID_HEADER);
                break;

            case "Name":
                page.click(CAT_NAME_HEADER);
                break;

            case "Parent":
                page.click(CAT_PARENT_HEADER);
                break;

            default:
                throw new RuntimeException("Unknown column: " + column);
        }

        page.waitForTimeout(1000);
    }

    public void sortTableById(String column, int columnIndex) {
        Locator rows = page.locator("table tbody tr");

    // CLICK column FIRST to trigger ASC sorting
        sortByColumn(column);

        page.waitForTimeout(1000);

        int rowCount = rows.count();

        List<String> actualValues = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            actualValues.add(
                rows.nth(i).locator("td").nth(columnIndex).textContent().trim()
            );
        }

        // ---------- ASC CHECK ----------
        List<String> expectedAsc = new ArrayList<>(actualValues);

        if (column.equals("ID")) {
            expectedAsc.sort((a,b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
        } else {
            Collections.sort(expectedAsc);
        }

        if (!actualValues.equals(expectedAsc)) {
            throw new RuntimeException(column + " column NOT sorted in ASC order");
        }

        // // ---------- CLICK AGAIN FOR DESC ----------
        // sortByColumn(column);

        // page.waitForTimeout(1000);

        // List<String> actualDesc = new ArrayList<>();

        // for (int i = 0; i < rowCount; i++) {
        //     actualDesc.add(
        //         rows.nth(i).locator("td").nth(columnIndex).textContent().trim()
        //     );
        // }

        // List<String> expectedDesc = new ArrayList<>(expectedAsc);
        // Collections.reverse(expectedDesc);

        // if (!actualDesc.equals(expectedDesc)) {
        //     throw new RuntimeException(column + " column NOT sorted in DESC order");
        // }
    }

    public void searchInvalidText(String categoryName){   
        Locator searchBar = page.locator(CATEGORY_SEARCH_BAR).first();
        assertThat(searchBar).isVisible();       // ensure search bar is visible
        searchBar.fill(categoryName);    
    }

    public void verifyInvalidSearchMessage(String expectedMessage){
        Locator message = page.locator("text=" + expectedMessage).first();
        assertThat(message).isVisible();
    }

    public void clickAddCategoryButton(){
        Locator addBtn = page.locator(ADD_CATEGORY_BUTTON).first();
        assertThat(addBtn).isVisible();
        addBtn.click();
    }

    public void verifyCategoryFieldAvailability(){
        assertThat(page.locator(CATEGORY_NAME_LABEL)).isVisible();
        assertThat(page.locator(CATEGORY_NAME_INPUT)).isVisible();
    }

    public void verifyParentCategoryFieldAvailability(){
        assertThat(page.locator(PARENT_CATEGORY_LABEL)).isVisible();
        assertThat(page.locator(PARENT_CATEGORY_DROPDOWN)).isVisible();
    }

    public void verifyButtonsAvailability(){
        assertThat(page.locator(SAVE_BUTTON)).isVisible();
        assertThat(page.locator(CANCEL_BUTTON)).isVisible();
    }

    public String generateAndEnterRandomCategoryName() {
        String randomName = generateRandomString(8);
        enterValidCategoryName(randomName); // your existing method to input & save
        lastGeneratedCategoryName = randomName; // save it
        return randomName;
    }

    public String getLastGeneratedCategoryName() {
        return lastGeneratedCategoryName;
    }

    public void enterValidCategoryName(String categoryName){
        Locator nameInput = page.locator(CATEGORY_NAME_INPUT);
        assertThat(nameInput).isVisible();
        nameInput.fill(categoryName);
    }

    public void saveAddedCategory(){
        Locator saveBtn = page.locator(SAVE_BUTTON);
        assertThat(saveBtn).isVisible();
        saveBtn.click();
    }

    public void verifySuccessMessage(String expectedMessage){
        Locator message = page.locator("text=" + expectedMessage);
        assertThat(message).isVisible();
    }

    public void searchAddedCategory(String categoryName){
        Locator table = page.locator(CATEGORIES_TABLE);
        assertThat(table).isVisible();

        String tableText = table.textContent();

        if (!tableText.contains(categoryName)) {
         throw new RuntimeException("Category not found in table: " + categoryName);
        }
    }

    public void selectParentCategoryWithValue(String parentCategory){
        page.locator(CATEGORY_PARENTS_DROPDOWN).selectOption(new SelectOption().setLabel(parentCategory));
    }

    public void verifyValueAvailableInParentsDropdown(String parentCategory) {
    // Get all option elements in the dropdown
        List<String> options = page.locator(CATEGORY_PARENTS_DROPDOWN + " option")
                               .allTextContents();

       assertTrue(options.contains(parentCategory),
        "Parent category '" + parentCategory + "' is NOT available in the dropdown. Available options: " + options);
    }

    public void checkMaxCharacters(String errorMessage){
        assertThat(page.locator(ERROR_MESSAGE)).hasText(errorMessage);
    }

    public void clickCancelButton(){
        Locator cancelBtn = page.locator(CANCEL_BUTTON);
        assertThat(cancelBtn).isVisible();
        cancelBtn.click();

    }



   
}

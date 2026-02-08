Feature: UI Test Scenarios for view categories page

@ui @viewCategories
  Scenario: Normal user navigation to category page
    Given I am logged in as "user"
    When I open the Categories page
    Then Categories page should load
    And I should see the categories table with columns "ID", "Name", "Parent"
    And I should see a search bar
    And I should see the Parents drop-down with default value "All Parents"
    And I should see a Search button next to drop-down

@ui @searchCategories
  Scenario: Normal user searches a category
    Given I am logged in as "user"
    When I open the Categories page
    And I input a valid category name in the search bar "five"
    And I click the search button
    Then I should see search results containing "five"

@ui @filterCategories
  Scenario: Normal user filters categories by parent category
    Given I am logged in as "user"
    When I open the Categories page
    And I select the parent category "three" from the drop-down
    And I click the search button
    Then I should see search results filtered by parent category "three"

@ui @sortCategorieByID
  Scenario: Normal user sorts category table by ID column
    Given I am logged in as "user"
    And I open the Categories page
    When I sort the category table by "ID"
    Then the table should be sortable in ascending and descending order

@ui @sortCategorieByParent
  Scenario: Normal user sorts category table by Parent column
    Given I am logged in as "user"
    And I open the Categories page
    When I sort the category table by "Parent"
    Then the table parent column should be in alphabetical order order

@ui @searchInvalidCategories
  Scenario: Normal user searches a non-existing category
    Given I am logged in as "user"
    When I open the Categories page
    And I input a non existing category name in the search bar "tea"
    And I click the search button
    Then I should see "No category found" error message

@ui @adminCreateCategory
Scenario: Admin user can navigate to create category page
  Given I am logged in as "admin"
  When I open the Categories page
  And I click the Add a category button
  Then I should be navigated to the Add Category page
  And The URL should contain "/ui/categories/add"
  And I should see category name label and input field
  And I should see parent category label drop-down
  And I should see Save and Cancel buttons

@ui @adminCreateMainCat
Scenario: Admin user can create category without parent category
  Given I am logged in as "admin"
  And I open the Categories page
  And I click the Add a category button
  When I enter a valid category name "<RANDOM>"
  And I keep parent category empty
  And I click the Save button
  Then I should see "Category created successfully" success message
  And The created category should appear in the category table

@ui @adminCreateSubCategory
Scenario: Admin user can create category with a parent category
  Given I am logged in as "admin"
  And I open the Categories page
  And I click the Add a category button
  And A parent category "<RANDOMPARENT>" added
  And I select the exsting parent category
  When I enter a valid sub category name "<RANDOM>"  
  And I click the Save button
  Then I should see "Category created successfully" success message

@ui @adminCategoryFieldValidation
Scenario: Admin input more than 10 characters as the category name
  Given I am logged in as "admin"
  And I open the Categories page
  And I click the Add a category button
  When I enter a sub category name with more than 10 characters "MORETHAN10CHARCTERS"  
  And I click the Save button
  Then I should see "Category name must be between 3 and 10 characters" inline error message

@ui @adminCancelChanges
Scenario: Admin user can cancel add category changes
  Given I am logged in as "admin"
  And I open the Categories page
  And I click the Add a category button
  And A parent category "<RANDOMPARENT>" added
  And I select the exsting parent category
  And I enter a valid sub category name "<RANDOM>" 
  When I click on the cancel button  
  Then Categories page should load 



  






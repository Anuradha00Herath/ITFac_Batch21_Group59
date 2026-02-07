Feature: Category Management UI - Admin

  @admin @ui
  Scenario: Navigate to Edit Category page
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for first category
    Then Edit page is opened

  @admin @ui
  Scenario: Update category name
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for any category
    And Change Category Name to "lemon"
    And Click Save button
    Then Category name is updated to "lemon" in the list

  @admin @ui
  Scenario: Validation error on empty name
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for any category
    And Change Category Name to ""
    And Click Save button
    Then Validation error message "Category name is required" is displayed

  @admin @ui
  Scenario: Cancel edit
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for any category
    And Change Category Name to "Temp"
    And Click Cancel button
    Then Category name remains unchanged in the list

@admin @ui
Scenario: Delete category
  Given User is logged in as Admin
  When Navigate to Categories page
  And Click Delete for first category
  And Confirm delete in confirmation dialog
  Then Category is removed from the list

Feature: Category Management UI - Admin

  @admin01 @ui
  Scenario: Navigate to Edit Category page
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for first category
    Then Edit page is opened

  @admin02 @ui
  Scenario: Update category name
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for any category
    And Change Category Name
    And Click Save button
    Then Category name is updated in the list

  @admin03 @ui
  Scenario: Validation error on empty name
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for any category
    And Change Category Name to ""
    And Click Save button
    Then Validation error message "Category name is required" is displayed

  @admin04 @ui
  Scenario: Cancel edit
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for any category
    And Change Category Name
    And Click Cancel button
    Then Category name remains unchanged in the list

  @admin05 @ui
  Scenario: Delete category
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Delete for first category
    And Confirm delete in confirmation dialog
    Then Category is removed from the list

   
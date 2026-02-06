Feature: Category Management UI - Admin

  Scenario: TC_CAT_UI_ADM_001 - Navigate to Edit Category page
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for category "yellow"
    Then Edit Category page is displayed for ID "2"

  Scenario: TC_CAT_UI_ADM_002 - Update category name
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for category "yellow"
    And Change Category Name to "lemon"
    And Click Save button
    Then Category name is updated to "lemon" in the list

  Scenario: TC_CAT_UI_ADM_003 - Validation error on empty name
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for category "yellow"
    And Change Category Name to ""
    And Click Save button
    Then Validation error message "Category name is required" is displayed

  Scenario: TC_CAT_UI_ADM_004 - Cancel edit
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Edit for category "yellow"
    And Change Category Name to "orange"
    And Click Cancel button
    Then Category name is updated to "yellow" in the list

  Scenario: TC_CAT_UI_ADM_005 - Delete sub-category
    Given User is logged in as Admin
    When Navigate to Categories page
    And Click Delete for category "yellow"
    Then Category "yellow" is removed from the list

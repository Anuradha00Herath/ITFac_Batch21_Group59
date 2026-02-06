Feature: Category Management UI - User

  Scenario: TC_CAT_UI_USR_001 - Verify "Add Category" button is NOT visible for regular User
    Given User is logged in as regular User
    When Navigate to Categories page
    Then "Add Category" button is NOT visible

      Scenario: TC_CAT_UI_USR_002 - Verify Edit button is hidden/disabled in Actions column for User
    Given User is logged in as regular User
    And At least one category exists
    When Navigate to Categories page
    Then Edit button is hidden or disabled in Actions column

  Scenario: TC_CAT_UI_USR_003 - Verify Delete button is hidden/disabled in Actions column for User
    Given User is logged in as regular User
    And At least one category exists
    When Navigate to Categories page
    Then Delete button is hidden or disabled in Actions column

  Scenario: TC_DSH_UI_USR_001 - Verify Dashboard displays summary information for User
    Given User is logged in as regular User
    And Categories, Plants, and Sales data exist in system
    When Navigate to Dashboard page
    Then Category summary is displayed with correct count
    And Plants summary is displayed with correct count
    And Sales summary is displayed with correct count
    And All summary cards are visible and accurate

  Scenario: TC_DSH_UI_USR_002 - Verify Dashboard menu item is highlighted as active for User
    Given User is logged in as regular User
    And User is on Dashboard page
    When Navigate to Dashboard page
    Then Dashboard menu item is highlighted as active
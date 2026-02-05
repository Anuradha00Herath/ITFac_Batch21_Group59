Feature: Admin Form Data Integrity

  Scenario: TC_ADMIN_UI_SALES_002 - Verify plant dropdown and stock information
    Given the user is logged in as "Admin"
    When the user navigates directly to the Sell Plant page
    And the user opens the plant dropdown
    Then the plant dropdown should show available plants
    And out-of-stock plants should be hidden or disabled in the dropdown

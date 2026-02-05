Feature: Sales Empty State

  Scenario: TC_USER_UI_SALES_004 - Verify message when no sales exist
    Given the admin has cleared all sales records via API
    And the user is logged in as "User"
    When the user navigates to the sales list page
    Then a message "No sales found" should be displayed
    And the sales table should not be visible
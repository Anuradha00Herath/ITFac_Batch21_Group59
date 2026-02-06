Feature: Admin Sales Form Validation

  Scenario: TC_ADMIN_UI_SALES_003 - Quantity required validation
    Given the user is logged in as "Admin"
    When the user navigates directly to the Sell Plant page
    And the user selects a plant from the dropdown
    And the user leaves quantity empty
    And the user clicks Sell
    Then a quantity required validation message should be shown
    And the sale should not be created

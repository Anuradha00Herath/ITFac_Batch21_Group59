@ui
Feature: Admin Sale Reduces Stock and Redirects

  Scenario: TC_ADMIN_UI_SALES_004 - Successful sale reduces stock and redirects
    Given the user is logged in as "Admin"
    And an available plant with stock at least 2 is chosen
    When the user navigates directly to the Sell Plant page
    And the user selects the chosen plant in the dropdown
    And the user enters sell quantity 1
    And the user clicks Sell
    Then the user should be redirected to the sales list page
    And the chosen plant stock should be reduced by 1

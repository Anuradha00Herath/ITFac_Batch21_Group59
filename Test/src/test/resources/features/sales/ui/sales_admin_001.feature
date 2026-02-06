@ui
Feature: Admin Navigation

  Scenario: TC_ADMIN_UI_SALES_001 - Verify navigation to Add Sale page
    Given the user is logged in as "Admin"
    When the user navigates to the sales list page
    And the user clicks the "Sell Plant" button
    Then the system should redirect to the "Sell Plant" form page
    And the form title should be "QA Training App | Sell Plant"
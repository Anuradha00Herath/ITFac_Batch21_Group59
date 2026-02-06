@ui
Feature: Sales Access Control

  Scenario: TC_USER_UI_SALES_005 - Verify User cannot see Add Sale button
    Given the user is logged in as "User"
    When the user navigates to the sales list page
    Then the "Add Sale" button should not be visible

  Scenario: TC_USER_UI_SALES_006 - Verify Admin can see Add Sale button
    Given the user is logged in as "Admin"
    When the user navigates to the sales list page
    Then the "Add Sale" button should be visible
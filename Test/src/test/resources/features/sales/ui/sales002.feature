Feature: Sales Sorting

  Background:
    Given the user is logged in as "User"
    And at least 11 sale records exist with different sold dates

  Scenario: TC_USER_UI_SALES_002 - Verify default sorting by sold date desc
    When the user navigates to the sales list page
    Then the latest sold date should appear at the top of the table
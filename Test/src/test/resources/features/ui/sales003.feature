Feature: Sales Plant Name Sorting

  Background:
    Given the user is logged in as "User"
    And multiple sales exist with different plant names

  Scenario: TC_USER_UI_SALES_003 - Verify sorting by plant name
    When the user navigates to the sales list page
    And the user clicks the "Plant Name" column header
    Then the records should be sorted by plant name in ascending order
    When the user clicks the "Plant Name" column header again
    Then the records should be sorted by plant name in descending order
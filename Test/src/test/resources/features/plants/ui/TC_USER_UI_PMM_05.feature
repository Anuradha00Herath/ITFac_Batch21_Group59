@ui
Feature: Plant Management - Sorting by Price

  Background:
    Given the user is logged in as "User"
    And multiple plant records exist in the database

  Scenario: TC_USER_UI_PMM_05 - Sort plants by price ascending and descending
    When the user navigates to the plants list page
    And the user clicks the Price sort link
    Then the plant list should be sorted by price based on current sort direction
    When the user clicks the Price sort link
    Then the plant list should be sorted by price based on current sort direction
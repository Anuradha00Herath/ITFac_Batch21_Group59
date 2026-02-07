@ui
Feature: Plant Management - Sorting

  Background:
    Given the user is logged in as "User"
    And multiple plant records exist in the database

  Scenario: TC_USER_UI_PMM_04 - Sort plants by name ascending and descending
    When the user navigates to the plants list page
    And the user clicks the Name sort link
    Then the plant list should be sorted by name based on current sort direction
    When the user clicks the Name sort link
    Then the plant list should be sorted by name based on current sort direction
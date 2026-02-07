@ui
Feature: Plant Management

  Background:
    Given the user is logged in as "User"
    And more than 10 plant records exist in the database

  Scenario: TC_USER_UI_PMM_01 - View paginated plant list and navigate pages
    When the user navigates to the plants list page
    Then the plant list should be displayed successfully
    And pagination controls should be visible on the plants list
    When the user clicks the Next page on plants pagination controls
    Then navigating to the next page should update the plant list
    And different plants should be displayed on different pages
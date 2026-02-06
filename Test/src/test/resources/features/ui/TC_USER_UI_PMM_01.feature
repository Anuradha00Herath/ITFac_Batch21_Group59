@ui
Feature: Plant Management - View plants with pagination

  Scenario: TC_USER_UI_PMM_01 Verify that the user can view a paginated list of plants and navigate between pages

    Given I am logged in as "user"
    And more than 10 plant records exist

    When I open the Plants list page

    Then the plant list should be displayed
    And pagination controls should be visible

    When I click the Next page button

    Then the plant list should be updated
    And different plants should be displayed on the next page


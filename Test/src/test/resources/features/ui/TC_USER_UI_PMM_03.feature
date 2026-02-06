@ui
Feature: Plant Management – Filter plants based on category

  Scenario: TC_USER_UI_PMM_03 Verify that the user can filter plants based on category

    Given I am logged in as "user"
    And plants with multiple categories exist

    When I open the Plants list page
    And I select a category from the category filter

    Then only plants belonging to the selected category should be displayed

    When I clear the category filter

    Then all plants should be displayed
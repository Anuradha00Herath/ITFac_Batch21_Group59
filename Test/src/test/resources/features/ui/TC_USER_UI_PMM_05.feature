@ui
Feature: Plant Management – Sort the plant list by price
  Scenario: TC_USER_UI_PMM_05 Verify that the user can sort the plant list by price in both ascending and descending order

    Given I am logged in as "user"
    And multiple plant records with different prices exist

    When I open the Plants list page
    And I click the plant price sort button

    Then the plant list should be sorted by price in ascending order

    When I click the plant price sort button again

    Then the plant list should be sorted by price in descending order
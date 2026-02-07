Feature: Plant Search Functionality
  As a user of the QA Training Application
  I want to search for plants in the Plants section
  So that I can quickly find specific plants based on my criteria

  Background:
    Given I am on the Plants page
    And I can see the plant data table with at least 5 plants

  @search @smoke
  Scenario: Search plants by exact name
    When I enter "Plant 1" in the search bar
    And I click the "Search" button
    Then I should see only "Plant 1" in the results
    And the table should show exactly 1 row

  @search
  Scenario: Search plants by partial name
    When I enter "Plant X" in the search bar
    And I click the "Search" button
    Then I should see plants containing "Plant X" in the results
    And I should see "Plant X" in the results
    And I should see "Plant XY" in the results
    And the table should show at least 2 rows
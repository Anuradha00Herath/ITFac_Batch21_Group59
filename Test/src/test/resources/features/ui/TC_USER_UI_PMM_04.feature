@ui
Feature: Plant Management – Sort the plant list by name

Scenario: TC_USER_UI_PMM_04 Verify that the user can sort the plant list by name in both ascending and descending order

Given I am logged in as "user"
And multiple plant records exist

When I open the Plants list page
And I click the plant name sort button

Then the plant list should be sorted by name in descending order

When I click the plant name sort button again

Then the plant list should be sorted by name in ascending order




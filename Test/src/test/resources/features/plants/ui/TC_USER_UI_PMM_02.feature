@ui
Feature: Plant Management - Search

  Background:
    Given the user is logged in as "User"
    And at least one plant record exists in the database

  Scenario: TC_USER_UI_PMM_02 - Search plants by name
    When the user navigates to the plants list page
    And the user searches plants by name "Rose"
    Then only plants matching name "Rose" should be displayed
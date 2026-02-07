@ui
Feature: Plant Management - Filter

  Background:
    Given the user is logged in as "User"
    And at least one plant record exists in the database

  Scenario: TC_USER_UI_PMM_03 - Filter plants by category
    When the user navigates to the plants list page
    And the user filters plants by category "Bryophyta"
    Then only plants in category "Bryophyta" should be displayed
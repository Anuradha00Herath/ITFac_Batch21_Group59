@ui
Feature: Plant Management - Admin edit plant

  Scenario: TC_ADMIN_UI_PMM_02 Verify that the Admin can access and use the Edit Plant functionality

    Given I am logged in as "admin"
    And at least one plant record exists

    When I open the Plants list page
    And I click the Edit button for a plant

    Then I should be redirected to the Edit Plant page

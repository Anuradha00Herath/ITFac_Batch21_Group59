@ui
Feature: Plant Management - Edit Plant

  Scenario: TC_ADMIN_UI_PMM_02 - Admin can access Edit Plant page
    Given the user is logged in as "Admin"
    And at least one plant record exists in the database
    When the user navigates to the plants list page
    Then the Edit action should be visible for a plant row
    When the admin clicks Edit for the first plant
    Then the admin should be redirected to the Edit Plant page
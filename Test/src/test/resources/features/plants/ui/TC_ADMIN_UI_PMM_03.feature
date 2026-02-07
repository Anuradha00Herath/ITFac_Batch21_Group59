@ui
Feature: Plant Management - Delete Plant

  Scenario: TC_ADMIN_UI_PMM_03 - Admin can delete a plant with confirmation
    Given the user is logged in as "Admin"
    And at least one plant record exists in the database
    When the user navigates to the plants list page
    Then the Delete action should be visible for a plant row
    When the admin deletes the first plant and confirms
    Then the deleted plant should be removed from the plant list
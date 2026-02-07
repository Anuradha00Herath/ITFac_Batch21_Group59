@ui
Feature: Plant Management - Navigate to Add Plant

  Scenario: TC_ADMIN_UI_PMM_04 - Admin can open Add Plant page
    Given the user is logged in as "Admin"
    When the user navigates to the plants list page
    And the admin clicks the "Add Plant" in plant button
    Then the admin should be redirected to the Add Plant page
    And the Add Plant form should be displayed and empty
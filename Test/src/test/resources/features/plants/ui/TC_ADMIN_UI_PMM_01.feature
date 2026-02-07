@ui
Feature: Plant Management - Admin Access

  Scenario: TC_ADMIN_UI_PMM_01 - Add Plant button visible only for Admin
    Given the user is logged in as "Admin"
    When the user navigates to the plants list page
    Then the "Add Plant" in plant button should be visible

    When the user logs out
    And the user is logged in as "User"
    And the user navigates to the plants list page
    Then the "Add Plant" in plant button should not be visible
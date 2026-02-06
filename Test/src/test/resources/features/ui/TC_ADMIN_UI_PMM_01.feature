@ui
Feature: Plant Management - Admin Add Plant button visibility

  Scenario: TC_ADMIN_UI_PMM_01 Verify that the “Add Plant” button is visible only to Admin users

    Given I am logged in as "admin"
    And I open the Plants list page

    Then the Add Plant button should be visible

    When I log out
    And I am logged in as "user"
    And I open the Plants list page

    Then the Add Plant button should not be visible

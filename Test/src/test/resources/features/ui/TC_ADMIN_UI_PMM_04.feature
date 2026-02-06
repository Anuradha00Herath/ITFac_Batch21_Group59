@ui
Feature: Plant Management - Navigate to Add Plant page

  Scenario: TC_ADMIN_UI_PMM_04 Verify that the Admin can navigate to the Add Plant page

    Given I am logged in as "admin"

    When I open the Plants list page
    And I click the Add Plant button

    Then I should be redirected to the Add Plant page
    And the Add Plant form should be displayed with empty fields

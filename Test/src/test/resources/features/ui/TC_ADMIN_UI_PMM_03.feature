@ui
Feature: Plant Management - Admin delete plant

  Scenario: TC_ADMIN_UI_PMM_03 Verify that the Admin can delete a plant from the plant list

    Given I am logged in as "admin"
    And at least one plant record exists

    When I open the Plants list page
    And I click the Delete button for a plant
    And I confirm the deletion

    Then the plant should be removed from the plant list

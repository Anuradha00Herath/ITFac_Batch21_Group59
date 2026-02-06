@ui
Feature: Plant Management - Admin add plant

  Scenario: TC_ADMIN_UI_PMM_05 Verify that the Admin can successfully add a plant using valid input data

    Given I am logged in as "admin"
    And valid sub-categories exist

    When I open the Add Plant page
    And I enter a valid plant name
    And I select a valid sub-category
    And I enter a valid price greater than 0
    And I enter a valid quantity
    And I click the Save button

    Then a success message should be displayed
    And the new plant should appear in the plant list

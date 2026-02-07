@ui
Feature: Plant Management - Add Plant

  Scenario: TC_ADMIN_UI_PMM_05 - Admin can add a plant with valid inputs
    Given the user is logged in as "Admin"
    When the admin opens the Add Plant page
    And the admin enters a valid plant name
    And the admin selects a valid sub-category
    And the admin enters price greater than 0
    And the admin enters quantity greater than or equal to 0
    And the admin clicks Save on plant form
    Then the plant should be successfully added and shown in the plant list
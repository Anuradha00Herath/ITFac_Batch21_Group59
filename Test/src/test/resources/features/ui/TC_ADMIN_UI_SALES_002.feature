@ui
Feature: Sell Plant - Required field validations (Admin)

  Scenario: TC_ADMIN_UI_SALES_002 Mandatory field validation on Sell Plant form
    Given I am logged in as "admin"
    When I open the Sell Plant page
    And I submit the sell form with missing required fields
    Then I should see required field validation messages
    And the sale should not be created

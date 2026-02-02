@ui
Feature: Sell Plant - Quantity must be > 0 (Admin)

  Scenario: TC_ADMIN_UI_SALES_003 Quantity must be greater than 0
    Given I am logged in as "admin"
    When I open the Sell Plant page
    And I select plant "1"
    And I enter sell quantity 0
    And I confirm sale
    Then I should see validation error "Quantity must be greater than 0"
    And the sale should not be created

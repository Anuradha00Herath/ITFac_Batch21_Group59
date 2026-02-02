@ui
Feature: Sell Plant - Access (Admin)

  Scenario: TC_ADMIN_UI_SALES_001 Admin can access Sell Plant page
    Given I am logged in as "admin"
    When I open the Sell Plant page
    Then Sell Plant page should load

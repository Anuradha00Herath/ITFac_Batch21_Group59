@ui
Feature: Sales List - Empty state (User)

  Background:
    Given I am logged in as "user"

  Scenario: TC_USER_UI_SALES_004 No sales found message
    Given there are no sales records
    When I open the Sales list page
    Then I should see empty-state message "No sales found"

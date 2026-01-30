@ui
Feature: Sales List - Pagination (User)

  Background:
    Given I am logged in as "user"

  Scenario: TC_USER_UI_SALES_001 Sales list loads with pagination
    When I open the Sales list page
    Then I should see sales list container
    And I should see sales records or empty state
    And I should see pagination controls when records exceed one page

@ui
Feature: Sales List - Role-based Sell button (User)

  Background:
    Given I am logged in as "user"

  Scenario: TC_USER_UI_SALES_005 Sell Plant button hidden for normal user
    When I open the Sales list page
    Then I should not see the "Sell Plant" button

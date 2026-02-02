@ui
Feature: Sales List - Sorting by Plant Name (User)

  Background:
    Given I am logged in as "user"

  Scenario: TC_USER_UI_SALES_003 Sorting by Plant Name
    When I open the Sales list page
    And I sort by "Plant Name"
    Then sales should be sorted by "Plant Name" in "asc" order

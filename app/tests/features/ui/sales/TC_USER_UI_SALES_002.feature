@ui
Feature: Sales List - Default sorting (User)

  Background:
    Given I am logged in as "user"

  Scenario: TC_USER_UI_SALES_002 Default sorting by sold date desc
    When I open the Sales list page
    Then sales should be sorted by "Sold Date" in "desc" order

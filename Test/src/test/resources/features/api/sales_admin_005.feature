@api
Feature: Sales API
  Scenario: TC_ADMIN_API_SALES_005 Sell non-existing plant
    Given I am authenticated as "admin" via API
    When I sell plant 999999 with quantity 1 as admin
    Then the response status should be 404
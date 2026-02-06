@api
Feature: Sales API
  Scenario: TC_USER_API_SALES_001 Get all sales successfully
    Given I am authenticated as "user" via API
    When I get all sales as user
    Then the response should contain a list of sales
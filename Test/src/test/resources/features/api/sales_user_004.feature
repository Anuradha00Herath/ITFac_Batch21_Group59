@api
Feature: Sales API
  Scenario: TC_USER_API_SALES_004 Get sales pagination without parameters
    Given I am authenticated as "user" via API
    When I request sales pagination endpoint without parameters as user
    Then the response status should be 400
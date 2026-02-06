@api
Feature: Sales API
  Scenario: TC_USER_API_SALES_005 Get sale by id
    Given I am authenticated as "user" via API
    And a sale exists (capture a sale id) as user
    When I get the captured sale as user
    Then the response should contain sale data
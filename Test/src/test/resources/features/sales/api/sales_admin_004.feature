@api
Feature: Sales API
  Scenario: TC_ADMIN_API_SALES_004 Get sale by id
    Given I am authenticated as "admin" via API
    And a sale exists as admin
    When I get sale by id captured as admin
    Then the response should contain sale data
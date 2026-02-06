@api
Feature: Sales API

  Scenario: TC_ADMIN_API_SALES_002 Admin can delete sale
    Given I am authenticated as "admin" via API
    And a sale exists as admin
    When I delete the captured sale as admin
    Then the response status should be 204
@api
Feature: Sales API
  Scenario: TC_USER_API_SALES_003 Unauthorized sales access
    When I request sales endpoint without authentication
    Then the response status should be 401
@api
Feature: Sales API

  Scenario: TC_USER_API_SALES_002 Get sales with pagination
    Given I am authenticated as "user" via API
    When I get sales page as user with page 0 size 5 sortField "soldAt" sortDir "desc"
    Then the response should contain paginated sales
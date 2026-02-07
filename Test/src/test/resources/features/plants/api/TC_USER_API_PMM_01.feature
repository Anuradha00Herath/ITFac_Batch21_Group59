@api
Feature: Plant API

  Scenario: TC_USER_API_PMM_01 - Verify that the API returns all plants
    Given I am authenticated as "user" via API
    When I get all plants as user
    Then the response status should be 200
    And the response body should contain a list of plants
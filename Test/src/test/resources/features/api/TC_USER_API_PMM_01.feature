@api
Feature: Plant API

  Scenario: TC_USER_API_PMM_01 Verify that the API returns all plants
    Given I am authenticated as "user" via API
    And plant records exist in the system
    When I send a GET request to "/api/plants"
    Then the response status code should be 200
    And the response body should contain a list of plant records
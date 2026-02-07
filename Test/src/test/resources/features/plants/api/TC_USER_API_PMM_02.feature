@api
Feature: Plant API - Get plant by ID

  Scenario: TC_USER_API_PMM_02 - Verify user can get plant by ID
    Given I am authenticated as "user" via API
    And at least one plant exists (capture a plant id)
    When I get the plant by id as user
    Then the response status should be 200
    And the response should contain plant details
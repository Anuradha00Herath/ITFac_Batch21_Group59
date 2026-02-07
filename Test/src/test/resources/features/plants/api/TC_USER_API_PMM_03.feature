@api
Feature: Plant API - Get plant by invalid id

  Scenario: TC_USER_API_PMM_03 - Verify API returns 404 for non-existing plant id
    Given I am authenticated as "user" via API
    And at least one plant exists (capture a plant id)
    And I prepare a non-existing plant id
    When I get the plant by non-existing id as user
    Then the response status should be 404
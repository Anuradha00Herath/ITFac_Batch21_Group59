@api
Feature: Plant API - Get plants by category

  Scenario: TC_USER_API_PMM_05 - Get plants by category
    Given I am authenticated as "user" via API
    And a valid category id exists
    When I get plants by category id as user
    Then the response status should be 200
    And the response should contain only plants from that category
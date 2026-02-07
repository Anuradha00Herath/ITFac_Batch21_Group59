@api
Feature: Plant API - Invalid category id

  Scenario: TC_USER_API_PMM_04 - Invalid category id returns 404
    Given I am authenticated as "user" via API
    And I prepare a non-existing category id
    When I get plants by invalid category id as user
    Then  the response status should be 404
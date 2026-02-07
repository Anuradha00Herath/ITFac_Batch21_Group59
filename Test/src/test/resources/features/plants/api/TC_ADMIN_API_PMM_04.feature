@api
Feature: Admin Plant API - Update Invalid Plant

  Scenario: TC_ADMIN_API_PMM_04 - Update non-existing plant fails
    Given I am authenticated as "admin" via API
    And a non-existing plant id is prepared
    When the admin tries to update a non-existing plant
    Then the response status should be 404
    And the response should indicate plant not found
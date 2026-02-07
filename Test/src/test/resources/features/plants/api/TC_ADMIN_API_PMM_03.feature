@api
Feature: Admin Plant API - Update Plant

  Scenario: TC_ADMIN_API_PMM_03 - Admin updates plant successfully
    Given I am authenticated as "admin" via API
    And an existing plant id is available
    When the admin updates the plant with valid data
    Then the response status should be 200
    And the response should contain updated plant details
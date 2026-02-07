@api
Feature: Admin Plant API - Create Plant

  Scenario: TC_ADMIN_API_PMM_01 - Admin creates plant successfully
    Given I am authenticated as "admin" via API
    And a valid category id exists
    When the admin creates a plant with valid data
    Then the response status should be 201
    And the response should contain created plant with id
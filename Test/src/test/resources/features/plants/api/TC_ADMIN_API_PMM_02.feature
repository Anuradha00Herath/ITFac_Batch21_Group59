@api
Feature: Admin Plant API - Duplicate Plant

  Scenario: TC_ADMIN_API_PMM_02 - Duplicate plant creation fails
    Given I am authenticated as "admin" via API
    And a plant already exists in a category
    When the admin tries to create a duplicate plant
    Then the response status should be 400
    And the response should contain duplicate plant error
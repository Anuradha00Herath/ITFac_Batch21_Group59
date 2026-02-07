@api
Feature: Admin Plant API - Invalid Category

  Scenario: TC_ADMIN_API_PMM_05 - Create plant with invalid category fails
    Given I am authenticated as "admin" via API
    And a non-existing category id is prepared
    When the admin tries to create a plant with invalid category
    Then the response status should be 404
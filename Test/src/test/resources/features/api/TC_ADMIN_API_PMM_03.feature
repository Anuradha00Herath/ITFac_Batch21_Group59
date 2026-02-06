@api
Feature: Plant API

Scenario: TC_ADMIN_API_PMM_03 Verify that an Admin can update an existing plant successfully

Given I am authenticated as "admin" via API
And a plant record exists
When I update the existing plant with valid data
Then the response status code should be 200
And the response body should contain updated plant details
@api
Feature: Plant API

Scenario: TC_ADMIN_API_PMM_04 Verify that updating a plant with a non-existing ID fails with a 404 Not Found response

Given I am authenticated as "admin" via API
When I update a plant with invalid id
Then the response status code should be 404
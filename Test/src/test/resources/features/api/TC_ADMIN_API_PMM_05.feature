@api
Feature: Plant API
Scenario: TC_ADMIN_API_PMM_05 Verify that plant creation fails when an invalid category ID is provided

Given I am authenticated as "admin" via API
When I create a new plant under an invalid category
Then the response status code should be 404
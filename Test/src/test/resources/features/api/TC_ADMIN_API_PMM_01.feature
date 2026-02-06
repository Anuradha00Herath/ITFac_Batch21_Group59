@api
Feature: Plant API

Scenario: TC_ADMIN_API_PMM_01 Verify that an Admin can successfully create a plant using valid data

Given I am authenticated as "admin" via API
And a valid plant category exists
When I create a new plant under the category using valid data
Then the response status code should be 201
And the response body should contain the created plant with an id
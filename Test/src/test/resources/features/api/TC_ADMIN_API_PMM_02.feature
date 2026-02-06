@api
Feature: Plant API
Scenario: TC_ADMIN_API_PMM_02 Verify that creating a duplicate plant is not allowed

Given I am authenticated as "admin" via API
And a plant with the same name and category already exists
When I create the same plant again under the same category
Then the response status code should be 400

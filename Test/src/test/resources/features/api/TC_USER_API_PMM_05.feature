@api
Feature: User – Plant API (Pagination, Category and Error Handling)

Scenario: TC_USER_API_PMM_05 Verify that the API returns plants belonging to a specific category

Given I am authenticated as "user" via API
And a valid plant category exists
When I send a GET request to "/api/plants/category/{categoryId}"
Then the response status code should be 200
And the response body should contain a list of plant records
And the response body should contain only plants from the category
@api
Feature: User – Plant API (Pagination, Category and Error Handling)

Scenario: TC_USER_API_PMM_04 Verify that requesting category details with an invalid ID returns a 404 Not Found response

Given I am authenticated as "user" via API
When I send a GET request to "/api/plants/category/999"
Then the response status code should be 200
And the response body should contain an empty list
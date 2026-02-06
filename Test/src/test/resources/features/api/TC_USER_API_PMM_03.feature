@api
Feature: User – Plant API (Pagination, Category and Error Handling)

Scenario: TC_USER_API_PMM_03 Verify that return error message when plant is not found

Given I am authenticated as "user" via API
And no plant records exist in the system
When I send a GET request to "/api/plants/paged?page=0&size=1"
Then the response status code should be 404
@api
Feature: User – Plant API (Pagination)

  Scenario: TC_USER_API_PMM_02 Verify that the system returns a paginated list of plants for an authenticated user

Given I am authenticated as "user" via API
And more than 10 plant records exist in the system
When I send a GET request to "/api/plants/paged?page=0&size=1"
Then the response status code should be 200
And the response body should contain a list of plant records
And the response should contain pagination metadata

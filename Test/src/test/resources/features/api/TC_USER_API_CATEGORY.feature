Feature: Category Management API - User Operations

  @user @api
  Scenario: TC_CAT_API_USR_001 - GET all categories as User
    Given User is authenticated
    When I send GET request to "/api/categories"
    Then the response status code should be 200
    And validate response body contains category list
    And response contains array of categories with ID, name, and parent category fields

  @user @api
  Scenario: TC_CAT_API_USR_002 - GET specific category by valid ID as User
    Given User is authenticated
    And at least one category exists in system
    When I get a valid category ID from system
    And I send GET request to "/api/categories/{id}"
    Then the response status code should be 200
    And validate category details in response
    And response contains correct category with ID, name, and parent category information

  @user @api
  Scenario: TC_CAT_API_USR_003 - GET category with non-existent ID as User
    Given User is authenticated
    When I send GET request to "/api/categories/{id}" with invalid ID "99999"
    Then the response status code should be 404
    And response contains error message indicating category not found

  @user @api
  Scenario: TC_CAT_API_USR_004 - UPDATE category as User (Authorization Check)
    Given User is authenticated
    And at least one category exists in system
    When I get valid category ID
    And I prepare request body with name "Updated" and parentId "null"
    And I send PUT request to "/api/categories/{id}"
    Then the response status code should be 403
    And I verify original category data is unchanged

  @user @api
  Scenario: TC_DSH_API_USR_001 - GET category summary successfully as User
    Given User is authenticated
    When I send GET request to "/api/categories/summary"
    Then the response status code should be 200
    And response body contains accurate summary data
    And User has read access to summary
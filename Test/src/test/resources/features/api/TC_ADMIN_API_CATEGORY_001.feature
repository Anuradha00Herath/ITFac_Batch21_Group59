Feature: Category Management API - Admin Operations

  @admin @api
  Scenario: TC_CAT_API_ADM_001 - GET all categories as Admin
    Given I am authenticated as "admin"
    When I send GET request to "/api/categories"
    Then the response status code should be 200
    And validate response body contains category list
    And response contains array of categories with ID, name, and parent category fields

  @admin @api
  Scenario: TC_CAT_API_ADM_002 - GET specific category by valid ID as Admin
    Given I am authenticated as "admin"
    And at least one category exists in system
    When I get a valid category ID from system
    And I send GET request to "/api/categories/{id}"
    Then the response status code should be 200
    And validate category details in response
    And response contains correct category with ID, name, and parent category information

  @admin @api
  Scenario: TC_CAT_API_ADM_003 - UPDATE category name as Admin
    Given I am authenticated as "admin"
    And category with valid ID exists in system
    When I get valid category ID
    And I prepare request body with name "UpdatedCat" and parentId "null"
    And I send PUT request to "/api/categories/{id}"
    Then the response status code should be 200
    And response shows category name updated to "UpdatedCat"
    And validate updated category data

  @admin @api
  Scenario: TC_CAT_API_ADM_004 - UPDATE category without name field as Admin
    Given I am authenticated as "admin"
    And category with valid ID exists in system
    When I get valid category ID
    And I prepare request body with only parentId "null"
    And I send PUT request to "/api/categories/{id}"
    Then the response status code should be 400
    And validate error message
    And error message: "Category name is required"

  @admin @api
  Scenario: TC_CAT_API_ADM_005 - DELETE category without dependencies as Admin
    Given I am authenticated as "admin"
    And category with no associated plants exists
    When I get category ID that has no plant dependencies
    And I send DELETE request to "/api/categories/{id}"
    Then the response status code should be 200 or 204
    When I send GET request to verify category is deleted
    Then GET request returns 404 for deleted category ID
Feature: Category Management API – Admin Operations
  As an administrator
  I want to manage categories
  So that I can maintain the category structure in the system

  Background:
    Given I am authenticated as "admin"

  @admin @api @read
  Scenario: TC_CAT_API_ADM_001 - Get all categories as Admin
    When I send GET request to "/api/categories"
    Then the response status code should be 200
    And validate response body contains category list
    And response contains array of categories with ID, name, and parent category fields

  @admin @api @read
  Scenario: TC_CAT_API_ADM_002 - Get category by valid ID as Admin
    Given at least one category exists in system
    When I send GET request for a valid category ID
    Then the response status code should be 200
    And validate category details in response
    And response contains correct category with ID, name, and parent category information

  @admin @api @update
  Scenario: TC_CAT_API_ADM_003 - Update category name as Admin
    Given category with valid ID exists in system
    When I update the category name
    Then the response status code should be 200
    And response shows category name updated
    And validate updated category data

  @admin @api @negative
  Scenario: TC_CAT_API_ADM_004 - Update category without name as Admin
    Given category with valid ID exists in system
    When I attempt to update category without name
    Then the response status code should be 400
    And error message: "Category name is required"

  @admin @api @delete
  Scenario: TC_CAT_API_ADM_005 - Delete category without dependencies as Admin
    Given category with no associated plants exists
    When I delete the category
    Then the response status code should be 200 or 204
    And the category should no longer exist

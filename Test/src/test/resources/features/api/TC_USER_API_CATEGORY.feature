Feature: Category Management API – User Operations
  As a regular user
  I want to access category information
  So that I can view categories without modifying them

  @user @api @read
  Scenario: TC_CAT_API_USR_001 - Get all categories as User
    Given User is authenticated
    When User sends a GET request to "/api/categories"
    Then the response status code should be 200
    And the response body contains a list of categories
    And each category contains id, name, and parent category fields

  @user @api @read
  Scenario: TC_CAT_API_USR_002 - Get category by valid ID as User
    Given User is authenticated
    And at least one category exists in the system
    When User sends a GET request for an existing category
    Then the response status code should be 200
    And the response contains correct category details
    And the category includes id, name, and parent category information

  @user @api @negative
  Scenario: TC_CAT_API_USR_003 - Get category by non-existent ID as User
    Given User is authenticated
    When User sends a GET request for a non-existent category
    Then the response status code should be 404
    And the response contains an error message indicating category not found

  @user @api @security
  Scenario: TC_CAT_API_USR_004 - User is not authorized to update category
    Given User is authenticated
    And at least one category exists in the system
    When User attempts to update a category
    Then the response status code should be 403
    And the category data remains unchanged

  @user @api @read
  Scenario: TC_DSH_API_USR_001 - Get category summary as User
    Given User is authenticated
    When User sends a GET request to "/api/categories/summary"
    Then the response status code should be 200
    And the response contains accurate summary information
    And the user has read-only access to the summary

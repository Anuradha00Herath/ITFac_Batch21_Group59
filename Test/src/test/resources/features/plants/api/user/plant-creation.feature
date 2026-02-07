Feature: Plants API Management
  As a user
  I want to manage plants through API
  So that I can perform CRUD operations on plant data

  # Previous scenario remains...
  
  @api @plants @pmm @authorization
  Scenario: Verify that User cannot create a new plant due to insufficient permissions
    Given the user is authenticated as a regular user
    And a valid sub-category exists in the database
    When the user attempts to create a plant with valid data
    Then the API response status should be 403
    And the error message should indicate insufficient permissions
    And the response should confirm user role lacks create privileges
    And no new plant should be added to the database
    And the plant list count should remain unchanged
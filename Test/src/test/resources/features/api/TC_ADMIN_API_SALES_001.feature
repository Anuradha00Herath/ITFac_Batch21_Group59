@api
Feature: Sales API

  # TC_ADMIN_API_SALES_001 (already done)
  Scenario: TC_ADMIN_API_SALES_001 Successful plant sale by Admin
    Given I am authenticated as "admin" via API
    And plant 1 exists with stock at least 1
    When I sell plant 1 with quantity 1 as admin
    Then the sale should be created and plant stock should be reduced by 1

  # TC_ADMIN_API_SALES_002
  Scenario: TC_ADMIN_API_SALES_002 Admin can delete sale
    Given I am authenticated as "admin" via API
    And a sale exists (capture a sale id) as admin
    When I delete the captured sale as admin
    Then the response status should be 204

  # TC_ADMIN_API_SALES_003
  Scenario: TC_ADMIN_API_SALES_003 Sell plant with invalid quantity
    Given I am authenticated as "admin" via API
    When I sell plant 1 with quantity 0 as admin
    Then the response status should be 400
    When I sell plant 1 with quantity -1 as admin
    Then the response status should be 400

  # TC_ADMIN_API_SALES_004
  Scenario: TC_ADMIN_API_SALES_004 Get sale by id
    Given I am authenticated as "admin" via API
    And a sale exists (capture a sale id) as admin
    When I get sale by id captured as admin
    Then the response should contain sale data

  # TC_ADMIN_API_SALES_005
  Scenario: TC_ADMIN_API_SALES_005 Sell non-existing plant
    Given I am authenticated as "admin" via API
    When I sell plant 999999 with quantity 1 as admin
    Then the response status should be 404

  # TC_USER_API_SALES_001
  Scenario: TC_USER_API_SALES_001 Get all sales successfully
    Given I am authenticated as "user" via API
    When I get all sales as user
    Then the response should contain a list of sales

  # TC_USER_API_SALES_002
  Scenario: TC_USER_API_SALES_002 Get sales with pagination
    Given I am authenticated as "user" via API
    When I get sales page as user with page 0 size 5 sortField "soldAt" sortDir "desc"
    Then the response should contain paginated sales

  # TC_USER_API_SALES_003
  Scenario: TC_USER_API_SALES_003 Unauthorized sales access
    When I request sales endpoint without authentication
    Then the response status should be 401

  # TC_USER_API_SALES_004
  Scenario: TC_USER_API_SALES_004 Get sales pagination without parameters
    Given I am authenticated as "user" via API
    When I request sales pagination endpoint without parameters as user
    Then the response status should be 400

  #TC_USER_API_SALES_005
  Scenario: TC_USER_API_SALES_005 Get sale by id
    Given I am authenticated as "user" via API
    And a sale exists (capture a sale id) as user
    When I get the captured sale as user
    Then the response should contain sale data
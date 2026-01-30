# File: tests/features/api/sales-api.feature
@api
Feature: Sales API - Admin actions and User read operations

  # -----------------------------
  # ADMIN API (5)
  # -----------------------------

  Scenario: TC_ADMIN_API_SALES_001 Successful plant sale by Admin
    Given I authenticate as "admin" via API
    And a plant exists with id "P1001" and current stock is at least 1
    When I sell quantity 1 of plant "P1001"
    Then the sale should be created successfully
    And the plant "P1001" stock should be decreased by 1

  Scenario: TC_ADMIN_API_SALES_002 Admin can delete a sale
    Given I authenticate as "admin" via API
    And I create a sale for plant "P1001" with quantity 1 via API
    When I delete the created sale via API
    Then the sale should be deleted successfully
    And fetching that sale by id should return "not found"

  Scenario: TC_ADMIN_API_SALES_003 Admin cannot sell with insufficient stock
    Given I authenticate as "admin" via API
    And a plant exists with id "P1002" and current stock is at least 1
    When I sell quantity 999 of plant "P1002"
    Then the API should respond with status 400
    And the error message should contain "Insufficient stock"

  Scenario: TC_ADMIN_API_SALES_004 Unauthorized admin action with user token
    Given I authenticate as "user" via API
    When I attempt to sell quantity 1 of plant "P1001" using user credentials
    Then the API should respond with status 403

  Scenario: TC_ADMIN_API_SALES_005 Sell plant with invalid quantity
    Given I authenticate as "admin" via API
    When I sell quantity 0 of plant "P1001"
    Then the API should respond with status 400
    And the error message should contain "quantity"

  # -----------------------------
  # USER API (5)
  # -----------------------------

  Scenario: TC_USER_API_SALES_001 Get all sales successfully
    Given I authenticate as "user" via API
    When I request all sales
    Then the API should respond with status 200
    And the response should contain a list of sales

  Scenario: TC_USER_API_SALES_002 Get sales with pagination
    Given I authenticate as "user" via API
    When I request sales page 0 size 10 sorted by "soldDate" "desc"
    Then the API should respond with status 200
    And the response should contain paginated sales data

  Scenario: TC_USER_API_SALES_003 Unauthorized sales access without token
    When I request all sales without authentication
    Then the API should respond with status 401

  Scenario: TC_USER_API_SALES_004 Get sales pagination without required params
    Given I authenticate as "user" via API
    When I request sales pagination endpoint without parameters
    Then the API should respond with status 400

  Scenario: TC_USER_API_SALES_005 Get sale by Id
    Given I authenticate as "user" via API
    And at least one sale exists and I capture a valid sale id
    When I request sale by that id
    Then the API should respond with status 200
    And the response should contain the sale details

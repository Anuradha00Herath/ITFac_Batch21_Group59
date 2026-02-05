@api
Feature: Sales API
Scenario: TC_ADMIN_API_SALES_003 Sell plant with invalid quantity
Given I am authenticated as "admin" via API
When I sell plant 1 with quantity 0 as admin
Then the response status should be 400
When I sell plant 1 with quantity -1 as admin
Then the response status should be 400
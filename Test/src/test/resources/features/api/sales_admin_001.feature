@api
Feature: Sales API

  # TC_ADMIN_API_SALES_001 (already done)
  Scenario: TC_ADMIN_API_SALES_001 Successful plant sale by Admin
    Given I am authenticated as "admin" via API
    And plant 1 exists with stock at least 1
    When I sell plant 1 with quantity 1 as admin
    Then the sale should be created and plant stock should be reduced by 1
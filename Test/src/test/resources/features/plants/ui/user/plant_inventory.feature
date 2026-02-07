@ui @plants @inventory
Feature: Plant Management - UI Inventory Indicators

  As a User
  I want to see low stock indicators on plants
  So that I can be aware of plants with limited availability

  Background:
    Given the user is logged into the application
    And test plants exist in the system

  @TC_USER_UI_PMM_02
  Scenario: Verify that the user can see "Low" stock badge
    When the user navigates to the plants page
    And the user locates plants with low quantity (less than 5)
    Then the user should see "Low" badge on plants with low stock
    And all inventory indicators should be clearly visible
    And low stock information should be transparent for User role
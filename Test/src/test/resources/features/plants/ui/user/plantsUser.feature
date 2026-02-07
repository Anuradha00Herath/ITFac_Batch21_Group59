@ui @plants @sorting
Feature: Plant Management - UI Sorting

  As a User
  I want to sort plants by different attributes
  So that I can easily find and manage plants based on my needs

  Background:
    Given the user is logged into the application
    And test plants exist in the system

  @TC_USER_UI_PMM_01
  Scenario: Verify that the user can sort plants by quantity (stock)
    When the user navigates to the plants page
    And the user clicks on the "Name" column header
    Then the plants should be sorted by name
    When the user tries sorting by Price
    Then the plants should be sorted by price
    When the user tries sorting by Quantity
    Then the plants should be sorted by quantity
    And sort indicators should display properly
    And all sorting features should be available to the User
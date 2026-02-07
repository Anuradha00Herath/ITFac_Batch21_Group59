@ui @plants @permissions
Feature: Plant Management - User Role Permissions

  As a Regular User
  I want to have appropriate access restrictions
  So that I can only perform actions permitted for my role

  Background:
    Given the user is logged into the application as a regular user
    And the plants management page is accessible for viewing

  @TC_USER_UI_PMM_03
  Scenario: Verify "Add Plant" button NOT visible to User
    When the user navigates to the plants page
    And the user searches for the "Add Plant" button
    Then the "Add Plant" button should be hidden (not rendered)
    And the button should be visible but disabled with permission tooltip
    And the user cannot initiate plant creation from the UI
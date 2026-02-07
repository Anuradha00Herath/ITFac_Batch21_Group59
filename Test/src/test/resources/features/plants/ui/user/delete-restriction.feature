@ui @plants @permissions
Feature: Plant Management - Delete Restrictions

  As a Regular User
  I want to view plants without delete capabilities
  So that I cannot remove plants from the system

  Background:
    Given the user is logged into the application as a regular user
    And plants exist in the system list

  @TC_USER_UI_PMM_05
  Scenario: Verify Delete action NOT available to User
    When the user navigates to the plants page
    And the user examines each plant row in the list
    And the user looks for Delete icons or buttons
    Then the Delete actions should be hidden for all plants
    Or the Delete actions should be disabled with permission restrictions
    And the user cannot access plant deletion functionality
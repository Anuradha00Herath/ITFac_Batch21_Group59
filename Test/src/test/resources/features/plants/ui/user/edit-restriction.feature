@ui @plants @permissions
Feature: Plant Management - Edit Restrictions

  As a Regular User
  I want to see plants without edit capabilities
  So that I cannot modify plant information

  Background:
    Given the user is logged into the application as a regular user
    And plants exist in the system list

  @TC_USER_UI_PMM_04
  Scenario: Verify Edit action NOT available to User
    When the user navigates to the plants page
    And the user examines each plant row in the list
    And the user looks for Edit icons or buttons
    Then the Edit actions should be hidden for all plants
    And the Edit actions should be disabled with permission restrictions
    And the user cannot access plant editing functionality
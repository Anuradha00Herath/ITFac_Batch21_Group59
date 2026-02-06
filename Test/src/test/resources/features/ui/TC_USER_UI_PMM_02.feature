@ui
Feature: Plant Management - Search plants by name

  Scenario: TC_USER_UI_PMM_02 Verify that the user can successfully search plants by name
    Given I am logged in as "user"
    And multiple plant records exist

    When I open the Plants list page
    And I enter "Liverwort" in the plant search field
    And I click the Search button

    Then only plants matching "Liverwort" should be displayed
    And non-matching plants are not shown

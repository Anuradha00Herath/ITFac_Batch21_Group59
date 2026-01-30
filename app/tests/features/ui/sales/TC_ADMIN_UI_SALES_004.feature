@ui
Feature: Sell Plant - Successful sale redirects (Admin)

  Scenario: TC_ADMIN_UI_SALES_004 Successful sale redirects
    Given I am logged in as "admin"
    When I open the Sell Plant page
    And I select the first available plant
    And I enter sell quantity 1
    And I confirm sale
    Then I should be on the Sales list page

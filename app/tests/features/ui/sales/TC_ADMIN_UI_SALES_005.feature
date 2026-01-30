@ui
Feature: Sales List - Delete requires confirmation (Admin)

  Scenario: TC_ADMIN_UI_SALES_005 Delete sale requires confirmation
    Given I am logged in as "admin"
    And there is at least one sale record
    When I open the Sales list page
    And I click Delete on the first sale
    Then I should see a delete confirmation prompt
    And I accept the delete confirmation
    Then I should see success message "Sale deleted successfully"


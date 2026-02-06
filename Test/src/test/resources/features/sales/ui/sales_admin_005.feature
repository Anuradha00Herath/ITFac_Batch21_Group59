Feature: Admin Cancel Sale Navigation

  Scenario: TC_ADMIN_UI_SALES_005 - Cancel returns to sales list
    Given the user is logged in as "Admin"
    When the user navigates directly to the Sell Plant page
    And the user clicks Cancel on the Sell Plant form
    Then the user should be redirected to the sales list page
    And no sale should be submitted on cancel

Feature: Sales Management

  Background:
    Given the user is logged in as "User"
    And at least one sale record exists in the database

  Scenario: Sales list loads with pagination
    When the user navigates to the sales list page
    And the user clicks the next page on pagination controls
    Then the sales records for the next page should be displayed successfully
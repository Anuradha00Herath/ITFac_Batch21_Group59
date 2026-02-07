Feature: API Test Scenarios for categories page

@api @viewSubCategories
   Scenario: Verify normal user can view sub-categories
    Given I am authenticated as "user"  
    When I send a GET request to the sub-categories API as "user"
    Then the api response status should be 200
    
@api @viewMainCategories
   Scenario: Verify normal user can view main categories
    Given I am authenticated as "user" 
    When I send a GET request to the main categories API as "user"
    Then the api response status should be 200  

@api @addCategoryNonAdmin
  Scenario: Non-admin user tries to create a category 
    Given I am authenticated as "user"
    When I send a POST request to the add main category API as "user"
    Then the POST category API response status should be 403
    And the post api response body should indicate "Forbidden"


@api @deleteCategoryNonAdmin
Scenario: Non-admin user tries to delete an existing category
    Given I am authenticated as "user"
    When I send a DELETE request to the delete category API for id 2 as "user"
    Then the delete API response status should be 403
    And the delete api response body should indicate "Forbidden"

@api @addCategoryMoreCharAdmin
  Scenario: Admin user tries to create a category with more than 10 characters
    Given I am authenticated as "admin"
    When I send a POST request to the add main category API with more than 10 characters as "admin"
    Then the POST category API response status for more than max character count should be 400
    And the POST api response code for more than max character count should indicate "BAD_REQUEST"
    And the POST api response body for more than max character count should indicate "Category name must be between 3 and 10 characters"    

@api @addMainCategoryAdmin
  Scenario: Admin user tries to create a parent category 
    Given I am authenticated as "admin"
    When I send a POST request to the add main category API as "admin"
    Then the POST valid category API response status should be 201

@api @addSubCategoryAdmin
  Scenario: Admin user tries to create a sub category 
    Given I am authenticated as "admin"
    When I send a POST request to the add sub category API as "admin"
    Then the POST valid category API response status should be 201    

@api @viewSubCategoriesAdmin
   Scenario: Verify admin user can view sub-categories
    Given I am authenticated as "admin"  
    When I send a GET request to the sub-categories API as "admin"
    Then the api response status should be 200

@api @viewMainCategoriesAdmin
   Scenario: Verify admin user can view main categories
    Given I am authenticated as "admin" 
    When I send a GET request to the main categories API as "admin"
    Then the api response status should be 200 

@api @viewCategoriesWithPaginationAdmin
   Scenario: Verify admin user can view categories with pagination
    Given I am authenticated as "admin" 
    When I send a GET request to the categories API with pagination as "admin"
    Then the api response status should be 200 






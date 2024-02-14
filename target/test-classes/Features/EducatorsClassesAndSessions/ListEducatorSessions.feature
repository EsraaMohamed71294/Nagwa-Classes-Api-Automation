Feature: Test all scenarios of the api List Educators Sessions

  Scenario: Verify Get List Educator Sessions data successfully
    Given   User Create Classes and Session for Educator to list Sessions for educator
    When    Performing the Api of list sessions for educator
    And     Get Educator Sessions from database
    Then    I verify the appearance of status code 200 and Educator Sessions data returned

  Scenario: Verify Get List Educator Sessions data empty
    Given   User Create Classes only for Educator to list Sessions
    When    Performing the Api of list sessions for educator with empty data
    And     Get Educator Sessions from database
    Then    I verify the appearance of status code 200 and Educator Sessions data is empty


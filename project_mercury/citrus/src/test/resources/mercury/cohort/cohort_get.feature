Feature: Fetch a cohort

  Scenario: A user can fetch details of created cohort
    Given a workspace account "User1" is created
    And "User1" has created workspace "one"
    And "User1" has created a cohort with values
      | name             | User1's cohort                    |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | color            | red                               |
      | bannerPattern    | http://test.com/bannerPattern.jpg |
      | bannerImage      | http://test.com/bannerImage.jpg   |
      | workspaceId      | ${one_workspace_id}               |
      | productId        | A103000103955                     |
    When "User1" fetches the cohort
    Then the cohort is successfully fetched

  Scenario: A user is not able to fetch cohort not shared with him
    Given workspace accounts are created
      | User1 |
      | User2 |
    And "User1" has created workspace "one"
    And "User1" has created a cohort for workspace "one"
    When "User2" fetches the cohort
    Then "User2" is not able to fetch the cohort due to missing permission level

  Scenario: A user with CONTRIBUTOR permission can fetch a cohort shared with him
    Given workspace accounts are created
      | User1 |
      | User2 |
    And "User1" has created workspace "one"
    And "User1" has created a cohort for workspace "one"
    And "User1" has shared the created cohort with "User2" as "CONTRIBUTOR"
    When "User2" fetches the cohort
    Then the cohort is successfully fetched

  Scenario: A user with REVIEWER permission can fetch a cohort shared with him
    Given workspace accounts are created
      | User1 |
      | User2 |
    And "User1" has created workspace "one"
    And "User1" has created a cohort for workspace "one"
    And "User1" has shared the created cohort with "User2" as "REVIEWER"
    When "User2" fetches the cohort
    Then the cohort is successfully fetched

  Scenario: An enrollmentsCount field contains number of enrollments to a cohort
    Given a workspace account "Alice" is created
    And an ies account "Bob" is provisioned
    And an ies account "Charlie" is provisioned
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort for workspace "one"
    And "Bob" autoenroll to cohort "cohort"
    And "Charlie" autoenroll to cohort "cohort"
    When "Alice" fetches the cohort
    Then count of enrollments is 2
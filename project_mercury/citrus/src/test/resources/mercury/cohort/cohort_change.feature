Feature: Update a cohort

  Scenario: The cohort OWNER is able to update the cohort
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort with values
      | name           | Alice's cohort                    |
      | enrollmentType | OPEN                              |
      | startDate      | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate        | Sat, 17 Aug 2019 00:00:00 GMT     |
      | color          | red                               |
      | bannerPattern  | http://test.com/bannerPattern.jpg |
      | bannerImage    | http://test.com/bannerImage.jpg   |
      | workspaceId    | ${one_workspace_id}               |
      | productId      | A103000103955                     |
    When "Alice" updates this cohort
      | name           | Alice's updated cohort                   |
      | enrollmentType | PASSPORT                                 |
      | startDate      | Sat, 18 Aug 2018 00:00:00 GMT            |
      | endDate        | Sun, 18 Aug 2019 00:00:00 GMT            |
      | color          | black                                    |
      | bannerPattern  | http://test.com/bannerPatternUpdated.jpg |
      | productId      | A103000103955                            |
    Then the cohort is successfully updated

  Scenario: The cohort CONTRIBUTOR is able to update the cohort
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort with values
      | name           | Alice's cohort      |
      | enrollmentType | OPEN                |
      | workspaceId    | ${one_workspace_id} |
    And "Alice" has shared the created cohort with "Bob" as "CONTRIBUTOR"
    When "Bob" updates this cohort
      | name           | Bob's cohort  |
      | enrollmentType | PASSPORT      |
      | productId      | A103000103955 |
    Then the cohort is successfully updated

  Scenario: The cohort REVIEWER is not able to update the cohort
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort with values
      | name           | Alice's cohort      |
      | enrollmentType | OPEN                |
      | workspaceId    | ${one_workspace_id} |
    And "Alice" has shared the created cohort with "Bob" as "REVIEWER"
    When "Bob" updates this cohort
      | name           | Bob's cohort |
      | enrollmentType | OPEN          |
    Then "Bob" is not able to update the cohort due to missing permission level

  Scenario: The cohort OWNER is able to update the cohort type to LTI
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort with values
      | name           | Alice's cohort                    |
      | enrollmentType | OPEN                              |
      | startDate      | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate        | Sat, 17 Aug 2019 00:00:00 GMT     |
      | color          | red                               |
      | bannerPattern  | http://test.com/bannerPattern.jpg |
      | bannerImage    | http://test.com/bannerImage.jpg   |
      | workspaceId    | ${one_workspace_id}               |
      | productId      | A103000103955                     |
    When "Alice" updates this cohort
      | name           | Alice's updated cohort                   |
      | enrollmentType | LTI                                      |
      | startDate      | Sat, 18 Aug 2018 00:00:00 GMT            |
      | endDate        | Sun, 18 Aug 2019 00:00:00 GMT            |
      | color          | black                                    |
      | bannerPattern  | http://test.com/bannerPatternUpdated.jpg |
      | productId      | A103000103955                            |
      | workspaceId    | ${one_workspace_id}                      |
      | ltiKey         | LTI_key                                  |
      | ltiSecret      | LTI_secret                               |
   Then the cohort is successfully updated


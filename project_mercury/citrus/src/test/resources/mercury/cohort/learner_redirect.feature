Feature: Learner redirect

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"

  Scenario: It should create a redirect when the activity is published and the cohort settings have productId
    And "Alice" has created a cohort "TEST" with values
      | name             | Learner redirect test             |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | workspaceId      | ${one_workspace_id}               |
      | productId        | mercury:randomTimeUUID()          |
    And "Alice" has created project "APOLLO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "APOLLO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to cohort "TEST" from a project with name "DEPLOYMENT_ONE"
    # the time for the event publisher to create the redirect which is an async op
    Given "Alice" wait 1 seconds
    When "Alice" get redirect url "/to/PRODUCT/${TEST_product_id}" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" is redirected to "https://learn-bronte-sandbox.pearson.com/${TEST_id}/${DEPLOYMENT_ONE_id}"

  Scenario: It should create a redirect when the product Id is added to the settings and 1 deployment already exists
    And "Alice" has created a cohort "TEST" with values
      | name             | Learner redirect test             |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | workspaceId      | ${one_workspace_id}               |
    And "Alice" has created project "APOLLO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "APOLLO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to cohort "TEST" from a project with name "DEPLOYMENT_ONE"
    # the time for the event publisher to create the redirect which is an async op
    Given "Alice" wait 1 seconds
    When "Alice" get redirect url "/to/PRODUCT/${TEST_product_id}" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" gets a 400
    When "Alice" has updated cohort "TEST" with values
      | name             | Learner redirect test             |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | productId        | mercury:randomTimeUUID()          |
    When "Alice" get redirect url "/to/PRODUCT/${TEST_product_id}" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" is redirected to "https://learn-bronte-sandbox.pearson.com/${TEST_id}/${DEPLOYMENT_ONE_id}"

  Scenario: It should update the redirect entry when the productId is updated
    Given "Alice" has created a cohort "TEST" with values
      | name             | Learner redirect test             |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | workspaceId      | ${one_workspace_id}               |
      | productId        | x-urn:testing:01                     |
    And "Alice" has created project "APOLLO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "APOLLO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to cohort "TEST" from a project with name "DEPLOYMENT_ONE"
    When "Alice" get redirect url "/to/PRODUCT/x-urn:testing:01" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" is redirected to "https://learn-bronte-sandbox.pearson.com/${TEST_id}/${DEPLOYMENT_ONE_id}"
    When "Alice" has updated cohort "TEST" with values
      | name           | Learner redirect test         |
      | enrollmentType | OPEN                          |
      | startDate      | Fri, 17 Aug 2018 00:00:00 GMT |
      | endDate        | Sat, 17 Aug 2019 23:59:59 GMT |
      | productId      | x-urn:testing:02              |
    When "Alice" get redirect url "/to/PRODUCT/x-urn:testing:02" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" is redirected to "https://learn-bronte-sandbox.pearson.com/${TEST_id}/${DEPLOYMENT_ONE_id}"
    When "Alice" get redirect url "/to/PRODUCT/x-urn:testing:01" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" gets a 400
    # this is important so the product ids in the test can be re-used across different cohorts
    Then "Alice" has updated cohort "TEST" with values
      | name             | Learner redirect test             |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |

  Scenario: It should delete the redirect entry when the productId is deleted
    Given "Alice" has created a cohort "TEST" with values
      | name             | Learner redirect test             |
      | enrollmentType   | OPEN                              |
      | startDate        | Fri, 17 Aug 2018 00:00:00 GMT     |
      | endDate          | Sat, 17 Aug 2019 23:59:59 GMT     |
      | workspaceId      | ${one_workspace_id}               |
      | productId        | x-urn:testing:01                     |
    And "Alice" has created project "APOLLO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "APOLLO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to cohort "TEST" from a project with name "DEPLOYMENT_ONE"
    When "Alice" get redirect url "/to/PRODUCT/x-urn:testing:01" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" is redirected to "https://learn-bronte-sandbox.pearson.com/${TEST_id}/${DEPLOYMENT_ONE_id}"
    When "Alice" has updated cohort "TEST" with values
      | name           | Learner redirect test         |
      | enrollmentType | OPEN                          |
      | startDate      | Fri, 17 Aug 2018 00:00:00 GMT |
      | endDate        | Sat, 17 Aug 2019 23:59:59 GMT |
    When "Alice" get redirect url "/to/PRODUCT/x-urn:testing:01" with headers
      | Forwarded | by=1.2.3.4;host=learn-bronte-sandbox.pearson.com;proto=https |
    Then "Alice" gets a 400

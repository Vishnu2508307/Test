Feature: Move activity to another pathway

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Bob" has created workspace "two"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Bob" has created project "MLAB" in workspace "two"
    And "Bob" has created activity "UNIT_TWO" inside project "MLAB"
    And "Bob" has created a "LINEAR_TWO" pathway for the "UNIT_TWO" activity

  Scenario: User should not be able to move root activity
    Given "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Bob" has created a "LESSON_TWO" activity for the "LINEAR_TWO" pathway
    And "Bob" has granted "Alice" with "CONTRIBUTOR" permission level on workspace "two"
    When "Alice" moves "UNIT_ONE" activity to "LINEAR_TWO" pathway at position 0
    Then the activity move fails with message "Only child activities can be moved. Please use project.activity.move for root activity" and code 400

  Scenario: User should have Contributor permission over destination pathway to move activity
    Given "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Bob" has created a "LESSON_TWO" activity for the "LINEAR_TWO" pathway
    When "Alice" moves "LESSON_ONE" activity to "LINEAR_TWO" pathway at position 0
    Then the activity move fails with message "Unauthorized: User does not have required permissions on the pathway" and code 401
    When "Bob" has granted "CONTRIBUTOR" permission level to "account" "Alice" over project "MLAB"
    Then "Alice" can move "LESSON_ONE" activity to "LINEAR_TWO" pathway at position 0
    When "Alice" fetches the "LINEAR_TWO" pathway
    And "LINEAR_TWO" pathway has walkable children
      | LESSON_ONE |
      | LESSON_TWO |
    And the "LINEAR_ONE" pathway has empty walkable children

  Scenario: User should have Contributor permission over origin activity to move to pathway
    Given "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Bob" has created a "LESSON_TWO" activity for the "LINEAR_TWO" pathway
    Then "Bob" can not move "LESSON_ONE" activity to "LINEAR_TWO" pathway at position 0 due to error: code 401 message "Unauthorized: Unauthorized permission level"
    When "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can move "LESSON_ONE" activity to "LINEAR_TWO" pathway at position 0
    When "Bob" fetches the "LINEAR_TWO" pathway
    And "LINEAR_TWO" pathway has walkable children
      | LESSON_ONE |
      | LESSON_TWO |
    And the "LINEAR_ONE" pathway has empty walkable children

  Scenario: user should be able to move activity to another pathway
    Given "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Bob" has created a "LESSON_TWO" activity for the "LINEAR_TWO" pathway
    And "Bob" has granted "CONTRIBUTOR" permission level to "account" "Alice" over project "MLAB"
    #create screens and annottations for the lesson_one
    And "Alice" has created a "LINEAR_ONE_ONE" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE_ONE" pathway
    And "Alice" has created a courseware "classifying" annotation "ONE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "linking" annotation "TWO" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "THREE" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "tagging" annotation "FOUR" for element "SCREEN_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "classifying" annotation "FIVE" for element "LESSON_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "linking" annotation "SIX" for element "LESSON_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "SEVEN" for element "LESSON_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "tagging" annotation "EIGHT" for element "LESSON_ONE" and rootElement "UNIT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has saved config for "SCREEN_ONE" interactive with references to "ONE,TWO,THREE,FOUR"
    When "Alice" moves "LESSON_ONE" activity to "LINEAR_TWO" pathway at position 0
    Then the "LESSON_ONE" activity has been successfully moved to "LINEAR_TWO" pathway
    When "Alice" fetches the "LINEAR_TWO" pathway
    And "LINEAR_TWO" pathway has walkable children
      | LESSON_ONE |
      | LESSON_TWO |
    And the "LINEAR_ONE" pathway has empty walkable children
    When "Alice" fetches the "SCREEN_ONE" interactive
    Then "SCREEN_ONE" interactive config has references to "ONE,TWO,THREE,FOUR"
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "classifying" and element "SCREEN_ONE"
    Then the following courseware annotations are returned
      | ONE |
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "linking" and element "SCREEN_ONE"
    Then the following courseware annotations are returned
      | TWO |
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "identifying" and element "SCREEN_ONE"
    Then the following courseware annotations are returned
      | THREE |
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "tagging" and element "SCREEN_ONE"
    Then the following courseware annotations are returned
      | FOUR |
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "classifying" and element "LESSON_TWO"
    Then the following courseware annotations are returned
      | FIVE |
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "linking" and element "LESSON_TWO"
    Then the following courseware annotations are returned
      | SIX |
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "identifying" and element "LESSON_TWO"
    Then the following courseware annotations are returned
      | SEVEN |
    When "Alice" fetches courseware annotation by rootElement "UNIT_TWO", motivation "tagging" and element "LESSON_TWO"
    Then the following courseware annotations are returned
      | EIGHT |
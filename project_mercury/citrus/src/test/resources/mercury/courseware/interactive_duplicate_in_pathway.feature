Feature: Interactive duplication into pathway

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "COURSE" activity

  Scenario: User should be able to duplicate interactive into a pathway
    Given "Alice" has created a "Screen" interactive for the "LINEAR" pathway
    And "Alice" creates a courseware "identifying" annotation "ONE" for element "Screen" and rootElement "COURSE"
    And "Alice" has saved config for "Screen" interactive with references to "ONE"
    When "Alice" duplicates "Screen" interactive into "LINEAR" pathway
    Then the "Screen_COPY" interactive has been successfully duplicated
    When "Alice" fetches the "Screen_COPY" interactive
    Then "Screen_COPY" interactive config has no references to "ONE"
    Then "Alice" can list courseware annotation with motivation "identifying" for element "Screen_COPY" of type "INTERACTIVE" and rootElement "COURSE"
    When "Alice" fetches the "LINEAR" pathway
    And "LINEAR" pathway has walkable children
      | Screen      |
      | Screen_COPY |

  Scenario: User should be able to duplicate interactive into a pathway at the specific position
    Given "Alice" has created a "Screen" interactive for the "LINEAR" pathway
    When "Alice" duplicates "Screen" interactive into "LINEAR" pathway at position 0
    Then the "Screen_COPY" interactive has been successfully duplicated
    When "Alice" fetches the "LINEAR" pathway
    And "LINEAR" pathway has walkable children
      | Screen_COPY |
      | Screen      |

  Scenario: User should have Contributor permission over pathway to duplicate interactive
    Given a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Bob" has created workspace "two"
    And "Alice" has created activity "COURSE_two" inside project "TRO"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created a "LINEAR_two" pathway for the "COURSE_two" activity
    Given "Alice" has created a "Screen" interactive for the "LINEAR" pathway
    Then "Charlie" can not duplicate "Screen" interactive into "LINEAR_two" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Given "Alice" has granted "CONTRIBUTOR" permission level to "account" "Charlie" over project "TRO"
    Then "Charlie" can duplicate "Screen" interactive into "LINEAR_two" pathway

  Scenario: User should have Reviewer permission over activity to duplicate activity
    Given a workspace account "Bob" is created
    And "Bob" has created workspace "two"
    And "Alice" has created activity "COURSE_two" inside project "TRO"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created a "LINEAR_two" pathway for the "COURSE_two" activity
    Given "Alice" has created a "Screen" interactive for the "LINEAR" pathway
    Then "Charlie" can not duplicate "Screen" interactive into "LINEAR_two" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Given "Alice" has granted "CONTRIBUTOR" permission level to "account" "Charlie" over project "TRO"
    When "Alice" has granted "Charlie" with "REVIEWER" permission level on workspace "one"
    Then "Charlie" can duplicate "Screen" interactive into "LINEAR_two" pathway

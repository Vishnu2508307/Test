Feature: Activity duplication into pathway

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    Given "Alice" has created and published course plugin
    Given "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has created a "LINEAR" pathway for the "COURSE" activity
    And "Alice" has uploaded special "Image" asset once

  Scenario: user should be able to duplicate activity into a pathway
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR" pathway
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    When "Alice" fetches the "LINEAR" pathway
    And "LINEAR" pathway has walkable children
      | LESSON_1      |
      | LESSON_1_COPY |

  Scenario: user should be able to duplicate activity into a pathway at the specific position
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR" pathway at position 0
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    When "Alice" fetches the "LINEAR" pathway
    And "LINEAR" pathway has walkable children
      | LESSON_1_COPY |
      | LESSON_1      |

  Scenario: User should have Contributor permission over pathway to duplicate activity
    Given a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Bob" has created workspace "two"
    And "Alice" has created activity "COURSE_two" inside project "TRO"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created a "LINEAR_two" pathway for the "COURSE_two" activity
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    Then "Charlie" can not duplicate "LESSON" activity into "LINEAR_two" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Given "Alice" has granted "CONTRIBUTOR" permission level to "account" "Charlie" over project "TRO"
    Then "Charlie" can duplicate "LESSON" activity into "LINEAR_two" pathway

  Scenario: User should have Reviewer permission over activity to duplicate activity
    Given a workspace account "Bob" is created
    And a workspace account "Charlie" is created
    And "Bob" has created workspace "two"
    And "Alice" has created activity "COURSE_two" inside project "TRO"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Bob" has created a "LINEAR_two" pathway for the "COURSE_two" activity
    And "Alice" has created a "LESSON" activity for the "LINEAR" pathway
    Then "Charlie" can not duplicate "LESSON" activity into "LINEAR_two" pathway due to error: code 401 message "Unauthorized: Unauthorized permission level"
    Given "Alice" has granted "CONTRIBUTOR" permission level to "account" "Charlie" over project "TRO"
    When "Alice" has granted "Charlie" with "REVIEWER" permission level on workspace "one"
    Then "Charlie" can duplicate "LESSON" activity into "LINEAR_two" pathway


#   A new asset id will be generated if an activity and duplicated activity are not in the same project and new duplicate flow is on
  Scenario: user should be able to duplicate activity into a pathway in another course that is in another project
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    And "Alice" has added "Image" asset to "LESSON_1" activity
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    And "Alice" has created activity "COURSE_2" inside project "ACCOUNTING"
    And "Alice" has created a "LINEAR_2" pathway for the "COURSE_2" activity
    And "Alice" has created a "LESSON_2" activity for the "LINEAR_2" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR_2" pathway while new duplicate flow is "on"
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    And "LESSON_1_COPY" activity payload contains "Image" asset with a new asset id
    When "Alice" fetches the "LINEAR_2" pathway
    And "LINEAR_2" pathway has walkable children
      | LESSON_2      |
      | LESSON_1_COPY |

  Scenario: user should be able to duplicate activity into a pathway at the specific position in another course that is in another project
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    And "Alice" has added "Image" asset to "LESSON_1" activity
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    And "Alice" has created activity "COURSE_2" inside project "ACCOUNTING"
    And "Alice" has created a "LINEAR_2" pathway for the "COURSE_2" activity
    And "Alice" has created a "LESSON_2" activity for the "LINEAR_2" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR_2" pathway at position 0 while new duplicate flow is "on"
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    And "LESSON_1_COPY" activity payload contains "Image" asset with a new asset id
    When "Alice" fetches the "LINEAR_2" pathway
    And "LINEAR_2" pathway has walkable children
      | LESSON_1_COPY |
      | LESSON_2      |

#    No new asset id will be generated even an activity and duplicated activity are not in the same project but new duplicate flow is off
  Scenario: user should be able to duplicate activity into a pathway in another course that is in another project
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    And "Alice" has added "Image" asset to "LESSON_1" activity
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    And "Alice" has created activity "COURSE_2" inside project "ACCOUNTING"
    And "Alice" has created a "LINEAR_2" pathway for the "COURSE_2" activity
    And "Alice" has created a "LESSON_2" activity for the "LINEAR_2" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR_2" pathway while new duplicate flow is "off"
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    And "LESSON_1_COPY" activity payload contains "Image" asset
    When "Alice" fetches the "LINEAR_2" pathway
    And "LINEAR_2" pathway has walkable children
      | LESSON_2      |
      | LESSON_1_COPY |

  Scenario: user should be able to duplicate activity into a pathway at the specific position in another course that is in another project
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    And "Alice" has added "Image" asset to "LESSON_1" activity
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    And "Alice" has created activity "COURSE_2" inside project "ACCOUNTING"
    And "Alice" has created a "LINEAR_2" pathway for the "COURSE_2" activity
    And "Alice" has created a "LESSON_2" activity for the "LINEAR_2" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR_2" pathway at position 0 while new duplicate flow is "off"
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    And "LESSON_1_COPY" activity payload contains "Image" asset
    When "Alice" fetches the "LINEAR_2" pathway
    And "LINEAR_2" pathway has walkable children
      | LESSON_1_COPY |
      | LESSON_2      |


#  No new asset id will be generated if an activity and duplicated activity are in the same project even new duplicate flow is on
  Scenario: user should be able to duplicate activity into a pathway in another course but in the same project
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    And "Alice" has added "Image" asset to "LESSON_1" activity
    And "Alice" has created activity "COURSE_2" inside project "TRO"
    And "Alice" has created a "LINEAR_2" pathway for the "COURSE_2" activity
    And "Alice" has created a "LESSON_2" activity for the "LINEAR_2" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR_2" pathway while new duplicate flow is "on"
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    And "LESSON_1_COPY" activity payload contains "Image" asset
    When "Alice" fetches the "LINEAR_2" pathway
    And "LINEAR_2" pathway has walkable children
      | LESSON_2      |
      | LESSON_1_COPY |

  Scenario: user should be able to duplicate activity into a pathway at the specific position in another course but in the same project
    Given "Alice" has created a "LESSON_1" activity for the "LINEAR" pathway
    And "Alice" has added "Image" asset to "LESSON_1" activity
    And "Alice" has created activity "COURSE_2" inside project "TRO"
    And "Alice" has created a "LINEAR_2" pathway for the "COURSE_2" activity
    And "Alice" has created a "LESSON_2" activity for the "LINEAR_2" pathway
    When "Alice" duplicates "LESSON_1" activity into "LINEAR_2" pathway at position 0 while new duplicate flow is "on"
    Then the "LESSON_1_COPY" activity has been successfully duplicated
    And "LESSON_1_COPY" activity payload contains "Image" asset
    When "Alice" fetches the "LINEAR_2" pathway
    And "LINEAR_2" pathway has walkable children
      | LESSON_1_COPY |
      | LESSON_2      |

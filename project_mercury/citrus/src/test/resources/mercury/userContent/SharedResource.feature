Feature: Share Course with other user(s)

  Background:
    Given an account "Alice" is created
    And an account "Bob" is created
    And an account "Charlie" is created
    And an account "Chuck" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"

  Scenario: A project owner should be able to share course to other user
    And "Alice" had shared lesson "LESSON_ONE" to with "Charlie"
    Then "Alice" successfully shared

  Scenario: Unauthorized user not allowed to share project
    And "Chuck" had shared course "TRO" to with "Charlie"
    Then "Chuck" failed to share
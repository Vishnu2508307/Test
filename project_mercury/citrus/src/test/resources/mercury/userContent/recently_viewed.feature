Feature: Make course as recently viewed

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
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"

  Scenario: user should be able to add created course to recently viewed
    Given "Alice" had added activity "LESSON_ONE" to recently viewed from "UNIT_ONE" inside project "TRO" in workspace "one"
    Then  "Alice" successfully added activity to recently viewed

  Scenario: Unauthorized user not allowed to share project
    Given "Chuck" had added activity "LESSON_ONE" to recently viewed from "UNIT_ONE" inside project "TRO" in workspace "one"
    Then "Chuck" failed to update recently viewed
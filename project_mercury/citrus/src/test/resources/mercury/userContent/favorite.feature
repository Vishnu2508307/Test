Feature: Make course as favorite

  Background:
    Given an account "Alice" is created
    And an account "Bob" is created
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

  Scenario: user should be able to add created course to favorite
    Given "Alice" had added activity "LESSON_ONE" to favorite from "UNIT_ONE" inside project "TRO" in workspace "one"
    Then  "Alice" successfully added activity "LESSON_ONE" favorite

  Scenario: user should be able to retrieve created course to favorite
    Given "Alice" had added activity "LESSON_ONE" to favorite from "UNIT_ONE" inside project "TRO" in workspace "one"
    Then  "Alice" successfully added activity "LESSON_ONE" favorite
    Given "Alice" can list activity added to favorite
    Then  "Alice" successfully listed activity to favorite

  Scenario: user should be able to remove favorite
    Given "Alice" had added activity "LESSON_ONE" to favorite from "UNIT_ONE" inside project "TRO" in workspace "one"
    Then  "Alice" successfully added activity "LESSON_ONE" favorite
    Given "Alice" can remove activity "LESSON_ONE" to favorite from "UNIT_ONE" inside project "TRO" in workspace "one" added to favorite
    Then  "Alice" successfully removed activity from favorite
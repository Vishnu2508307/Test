#Feature: The export process
#
#  # These tests shouldn't be run in the pipeline until we can connect to AWS in sandbox
#  Background:
#    Given an account "Alice" is created
#    And "Alice" has created workspace "one"
#    And "Alice" has created and published course plugin
#    And "Alice" has created project "TRO" in workspace "one"
#    And "Alice" has created activity "LESSON_ONE" inside project "TRO"
#    And "Alice" has saved configuration for "LESSON_ONE" activity with config
#    """
#    {"title": "Citrus Test - Activity Config"}
#    """
#    And "Alice" has created a "LINEAR_ONE" pathway for the "LESSON_ONE" activity
#    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
#    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_ONE" pathway
#    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_ONE" pathway
#
#  Scenario: The owner of an courseware should be able to successfully export a course
#    Given "Alice" has created an export of activity "LESSON_ONE"
#    When "Alice" subscribes to the export for activity "LESSON_ONE"
#    Then "Alice" is successfully subscribed to the export
#    And "Alice" gets an export broadcast for "LESSON_ONE"
#    And "Alice" gets an export broadcast for "LESSON_ONE"
#    And "Alice" gets an export broadcast for "LESSON_ONE"
#    And "Alice" gets an export broadcast for "LESSON_ONE"
#    And "Alice" gets an export broadcast complete for "LESSON_ONE"
#    When "Alice" lists all the exports for project "TRO"
#    Then the following exports are listed for project "TRO" with export type "GENERIC"
#      | LESSON_ONE |
#    When "Alice" unsubscribes to the export for activity "LESSON_ONE"
#    Then "Alice" is successfully unsubscribed to the export
#
#  Scenario: When an error occurs the subscription should be notified
#    Given "Alice" has created project "PROJECT" in workspace "one"
#    And "Alice" has created activity "UNIT_ONE" inside project "PROJECT"
#    When "Alice" has updated the theme config for activity "UNIT_ONE" with
#    """
#    {invalid json}
#    """
#    And "Alice" has created an export of activity "UNIT_ONE"
#    When "Alice" subscribes to the export for activity "UNIT_ONE"
#    Then "Alice" is successfully subscribed to the export
#    And "Alice" gets an export broadcast error for "UNIT_ONE"

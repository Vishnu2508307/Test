Feature: Publish an activity to a learner environment

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "LESSON_A" inside project "TRO"

  Scenario: It should successfully publish an activity
    Given "Alice" has created a "FREE" pathway named "FREE_A" for the "LESSON_A" activity
    And "Alice" has created a "SCREEN_A" interactive for the "FREE_A" pathway
    And "Alice" has created a "SCREEN_B" interactive for the "FREE_A" pathway
    And "Alice" has created a "SCREEN_C" interactive for the "FREE_A" pathway
    And "Alice" has created a "SCREEN_D" interactive for the "FREE_A" pathway
    And "Alice" has created a "SCREEN_E" interactive for the "FREE_A" pathway
    When "Alice" publishes "LESSON_A" activity
    Then "LESSON_A" activity is successfully published at "DEPLOYMENT_A"


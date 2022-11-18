Feature: Subscription to progress changes

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "LINEAR_ONE" pathway
    And "Alice" has created a "LINEAR_TWO" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "LINEAR_TWO" pathway
    And "Alice" has created a "SCREEN_FOUR" interactive for the "LINEAR_ONE" pathway
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: User should be able to subscribe to progress changes and get messages
    Given "Alice" has subscribed to progress for "DEPLOYMENT_ONE" and targets
      | UNIT_ONE   |
      | LINEAR_ONE |
      | LESSON_ONE |
      | LINEAR_TWO |
      | SCREEN_ONE |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_ONE" and gets progresses
      | UNIT_ONE   | 0.11111111 |
      | LINEAR_ONE | 0.11111111 |
      | LESSON_ONE | 0.33333334 |
      | LINEAR_TWO | 0.33333334 |
      | SCREEN_ONE | 1          |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_TWO" and gets progresses
      | UNIT_ONE   | 0.22222222 |
      | LINEAR_ONE | 0.22222222 |
      | LESSON_ONE | 0.6666667  |
      | LINEAR_TWO | 0.6666667  |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_THREE" and gets progresses
      | UNIT_ONE   | 0.33333334 |
      | LINEAR_ONE | 0.33333334 |
      | LESSON_ONE | 1          |
      | LINEAR_TWO | 1          |

  Scenario: User should be able to unsubscribe to progress changes and get messages
    Given "Alice" has subscribed to progress for "DEPLOYMENT_ONE" and targets
      | UNIT_ONE   |
      | LINEAR_ONE |
      | LESSON_ONE |
      | LINEAR_TWO |
      | SCREEN_ONE |
    Then "Alice" has unsubscribed to progress for "DEPLOYMENT_ONE" and targets
      | UNIT_ONE   |
      | LINEAR_ONE |
      | LESSON_ONE |
      | LINEAR_TWO |
      | SCREEN_ONE |

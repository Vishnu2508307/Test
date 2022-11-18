Feature: Restart activity with Free pathways

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "FREE" pathway named "UNIT_PATHWAY" for the "UNIT_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "UNIT_PATHWAY" pathway
    And "Alice" has created a "LESSON_TWO" activity for the "UNIT_PATHWAY" pathway
    And "Alice" has created a "FREE" pathway named "LESSON_PATHWAY" for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LESSON_PATHWAY" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LESSON_PATHWAY" pathway
    When "Alice" publishes "UNIT_ONE" activity
    Then "UNIT_ONE" activity is successfully published at "DEPLOYMENT_ONE"

  Scenario: User should be able to restart activity
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "LESSON_ONE" activity with values
      | id         | attempt | progress |
      | UNIT_ONE   | 1       | 0        |
      | LESSON_ONE | 1       | 0        |
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets interactives with values
      | id         | attempt | progress |
      | SCREEN_ONE | 1       | 0        |
      | SCREEN_TWO | 1       | 0        |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_ONE"
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets interactives with values
      | id         | attempt | progress |
      | SCREEN_ONE | 1       | 1        |
      | SCREEN_TWO | 1       | 0        |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_TWO"
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets interactives with values
      | id         | attempt | progress |
      | SCREEN_ONE | 1       | 1        |
      | SCREEN_TWO | 1       | 1        |
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "LESSON_ONE" activity with values
      | id         | attempt | progress |
      | UNIT_ONE   | 1       | 0.5      |
      | LESSON_ONE | 1       | 1        |
    When "Alice" restarts the "UNIT_ONE" activity for "DEPLOYMENT_ONE" deployment
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "LESSON_ONE" activity with values
      | id         | attempt | progress |
      | UNIT_ONE   | 2       | 0        |
      | LESSON_ONE | 1       | 0        |
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets interactives with values
      | id         | attempt | progress |
      | SCREEN_ONE | 1       | 0        |
      | SCREEN_TWO | 1       | 0        |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_ONE"
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets interactives with values
      | id         | attempt | progress |
      | SCREEN_ONE | 1       | 1        |
      | SCREEN_TWO | 1       | 0        |
    When "Alice" does evaluation for "DEPLOYMENT_ONE" and interactive "SCREEN_TWO"
    When "Alice" opens the "LESSON_ONE" activity for the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets interactives with values
      | id         | attempt | progress |
      | SCREEN_ONE | 1       | 1        |
      | SCREEN_TWO | 1       | 1        |
    When "Alice" restarts the "LESSON_ONE" activity for "DEPLOYMENT_ONE" deployment
    When "Alice" opens the "DEPLOYMENT_ONE" deployment
    Then "Alice" gets the "LESSON_ONE" activity with values
      | id         | attempt | progress |
      | UNIT_ONE   | 2       | 0        |
      | LESSON_ONE | 2       | 0        |

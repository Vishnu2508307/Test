Feature: On activity duplication references to old APICs should be replaced with new ids.

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "COURSE_ONE" inside project "TRO"
    And "Alice" has created a "COURSE_ONE_LINEAR" pathway for the "COURSE_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "COURSE_ONE_LINEAR" pathway
    And "Alice" has created a "LESSON_ONE_LINEAR" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LESSON_ONE_LINEAR" pathway
    And "Alice" has created a "SCREEN_TWO" interactive for the "LESSON_ONE_LINEAR" pathway
    And "Alice" has created a "PROGRESS" component for the "SCREEN_ONE" interactive
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_ONE" interactive

  Scenario: For configs only references to ancestors and descendants should be replaced.
    Given "Alice" has saved config for "COURSE_ONE" activity with references to "FEEDBACK_ONE,PROGRESS,SCREEN_TWO,SCREEN_ONE,LESSON_ONE_LINEAR,LESSON_ONE,COURSE_ONE_LINEAR,COURSE_ONE"
    And "Alice" has saved config for "SCREEN_ONE" interactive with references to "COURSE_ONE,COURSE_ONE_LINEAR,SCREEN_ONE,SCREEN_TWO,FEEDBACK_ONE,PROGRESS"
    And "Alice" has saved config for "PROGRESS" component with references to "SCREEN_ONE,LESSON_ONE,COURSE_ONE_LINEAR,COURSE_ONE"
    And "Alice" has saved config for "FEEDBACK_ONE" feedback with references to "SCREEN_ONE,LESSON_ONE_LINEAR,LESSON_ONE,COURSE_ONE"
    When "Alice" duplicates "COURSE_ONE" to project "TRO"
    Then the new copy "COURSE_ONE_COPY" is successfully created inside project "TRO"
    When "Alice" fetches the "COURSE_ONE_COPY" activity
    Then "COURSE_ONE_COPY" activity has a "COURSE_ONE_LINEAR_COPY" pathway
    When "Alice" fetches the "COURSE_ONE_LINEAR_COPY" pathway
    Then "COURSE_ONE_LINEAR_COPY" pathway has walkable children
      | LESSON_ONE_COPY |
    When "Alice" fetches the "LESSON_ONE_COPY" activity
    Then "LESSON_ONE_COPY" activity has a "LESSON_ONE_LINEAR_COPY" pathway
    When "Alice" fetches the "LESSON_ONE_LINEAR_COPY" pathway
    Then "LESSON_ONE_LINEAR_COPY" pathway has walkable children
      | SCREEN_ONE_COPY |
      | SCREEN_TWO_COPY |
    When "Alice" fetches the "SCREEN_ONE_COPY" interactive
    Then "SCREEN_ONE_COPY" interactive has "FEEDBACK_ONE_COPY" feedback and "PROGRESS_COPY" component
    When "Alice" fetches the "COURSE_ONE_COPY" activity
    Then "COURSE_ONE_COPY" activity config has references to "FEEDBACK_ONE_COPY,PROGRESS_COPY,SCREEN_TWO_COPY,SCREEN_ONE_COPY,LESSON_ONE_LINEAR_COPY,LESSON_ONE_COPY,COURSE_ONE_LINEAR_COPY,COURSE_ONE_COPY"
    When "Alice" fetches the "SCREEN_ONE_COPY" interactive
    Then "SCREEN_ONE_COPY" interactive config has references to "COURSE_ONE_COPY,COURSE_ONE_LINEAR_COPY,SCREEN_ONE_COPY,SCREEN_TWO,FEEDBACK_ONE_COPY,PROGRESS_COPY"
    When "Alice" fetches the "PROGRESS_COPY" component
    Then "PROGRESS_COPY" component config has references to "SCREEN_ONE_COPY,LESSON_ONE_COPY,COURSE_ONE_LINEAR_COPY,COURSE_ONE_COPY"
    When "Alice" fetches the "FEEDBACK_ONE_COPY" feedback
    Then "FEEDBACK_ONE_COPY" feedback config has references to "SCREEN_ONE_COPY,LESSON_ONE_LINEAR_COPY,LESSON_ONE_COPY,COURSE_ONE_COPY"

  Scenario: For scenarios all references should be replaced.
    Given "Alice" has created scenario for "COURSE_ONE" activity with references
      | name      | scenario for Course_ONE activity |
      | lifecycle | ACTIVITY_EVALUATE                |
      | condition | condition                        |
      | actions   | actions                          |
    When "Alice" duplicates "COURSE_ONE" to project "TRO"
    Then the new copy "COURSE_ONE_COPY" is successfully created inside project "TRO"
    When "Alice" fetches the "COURSE_ONE_COPY" activity
    Then "COURSE_ONE_COPY" activity has a "COURSE_ONE_LINEAR_COPY" pathway
    When "Alice" fetches the "COURSE_ONE_LINEAR_COPY" pathway
    Then "COURSE_ONE_LINEAR_COPY" pathway has walkable children
      | LESSON_ONE_COPY |
    When "Alice" fetches the "LESSON_ONE_COPY" activity
    Then "LESSON_ONE_COPY" activity has a "LESSON_ONE_LINEAR_COPY" pathway
    When "Alice" fetches the "LESSON_ONE_LINEAR_COPY" pathway
    Then "LESSON_ONE_LINEAR_COPY" pathway has walkable children
      | SCREEN_ONE_COPY |
      | SCREEN_TWO_COPY |
    When "Alice" fetches the "SCREEN_ONE_COPY" interactive
    Then "SCREEN_ONE_COPY" interactive has "FEEDBACK_ONE_COPY" feedback and "PROGRESS_COPY" component
    Then the list of scenarios for the "COURSE_ONE_COPY" activity and lifecycle ACTIVITY_EVALUATE contains duplicated scenario
      | name      | scenario for Course_ONE activity |
      | lifecycle | ACTIVITY_EVALUATE                |
      | condition | condition                        |
      | actions   | actions                          |


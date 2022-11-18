Feature: Fetching a breadcrumb

  Background:
    Given a workspace account "Alice" is created
    Given "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"

  Scenario Outline: User should be able to fetch a full path from the interactive to the top activity
    Given "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "UNIT" activity for the "COURSE_LINEAR" pathway
    And "Alice" has created a "UNIT_LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "LESSON" activity for the "UNIT_LINEAR" pathway
    And "Alice" has created a "LESSON_LINEAR" pathway for the "LESSON" activity
    And "Alice" has created a "SCREEN" interactive for the "LESSON_LINEAR" pathway
    When "Alice" fetches a breadcrumb for <element>
    Then the breadcrumb should be <breadcrumb>

    Examples:
      | element             | breadcrumb                                                        |
      | SCREEN interactive  | COURSE/COURSE_LINEAR/UNIT/UNIT_LINEAR/LESSON/LESSON_LINEAR/SCREEN |
      | UNIT activity       | COURSE/COURSE_LINEAR/UNIT                                         |
      | UNIT_LINEAR pathway | COURSE/COURSE_LINEAR/UNIT/UNIT_LINEAR                             |
      | COURSE activity     | COURSE                                                            |

  Scenario Outline: User should be able to fetch workspace and project details along with the path for a courseware element
    Given "Alice" has created activity "COURSE" inside project "TRO"
    And "Alice" has created a "COURSE_LINEAR" pathway for the "COURSE" activity
    And "Alice" has created a "UNIT" activity for the "COURSE_LINEAR" pathway
    And "Alice" has created a "UNIT_LINEAR" pathway for the "UNIT" activity
    And "Alice" has created a "LESSON" activity for the "UNIT_LINEAR" pathway
    And "Alice" has created a "LESSON_LINEAR" pathway for the "LESSON" activity
    And "Alice" has created a "SCREEN" interactive for the "LESSON_LINEAR" pathway
    When "Alice" fetches a breadcrumb for <element>
    Then the breadcrumb should have <workspace> <project> <breadcrumb>

    Examples:
      | element             | workspace | project | breadcrumb                                                        |
      | SCREEN interactive  | one       | TRO     | COURSE/COURSE_LINEAR/UNIT/UNIT_LINEAR/LESSON/LESSON_LINEAR/SCREEN |
      | UNIT activity       | one       | TRO     | COURSE/COURSE_LINEAR/UNIT                                         |
      | UNIT_LINEAR pathway | one       | TRO     | COURSE/COURSE_LINEAR/UNIT/UNIT_LINEAR                             |
      | COURSE activity     | one       | TRO     | COURSE                                                            |
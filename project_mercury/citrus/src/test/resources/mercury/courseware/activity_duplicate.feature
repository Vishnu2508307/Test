Feature: Activity duplication

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"

  #  project activity duplication
  Scenario: User should be able to copy an activity inside a project
    Given "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    When "Alice" duplicates "UNIT_ONE" to project "ACCOUNTING"
    Then the new copy "NEW_UNIT_ONE" is successfully created inside project "ACCOUNTING"
    And "Alice" can list the following activities from project "ACCOUNTING"
      | NEW_UNIT_ONE |

  Scenario: User should be able to create a full copy of an activity in a project
    Given "Alice" has created and published course plugin
    And "Alice" has created project "TEMPLATES" in workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TEMPLATES"
    And "Alice" can save config for "ACTIVITY_ONE" activity successfully
    And "Alice" has saved theme config for "ACTIVITY_ONE" activity successfully
    And "Alice" has created scenarios for "ACTIVITY_ONE" activity
      | one   | ACTIVITY_EVALUATE |
      | two   | ACTIVITY_EVALUATE |
      | three | ACTIVITY_ENTRY    |
      | four  | ACTIVITY_ENTRY    |
    When "Alice" duplicates "ACTIVITY_ONE" to project "TRO"
    Then the new copy "NEW_ACTIVITY_ONE" is successfully created inside project "TRO"
    And the list of scenarios for "NEW_ACTIVITY_ONE" "ACTIVITY" and lifecycle "ACTIVITY_EVALUATE" contains
      | one |
      | two |
    And the list of scenarios for "NEW_ACTIVITY_ONE" "ACTIVITY" and lifecycle "ACTIVITY_ENTRY" contains
      | three |
      | four  |

  Scenario: The new user should be saved as the creator when an activity is duplicated in a project
    Given a workspace account "Bob" is created
    And "Bob" has created workspace "two"
    And "Bob" has created project "TRO" in workspace "two"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TEMPLATES" in workspace "one"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TEMPLATES"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TEMPLATES"
    When "Bob" duplicates "ACTIVITY_ONE" to project "TRO"
    Then the new copy "NEW_ACTIVITY_ONE" is successfully created inside project "TRO" with "Bob" as creator

  Scenario: A user should be able to duplicate a courseware structure from a top level activity in a project
    Given "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TEMPLATES" in workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created activity "COURSE_ONE" inside project "TEMPLATES"
    And "Alice" has created a "COURSE_ONE_LINEAR" pathway for the "COURSE_ONE" activity
    And "Alice" has created a "LESSON_ONE" activity for the "COURSE_ONE_LINEAR" pathway
    And "Alice" has linked to "ACTIVITY" "LESSON_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has created a "LESSON_TWO" activity for the "COURSE_ONE_LINEAR" pathway
    And "Alice" has created a "LESSON_ONE_LINEAR" pathway for the "LESSON_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LESSON_ONE_LINEAR" pathway
    And "Alice" has linked to "INTERACTIVE" "SCREEN_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has created a "SCREEN_TWO" interactive for the "LESSON_ONE_LINEAR" pathway
    And "Alice" has created a "SCREEN_THREE" interactive for the "COURSE_ONE_LINEAR" pathway
    And "Alice" has created a "PROGRESS" component for the "LESSON_TWO" activity
    And "Alice" has created 28 components for the "SCREEN_THREE" interactive with config
    """
    {"title":"Component"}
    """
    And "Alice" has created a "FEEDBACK_ONE" feedback for the "SCREEN_ONE" interactive
    And "Alice" has created a "FEEDBACK_TWO" feedback for the "SCREEN_ONE" interactive
    And "Alice" has created a "TEXT" component for the "SCREEN_ONE" interactive
    And "Alice" has created a courseware "classifying" annotation "ONE" for element "SCREEN_ONE" and rootElement "COURSE_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "linking" annotation "TWO" for element "SCREEN_ONE" and rootElement "COURSE_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "identifying" annotation "THREE" for element "SCREEN_ONE" and rootElement "COURSE_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "tagging" annotation "FOUR" for element "SCREEN_ONE" and rootElement "COURSE_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created a courseware "commenting" annotation "FIVE" for element "SCREEN_ONE" and rootElement "COURSE_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Alice" has created evaluable for "SCREEN_ONE" "INTERACTIVE" with evaluation mode "COMBINED"
    When "Alice" duplicates "COURSE_ONE" to project "TRO"
    Then the new copy "COURSE_ONE_COPY" is successfully created inside project "TRO"
    When "Alice" fetches the "COURSE_ONE_COPY" activity
    Then "COURSE_ONE_COPY" activity has a "COURSE_ONE_LINEAR_COPY" pathway
    When "Alice" fetches the "COURSE_ONE_LINEAR_COPY" pathway
    Then "COURSE_ONE_LINEAR_COPY" pathway has walkable children
      | LESSON_ONE_COPY   |
      | LESSON_TWO_COPY   |
      | SCREEN_THREE_COPY |
    When "Alice" fetches the "LESSON_ONE_COPY" activity
    Then "LESSON_ONE_COPY" activity has a "LESSON_ONE_LINEAR_COPY" pathway
    When "Alice" fetches the "LESSON_ONE_COPY" activity
    Then the activity payload contains linked items
      | ITEM_ONE |
    When "Alice" fetches the "LESSON_ONE_LINEAR_COPY" pathway
    Then "LESSON_ONE_LINEAR_COPY" pathway has walkable children
      | SCREEN_ONE_COPY |
      | SCREEN_TWO_COPY |
    When "Alice" fetches the "LESSON_TWO_COPY" activity
    Then "LESSON_TWO_COPY" activity has a "PROGRESS_COPY" component
    When "Alice" fetches the "SCREEN_ONE_COPY" interactive
    Then "SCREEN_ONE_COPY" interactive has 2 feedbacks
    When "Alice" fetches courseware annotation by rootElement "COURSE_ONE_COPY", motivation "classifying" and element "SCREEN_ONE_COPY"
    Then the following courseware annotations are returned
      | ONE |
    When "Alice" fetches courseware annotation by rootElement "COURSE_ONE_COPY", motivation "linking" and element "SCREEN_ONE_COPY"
    Then the following courseware annotations are returned
      | TWO |
    When "Alice" fetches courseware annotation by rootElement "COURSE_ONE_COPY", motivation "identifying" and element "SCREEN_ONE_COPY"
    Then the following courseware annotations are returned
      | THREE |
    When "Alice" fetches courseware annotation by rootElement "COURSE_ONE_COPY", motivation "tagging" and element "SCREEN_ONE_COPY"
    Then the following courseware annotations are returned
      | FOUR |
    When "Alice" fetches courseware annotation by rootElement "COURSE_ONE_COPY", motivation "commenting" and element "SCREEN_ONE_COPY"
    Then no courseware annotations are returned
    And "Alice" tries to fetch evaluable for "SCREEN_ONE_COPY" "INTERACTIVE"
    Then "Alice" can fetch following data from "SCREEN_ONE_COPY" evaluable
      | elementType    | INTERACTIVE   |
      | evaluationMode | COMBINED      |
    When "Alice" fetches the "SCREEN_ONE_COPY" interactive
    Then "SCREEN_ONE_COPY" interactive has 1 components
    When "Alice" fetches the "SCREEN_ONE_COPY" interactive
    Then the interactive payload contains linked items
      | ITEM_ONE |

  Scenario: User should be able to copy an activity inside a project with activity theme association
    Given "Alice" created theme "theme_one" for workspace "one"
    And "Alice" creates "DEFAULT" theme variant "Main" for theme "theme_one" with config
    """
     {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    When "Alice" associate theme "theme_one" with icon libraries
      | name        | status   |
      | FONTAWESOME | SELECTED |
      | MICROSOFT   |          |
    Then the association is created successfully
    Then "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created project "ACCOUNTING" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    When "Alice" duplicates "UNIT_ONE" to project "ACCOUNTING"
    Then the new copy "NEW_UNIT_ONE" is successfully created inside project "ACCOUNTING"
    And "Alice" can list the following activities from project "ACCOUNTING"
      | NEW_UNIT_ONE |

  Scenario: A user should be able to duplicate activity when theme has been deleted by other user
    Given a workspace account "Bob" is created
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" created theme "theme_one" for workspace "one"
    When "Alice" creates "DEFAULT" theme variant "Sepia" for theme "theme_one" with config
    """
    {"color":"black", "margin":"30"}
    """
    Then default theme variant "variant_one_theme_one" is created successfully
    And "Alice" associate theme "theme_one" with "ACTIVITY" "UNIT_ONE"
    Then theme "theme_one" is successfully associated with activity "UNIT_ONE"
    And "Alice" granted "Bob" with "OWNER" permission level for theme "theme_one"
    When "Bob" deletes theme "theme_one"
    Then "theme_one" is successfully deleted
    When "Alice" duplicates "UNIT_ONE" to project "TRO"
    Then the new copy "NEW_UNIT_ONE" is successfully created inside project "TRO"
    And "Alice" can list the following activities from project "TRO"
      | NEW_UNIT_ONE |
      | UNIT_ONE |

Feature: It should allow a workspace user with permissions to register/de-register a plugin reference to/from a student scope

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

  Scenario: It should not allow a user without permission to register an element to a student scope
    When "Bob" register "LESSON_TWO" "ACTIVITY" element to "UNIT_ONE" student scope
    Then the "register" request is not permitted

  Scenario: It should not allow to register an element to a scope that is not in the parent path
    When "Alice" register "LESSON_TWO" "ACTIVITY" element to "LESSON_ONE" student scope
    Then the register request fails due to "cannot register element with student scope `${LESSON_ONE_studentScope}` that is not in the parent path"

  Scenario: It should not allow to register an element that is not a plugin reference
    When "Alice" register "LINEAR_ONE" "PATHWAY" element to "UNIT_ONE" student scope
    Then the register request fails due to "only elementType with a plugin reference can be registered"

  Scenario: It should allow a user with permission to register an element to a student scope
    When "Alice" register "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    Then the element is successfully registered
    Then "Alice" can list the following sources registered for a scope "UNIT_ONE" with element "UNIT_ONE" of type "ACTIVITY"
      | LESSON_ONE | ACTIVITY |

  Scenario: It should not allow a user without permission to de-register an element from a student scope
    Given "Alice" has registered "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    When "Bob" de-register "LESSON_ONE" "ACTIVITY" element from "UNIT_ONE" student scope
    Then the "deregister" request is not permitted

  Scenario: It should allow a user with permission to de-register an element from a student scope
    Given "Alice" has registered "SCREEN_THREE" "INTERACTIVE" element to "LESSON_ONE" student scope
    When "Alice" de-register "SCREEN_THREE" "INTERACTIVE" element from "LESSON_TWO" student scope
    Then the element is successfully de-registered
    Then "Alice" can list the following sources registered for a scope "LESSON_ONE" with element "LESSON_ONE" of type "ACTIVITY"
      | SCREEN_THREE | INTERACTIVE |

  Scenario: It should allow a user to list all sources for a scope
    When "Alice" register "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    Then the element is successfully registered
    When "Alice" register "LESSON_TWO" "ACTIVITY" element to "UNIT_ONE" student scope
    Then the element is successfully registered
    Then "Alice" can list the following sources registered for a scope "UNIT_ONE" with element "UNIT_ONE" of type "ACTIVITY"
      | LESSON_ONE | ACTIVITY |
      | LESSON_TWO | ACTIVITY |
    When "Alice" de-register "LESSON_TWO" "ACTIVITY" element from "UNIT_ONE" student scope
    Then the element is successfully de-registered
    Then "Alice" can list the following sources registered for a scope "UNIT_ONE" with element "UNIT_ONE" of type "ACTIVITY"
      | LESSON_ONE | ACTIVITY |
    When "Alice" de-register "LESSON_ONE" "ACTIVITY" element from "UNIT_ONE" student scope
    Then the element is successfully de-registered
    Then "Alice" list empty sources registered for a scope "UNIT_ONE" with element "UNIT_ONE" of type "ACTIVITY"

  Scenario: It should not allow a user with no permission to list sources for a scope
    When "Alice" register "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    Then the element is successfully registered
    When "Bob" tries to list all sources registered for a scope "UNIT_ONE" with element "UNIT_ONE" of type "ACTIVITY"
    Then it fails because of missing permission level

  Scenario: A user with REVIEWER permission should be able to list all sources for a scope
    When "Alice" register "LESSON_ONE" "ACTIVITY" element to "UNIT_ONE" student scope
    Then the element is successfully registered
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    Then "Bob" can list the following sources registered for a scope "UNIT_ONE" with element "UNIT_ONE" of type "ACTIVITY"
      | LESSON_ONE | ACTIVITY |

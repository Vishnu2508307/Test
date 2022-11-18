Feature: Link/unlink competency documents to a courseware element

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created a document item "ITEM_TWO" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway

  Scenario: It should allow a workspace user with owner permission to link/unlink multiple competency item to a courseware element
    Given "Alice" has linked to "INTERACTIVE" "SCREEN_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
      | ITEM_TWO |
    When "Alice" fetches the "SCREEN_ONE" interactive
    Then the interactive payload contains linked items
      | ITEM_ONE |
      | ITEM_TWO |
    When "Alice" unlinks from "INTERACTIVE" "SCREEN_ONE" competency items from document "SKILLS"
      | ITEM_TWO |
    Then the competency item is successfully "Unlink"
    When "Alice" fetches the "SCREEN_ONE" interactive
    Then the interactive payload contains linked items
      | ITEM_ONE |

  Scenario: It should not allow a workspace user without permission over the courseware element to link/unlink competency items
    Given "Alice" has shared the "SKILLS" document with "Bob" as CONTRIBUTOR
    When "Bob" links to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    Then the competency item is not "linked" due to missing permission level

  Scenario: It should not allow a workspace user without permission over the competency document to link/unlink competency items
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" links to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    Then the competency item is not "linked" due to missing permission level

  Scenario: It should allow a workspace user with proper permissions to link/unlink competency items to a courseware element
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    And "Alice" has granted "CONTRIBUTOR" permission level to "account" "Bob" over project "TRO"
    And "Alice" has shared the "SKILLS" document with "Bob" as CONTRIBUTOR
    When "Bob" has linked to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    When "Alice" fetches the "UNIT_ONE" activity
    Then the activity payload contains linked items
      | ITEM_ONE |
    Then "Bob" can also unlink from "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    When "Alice" fetches the "UNIT_ONE" activity
    Then the activity payload does not contain linked items

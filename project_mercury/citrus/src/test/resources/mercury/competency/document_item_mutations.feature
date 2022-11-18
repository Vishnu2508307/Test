Feature: Testing document item mutations api

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"

  Scenario: A workspace user should be able to create a document item
    When "Alice" creates a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    Then the document item "ITEM_ONE" is created successfully

  Scenario: A workspace user that has no permission over the competency document should not be able to create an item
    Given a workspace account "Bob" is created
    When "Bob" creates a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    Then the document item is not "created" due to missing permission level

  Scenario: A workspace user should be able to update an existing document item
    Given "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    When "Alice" updates document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | performance skill |
      | abbreviatedStatement | ps                |
      | humanCodingScheme    | 30SC              |
    Then the document item "ITEM_ONE" is updated successfully with
      | fullStatement        | performance skill |
      | abbreviatedStatement | ps                |
      | humanCodingScheme    | 30SC              |

  Scenario: A workspace user should be able to delete a document item
    Given "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    When "Alice" deletes document item "ITEM_ONE" for "SKILLS" document
    Then document item "ITEM_ONE" for "SKILLS" document is successfully deleted

  Scenario: A workspace user with no permission over the competency document should not be able to replace an item
    Given "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And a workspace account "Bob" is created
    When "Bob" updates document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | performance skill |
      | abbreviatedStatement | ps                |
      | humanCodingScheme    | 30SC              |
    Then the document item is not "replaced" due to missing permission level

  Scenario: A workspace user with no permission over the competency document should not be able to delete an item
    Given "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And a workspace account "Bob" is created
    When "Bob" deletes document item "ITEM_ONE" for "SKILLS" document
    Then the document item is not "deleted" due to missing permission level

  Scenario: Document item can not be deleted if it was published
    Given "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill 1 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has linked to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" deletes document item "ITEM_ONE" for "SKILLS" document
    Then GraphQL should respond with error code 400 and message "Published document item can not be deleted"

  Scenario: Document item cannot be deleted if it was linked to a courseware element
    Given "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill 1 |
      | abbreviatedStatement | ks                |
      | humanCodingScheme    | 10SC              |
    And "Alice" has linked to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    When "Alice" deletes document item "ITEM_ONE" for "SKILLS" document
    Then GraphQL should respond with error code 400 and message "Linked document item can not be deleted"

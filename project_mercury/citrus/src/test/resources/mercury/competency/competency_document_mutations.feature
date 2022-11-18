Feature: Testing document mutations api

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "one"
    And "Alice" has created a competency document "SKILLS" in workspace "one"

  Scenario: It should allow a document owner to delete the document
    When "Alice" has deleted a document "SKILLS" in workspace "one"
    Then document "SKILLS" in workspace "one" is successfully deleted
    When "Alice" fetches competency documents for workspace "one"
    Then the no documents are returned for workspace

  Scenario: It should allow a document owner to delete the document and items
    When "Alice" creates a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    Then the document item "ITEM_ONE" is created successfully
    When "Alice" has deleted a document "SKILLS" in workspace "one"
    Then document "SKILLS" in workspace "one" is successfully deleted
    When "Alice" fetches competency documents for workspace "one"
    Then the no documents are returned for workspace

  Scenario: It should allow a document owner to get undeleted documents and items
    When "Alice" has created a competency document "SKILLS_TWO" in workspace "one"
    And "Alice" has created a document item "ITEM_TWO" for "SKILLS_TWO" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    When "Alice" has deleted a document "SKILLS" in workspace "one"
    Then document "SKILLS" in workspace "one" is successfully deleted
    When "Alice" fetches competency documents for workspace "one"
    Then the following documents are returned in workspace
      | SKILLS_TWO | ITEM_TWO        |

  Scenario: It should allow a document owner to get undeleted documents
    When "Alice" has created a competency document "SKILLS_TWO" in workspace "one"
    When "Alice" has deleted a document "SKILLS_TWO" in workspace "one"
    Then document "SKILLS_TWO" in workspace "one" is successfully deleted
    When "Alice" fetches competency documents for workspace "one"
    Then the following documents are returned in workspace
      | SKILLS |

  Scenario:  It should allow a document contributor to delete the document
    Given "Alice" has shared the "SKILLS" document with "Bob" as CONTRIBUTOR
    When "Bob" has deleted a document "SKILLS" in workspace "one"
    Then document "SKILLS" in workspace "one" is successfully deleted
    When "Alice" fetches competency documents for workspace "one"
    Then the no documents are returned for workspace

  Scenario: It should not allow a document reviewer to delete the document
    Given "Alice" has shared the "SKILLS" document with "Bob" as REVIEWER
    When "Bob" has deleted a document "SKILLS" in workspace "one"
    Then the graphql request is denied due to missing permission level

  Scenario:  It should not allow a workspace user with contributor permission to delete the document without document permission
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" has deleted a document "SKILLS" in workspace "one"
    Then the graphql request is denied due to missing permission level

  Scenario: A document cannot be deleted if any document item was published
    Given "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill 1 |
      | abbreviatedStatement | ks 1              |
      | humanCodingScheme    | 10SC 1            |
    And "Alice" has linked to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_ONE |
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Alice" has created a document item "ITEM_TWO" for "SKILLS" document with
      | fullStatement        | knowledge skill 2 |
      | abbreviatedStatement | ks 2              |
      | humanCodingScheme    | 10SC 2            |
    When "Alice" has deleted a document "SKILLS" in workspace "one"
    Then GraphQL should respond with error code 400 and message "Published document item can not be deleted"

  Scenario: A document cannot be deleted if any document item linked to a courseware element
    Given "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill 1 |
      | abbreviatedStatement | ks 1              |
      | humanCodingScheme    | 10SC 1            |
    And "Alice" has created a document item "ITEM_TWO" for "SKILLS" document with
      | fullStatement        | knowledge skill 2 |
      | abbreviatedStatement | ks 2              |
      | humanCodingScheme    | 10SC 2            |
    And "Alice" has linked to "ACTIVITY" "UNIT_ONE" competency items from document "SKILLS"
      | ITEM_TWO |
    When "Alice" has deleted a document "SKILLS" in workspace "one"
    Then GraphQL should respond with error code 400 and message "Linked document item can not be deleted"

  Scenario: It should allow a document owner to update the document
    When "Alice" creates a document item "ITEM_ONE" for "SKILLS" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    Then the document item "ITEM_ONE" is created successfully
    When "Alice" updates the document "SKILLS" in workspace "one" with
      | title | updated title |
    Then document "SKILLS" in workspace "one" is successfully updated with
      | title | updated title |

  Scenario: It should allow a document contributor to update the document
    Given "Alice" has shared the "SKILLS" document with "Bob" as CONTRIBUTOR
    When "Bob" updates the document "SKILLS" in workspace "one" with
      | title | updated title |
    Then document "SKILLS" in workspace "one" is successfully updated with
      | title | updated title |

  Scenario: It should not allow a document reviewer to update the document
    Given "Alice" has shared the "SKILLS" document with "Bob" as REVIEWER
    When "Bob" updates the document "SKILLS" in workspace "one" with
      | title | updated title |
    Then the graphql request is denied due to missing permission level

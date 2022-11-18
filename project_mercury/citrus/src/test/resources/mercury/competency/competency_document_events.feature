Feature: It should broadcast changes to subscribed clients when create/update/delete a document item

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Bob" is logged via a "Bob" client
    And "Alice" has created workspace "one"
    And "Alice" has created a competency document "KNOWLEDGE" in workspace "one"
    And "Alice" has granted "Bob" with "REVIEWER" permission level on workspace "one"
    And "Alice" has shared the "KNOWLEDGE" document with "Bob" as CONTRIBUTOR

  Scenario: Bob should be notified when Alice creates a document item
    Given "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" has created a document item "SKILL_ONE" for "KNOWLEDGE" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    Then "Bob" will receive document item "SKILL_ONE" payload via "Bob" client

  Scenario: Bob should be notified when Alice updates a document item
    Given "Alice" has created a document item "SKILL_ONE" for "KNOWLEDGE" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" has updated document item "SKILL_ONE" for document "KNOWLEDGE" with
      | fullStatement        | performance skill |
      | abbreviatedStatement | ps                |
      | humanCodingScheme    | 30SC              |
    Then "Bob" will receive document item "SKILL_ONE" payload via "Bob" client

  Scenario: Bob should be notified when Alice deletes a document item
    Given "Alice" has created a document item "SKILL_ONE" for "KNOWLEDGE" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" has deleted document item "SKILL_ONE" for "KNOWLEDGE" document
    Then "Bob" will receive document item "SKILL_ONE" payload via "Bob" client

  Scenario: Bob should be notified when Alice creates a new association
    Given "Alice" has created a document item "ITEM_ONE" for "KNOWLEDGE" document
    And "Alice" has created a document item "ITEM_TWO" for "KNOWLEDGE" document
    And "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" has created an association "ASSOCIATION_ONE" for "KNOWLEDGE" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    Then "Bob" received association "ASSOCIATION_ONE" payload via "Bob" client

  Scenario: Bob should be notified when Alice deletes an association
    Given "Alice" has created a document item "ITEM_ONE" for "KNOWLEDGE" document
    And "Alice" has created a document item "ITEM_TWO" for "KNOWLEDGE" document
    And "Alice" has created an association "ASSOCIATION_ONE" for "KNOWLEDGE" document with
      | originItemId      | ITEM_ONE  |
      | destinationItemId | ITEM_TWO  |
      | associationType   | isChildOf |
    And "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" has deleted association "ASSOCIATION_ONE" for "KNOWLEDGE" document
    Then "Bob" received association "ASSOCIATION_ONE" id and "KNOWLEDGE" document id via "Bob" client

  Scenario: Bob should be notified when Alice deletes a document
    Given "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" has deleted a document "KNOWLEDGE" in workspace "one"
    Then document "KNOWLEDGE" in workspace "one" is successfully deleted
    And "Bob" will receive document "KNOWLEDGE" payload via "Bob" client

  Scenario: Bob should be notified when Alice deletes a document and its item
    Given "Alice" has created a document item "SKILL_ONE" for "KNOWLEDGE" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" has deleted a document "KNOWLEDGE" in workspace "one"
    Then document "KNOWLEDGE" in workspace "one" is successfully deleted
    And "Bob" will receive document "KNOWLEDGE" payload via "Bob" client

  Scenario: Bob should be notified when Alice updates a document title
    Given "Bob" has subscribed to document "KNOWLEDGE" via "Bob" client
    When "Alice" updates the document "KNOWLEDGE" in workspace "one" with
      | title | updated title |
    Then document "KNOWLEDGE" in workspace "one" is successfully updated with
      | title | updated title |
    Then "Bob" will receive document "KNOWLEDGE" payload via "Bob" client
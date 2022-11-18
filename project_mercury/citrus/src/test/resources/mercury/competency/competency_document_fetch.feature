Feature: It should allow a workspace user with permission to fetch competency documents

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created workspace "one"
    And "Alice" has created workspace "two"
    And "Alice" has created a competency document "SKILL_ONE" in workspace "one"
    And "Alice" has created a competency document "SKILL_TWO" in workspace "one"
    And "Alice" has created a competency document "SKILL_THREE" in workspace "two"
    And "Alice" has created a document item "ITEM_ONE" for "SKILL_ONE" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created a document item "ITEM_TWO" for "SKILL_ONE" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created a document item "ITEM_THREE" for "SKILL_TWO" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |
    And "Alice" has created a document item "ITEM_FOUR" for "SKILL_THREE" document with
      | fullStatement        | knowledge skill |
      | abbreviatedStatement | ks              |
      | humanCodingScheme    | 10SC            |

  Scenario: It should allow the creator to list the document items in workspace
    When "Alice" fetches competency documents for workspace "one"
    Then the following documents are returned in workspace
     | SKILL_ONE | ITEM_ONE,ITEM_TWO |
     | SKILL_TWO | ITEM_THREE        |

  Scenario: It should allow a reviewer to list the document items in workspace
    Given "Alice" has shared the "SKILL_ONE" document with "Bob" as REVIEWER
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" fetches competency documents for workspace "one"
    Then the following documents are returned in workspace
      | SKILL_ONE | ITEM_ONE,ITEM_TWO |

  Scenario: It should not allow a user without workspace permission to list the document items in workspace
    Given "Alice" has shared the "SKILL_ONE" document with "Bob" as REVIEWER
    When "Bob" fetches competency documents for workspace "one"
    Then the graphql request is denied due to missing permission level

  Scenario: It should return an empty list if the documents where not shared with a user in workspace
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" fetches competency documents for workspace "one"
    Then the no documents are returned for workspace

  Scenario: It should allow the creator to list the document items
    When "Alice" fetches competency documents
    Then the following documents are returned
      | SKILL_ONE   | ITEM_ONE,ITEM_TWO |
      | SKILL_TWO   | ITEM_THREE        |
      | SKILL_THREE | ITEM_FOUR         |

  Scenario: It should allow a reviewer to list the document items
    Given "Alice" has shared the "SKILL_ONE" document with "Bob" as REVIEWER
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" fetches competency documents
    Then the following documents are returned
      | SKILL_ONE | ITEM_ONE,ITEM_TWO |

  Scenario: It should  allow a user without workspace permission to list the document items shared with them
    Given "Alice" has shared the "SKILL_ONE" document with "Bob" as REVIEWER
    When "Bob" fetches competency documents
    Then the following documents are returned
      | SKILL_ONE | ITEM_ONE,ITEM_TWO |

  Scenario: It should return an empty list if the documents where not shared with a user
    Given "Alice" has granted "Bob" with "CONTRIBUTOR" permission level on workspace "one"
    When "Bob" fetches competency documents
    Then the no documents are returned

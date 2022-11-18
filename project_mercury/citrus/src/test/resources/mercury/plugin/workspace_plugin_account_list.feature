Feature: Fetching a summary list and a detail list of accounts which have access to a plugin

  Scenario: User are added to the list of plugin accounts if plugin was shared with him
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created a plugin
    And "Alice" shares this plugin with "Bob"
    When "Alice" requests detailed list of plugin accounts
    Then the following users are in the detailed list
      | Alice | OWNER       |
      | Bob   | CONTRIBUTOR |
    When "Alice" revokes "Bob"'s plugin permission
    And "Alice" requests detailed list of plugin accounts
    Then the following users are in the detailed list
      | Alice | OWNER |

  Scenario: User are added to the summary list of plugin accounts if plugin was shared with him
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created a plugin
    And "Alice" shares this plugin with "Bob"
    When "Alice" requests summary list of plugin accounts
    Then the following users are in the summary list
      | Alice | OWNER       |
      | Bob   | CONTRIBUTOR |
    When "Alice" revokes "Bob"'s plugin permission
    And "Alice" requests summary list of plugin accounts
    Then the following users are in the summary list
      | Alice | OWNER |

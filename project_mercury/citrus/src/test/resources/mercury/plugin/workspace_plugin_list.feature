Feature: Fetching a list of plugins visible for an account

  Scenario: An account can see plugins created by him
    Given a workspace account "Alice" is created
    When "Alice" has created a plugin
    Then "Alice" can see this plugin in a list of visible plugins

  Scenario: An account can not see plugins created by other users if there are not shared with him
    Given workspace accounts are created
      | Alice |
      | Bob   |
    When "Alice" has created a plugin
    Then "Bob" can not see any plugin in workspace

  Scenario: An account can see plugins shared with him
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created a plugin
    When "Alice" shares this plugin with "Bob"
    Then "Bob" can see this plugin in a list of visible plugins
    When "Alice" revokes "Bob"'s plugin permission
    Then "Bob" can not see any plugin in workspace

  Scenario: An account should be able to see all the plugins shared with the teams he/she belongs to
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created a plugin with name "Input"
    And "Alice" has created a plugin with name "Graph"
    And "Bob" has created a plugin with name "Calculator"
    And "Alice" has created a "Development" team
    And "Alice" has shared plugin "Graph" with team "Development" as "CONTRIBUTOR"
    And "Alice" adds "Bob" to the "Development" team as "REVIEWER"
    When "Bob" lists the workspace plugins
    Then the following plugins are returned
      | Graph      |
      | Calculator |
    When "Alice" revokes "Bob"'s "Development" team permission
    When "Bob" lists the workspace plugins
    Then the following plugins are returned
      | Calculator |

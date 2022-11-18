Feature: Fetching a plugin collaborators list This is combined list of teams and accounts

  Background:
    Given workspace accounts are created
      | Alice |
      | Bob   |
    And "Alice" has created a plugin
    And "Alice" has created a "Awesome" team

  Scenario: Team is added to the list of collaborators if plugin was shared with it
    Given "Alice" has shared a plugin with "Awesome" team as "REVIEWER"
    Then the list of plugin collaborators contains total 2
      | name    | permissionLevel | type    |
      | Awesome | REVIEWER        | team    |
      | Alice   | OWNER           | account |
    When "Alice" revokes "Awesome" team's plugin permission
    Then the list of plugin collaborators contains total 1
      | name  | permissionLevel | type    |
      | Alice | OWNER           | account |

  Scenario: User is added to the list of collaborators if plugin was shared with him
    When "Alice" has shared a plugin with "Bob" as "CONTRIBUTOR"
    Then the list of plugin collaborators contains total 2
      | name  | permissionLevel | type    |
      | Alice | OWNER           | account |
      | Bob  | CONTRIBUTOR     | account |
    When "Alice" revokes "Bob"'s plugin permission
    Then the list of plugin collaborators contains total 1
      | name  | permissionLevel | type    |
      | Alice | OWNER           | account |

  Scenario: User should be able to limit the list of collaborators
    When "Alice" has shared a plugin with "Awesome" team as "REVIEWER"
    Then the list of plugin collaborators with limit 1 contains total 2
      | name    | permissionLevel | type |
      | Awesome | REVIEWER        | team |

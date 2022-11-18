Feature: Associate and disassociate icon library with theme and activity theme

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" created theme "theme_one" for workspace "one"

  Scenario: User should be able to create icon library and theme association
    When "Alice" associate theme "theme_one" with icon libraries
      | name        | status   |
      | FONTAWESOME | SELECTED |
      | MICROSOFT   |          |
    Then the association is created successfully
    When "Alice" associate theme "theme_one" with icon libraries
      | name        | status   |
      | MICROSOFT   | SELECTED |
    Then the association is created successfully

  Scenario: User should be able to associate activity with icon library for activity theme
    When "Alice" has created activity "UNIT" inside project "TRO"
    When "Alice" associate activity "UNIT" with icon libraries
      | name        | status   |
      | FONTAWESOME | SELECTED |
      | MICROSOFT   |          |
    Then icon libraries associated successfully with activity
    Then the "UNIT" activity has activity theme icon libraries
    When "Alice" associate activity "UNIT" with icon libraries
      | name        | status   |
      | MICROSOFT   | SELECTED |
    Then icon libraries associated successfully with activity
    Then the "UNIT" activity has activity theme icon libraries

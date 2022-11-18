Feature: Create and update a team

  Background:
    Given a workspace account "Alice" is created

  Scenario: User should be able to create a team
    When "Alice" creates a team
      | name        | Citrus team  |
      | description | awesome team |
      | thumbnail   | thumbnail    |
    Then the team is successfully created

  Scenario: User should be able to update team fields separately
    Given "Alice" has created a "Citrus team" team
    When "Alice" updates the team "Citrus team"
      | name        | Updated Citrus team |
      | description | awesome team        |
      | thumbnail   | thumbnail           |
    Then the team is successfully updated and has fields
      | name        | Updated Citrus team |
      | description | awesome team        |
      | thumbnail   | thumbnail           |
    When "Alice" updates the team "Citrus team"
      | description | awesome team 2 |
      | thumbnail   |                |
    Then the team is successfully updated and has fields
      | name        | Updated Citrus team |
      | description | awesome team 2      |

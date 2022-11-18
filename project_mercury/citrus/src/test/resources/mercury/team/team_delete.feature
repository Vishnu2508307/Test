Feature: Delete a team

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created

  Scenario: User should be able to delete a team
    Given "Alice" has created a "Citrus team" team
    When "Alice" tries to delete the team "Citrus team"
    Then the team is successfully deleted

  Scenario: User with reviewer permission should not be able to delete a team
    Given "Alice" has created a "Citrus team" team
    And "Alice" has granted "Bob" with "REVIEWER" permission over the team "Citrus team"
    When "Bob" tries to delete the team "Citrus team"
    Then the team is not deleted due to missing permission level

  Scenario: User with contributor permission should not be able to delete a team
    Given "Alice" has created a "Citrus team" team
    And "Alice" has granted "Bob" with "CONTRIBUTOR" permission over the team "Citrus team"
    When "Bob" tries to delete the team "Citrus team"
    Then the team is not deleted due to missing permission level

  Scenario: User with owner permission should be able to delete a team
    Given "Alice" has created a "Citrus team" team
    And "Alice" has granted "Bob" with "OWNER" permission over the team "Citrus team"
    When "Bob" tries to delete the team "Citrus team"
    Then the team is successfully deleted

  Scenario: User should be able to delete a team and all associated permissions
    Given "Alice" has created a "TeamOne" team
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    When "Alice" adds "Bob" to the team "TeamOne" as "REVIEWER"
    Then "Bob" is successfully added to the team "TeamOne" as "REVIEWER"
    And "Alice" has granted "CONTRIBUTOR" permission level to team "TeamOne" over project "TRO"
    Then "Bob" is able to fetch project "TRO"
    When "Alice" tries to delete the team "TeamOne"
    Then the team is successfully deleted
    When "Bob" fetches project "TRO"
    Then the project is not fetched due to missing proper permission


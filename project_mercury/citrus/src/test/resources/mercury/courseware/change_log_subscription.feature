Feature: Courseware changelogs events

  Background:
    Given an account "Alice" is created
    Given "Alice" has created and published course plugin
    And an account "Bob" is created
    And an account "Chuck" is created
    And "Bob" is logged via a "Bob" client
    And "Chuck" is logged via a "Chuck" client
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"

  Scenario: A user with reviewer permission over the project should be able to see changelogs events
    Given "Alice" has granted "REVIEWER" permission level to "account" "Bob" over project "TRO"
    And "Bob" subscribes to changelog events over project "TRO"
    And "Alice" has created activity "ACTIVITY_ONE" inside project "TRO"
    Then "Bob" sees "ACTIVITY_ONE" was "CREATED" by "Alice" changelog
    When "Alice" deletes activity "ACTIVITY_ONE" from project "TRO"
    Then activity "ACTIVITY_ONE" is successfully deleted from project "TRO"
    Then "Bob" sees "ACTIVITY_ONE" was "DELETED" by "Alice" changelog

  Scenario: A user subscribed to the root activity should be able to see changelogs event
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Bob" subscribes to changelogs events over activity "UNIT_ONE"
    When "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Then "Bob" sees "SCREEN_ONE" was "CREATED" by "Alice" changelog

  Scenario:A user with reviewer or higher permission can only subscribe to changelog and is not notified in case of unsubscription
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    Then "Chuck" tries to subscribe to the changelogs events over activity "UNIT_ONE" but fails due to missing permission level
    And "Alice" has granted "REVIEWER" permission level to "account" "Chuck" over project "TRO"
    And "Chuck" subscribes to changelogs events over activity "UNIT_ONE"
    Then "Chuck" unsubscribes to changelogs events over the activity "UNIT_ONE"
    When "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    Then "Chuck" should not be notified





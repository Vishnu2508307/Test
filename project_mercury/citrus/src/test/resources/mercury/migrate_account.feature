Feature: Migrate an account

  Background:
    Given a workspace account "Alex" is created
    And a workspace account "Bill" is created

  Scenario: Migrate an account to another subscription
    Given "Alex" migrates "Bill"'s account to his subscription as "DEVELOPER"
    Then "Bill"'s account is under "Alex"'s subscription

  Scenario: Cannot migrate account when lacking permissions over the subscription
    Given "Alex" provision "Charlie" as a new user with role "ADMIN"
    When "Charlie" migrates "Bill"'s account to his subscription as "DEVELOPER"
    Then the account is not migrated
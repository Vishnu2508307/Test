Feature: Listing users who have access to a subscription

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    And "Alice" has created a "Sales" team

  Scenario: it should fetch all the collaborators for a subscription
    Given "Alice" has granted "REVIEWER" permission level over the subscription to "account"
      | Bob |
    And "Alice" has granted "CONTRIBUTOR" permission level over the subscription to "team"
      | Sales |
    When "Alice" fetches the collaborators for the subscription
    Then the subscription collaborators list contains
      | Alice  | account |
      | Bob    | account |
      | Sales  | team    |

  Scenario: it should fetch the collaborators for a subscription with a limit
    Given "Alice" has granted "REVIEWER" permission level over the subscription to "account"
      | Bob |
    And "Alice" has granted "CONTRIBUTOR" permission level over the subscription to "team"
      | Sales |
    When "Alice" fetches the collaborators for the subscription with limit 1
    Then the subscription collaborators list contains
      | Sales  | team |

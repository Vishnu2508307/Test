Feature: Listing teams

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Bob" is created
    Given "Alice" has created a "Marketing" team
    And "Alice" has created a "Development" team

  Scenario: A workspace user should be able to list all the team that belongs to his/her own subscription
    When "Alice" lists the teams in the subscription
    Then the following subscription teams are returned
      | Marketing |
      | Development |

  Scenario: A workspace user should be able to list all the team that he/she is part of
    Given "Alice" adds "Bob" to the "Marketing" team as "CONTRIBUTOR"
    When "Bob" lists the teams
    Then the following teams are returned
      | Marketing |

  Scenario: A workspace user will see an empty list when there are no teams in the subscription
    When "Bob" lists the teams in the subscription
    Then an empty subscription list of teams is returned

  Scenario: A workspace user will see an empty list when he/she is not part of any team
    When "Bob" lists the teams
    Then an empty list of teams is returned

  Scenario: A workspace user should not see the team he/she has been removed from
    Given "Alice" adds "Bob" to the "Marketing" team as "CONTRIBUTOR"
    And "Alice" adds "Bob" to the "Development" team as "CONTRIBUTOR"
    When "Bob" lists the teams
    Then the following teams are returned
      | Marketing |
      | Development |
    When "Alice" revokes "Bob"'s "Marketing" team permission
    And "Bob" lists the teams
    Then the following teams are returned
      | Development |

  Scenario: Only a user with reviewer or higher permission level over the subscription should be able to list the teams
    Given "Alice" adds "Bob" to the "Marketing" team as "CONTRIBUTOR"
    And "Alice" adds "Bob" to the "Development" team as "CONTRIBUTOR"
    And "Alice" creates account "Charlie" with workspace role
    When "Charlie" lists the teams in the subscription
    Then the teams are not listed due to missing permission level
    When "Alice" has granted "REVIEWER" permission level over the subscription to "account"
      | Charlie |
    And "Charlie" lists the teams in the subscription
    Then the following subscription teams are returned
      | Marketing |
      | Development |

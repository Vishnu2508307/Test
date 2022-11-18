Feature: Fetch account summary

  Background:
    Given a workspace account "Alice" is created with details
    Given a workspace account "Bob" is created with details
    Given a workspace account "Charlie" is created with details
    And an ies account "John" is provisioned
    And an ies account "Alex" is provisioned

  Scenario: A user can able to fetch workspace account details
    When "Alice" fetches following account details
      | Bob     |
      | Charlie |
    Then account details are successfully fetched

  Scenario: A user can able to fetch ies account details
    When "Alice" fetches following account details
      | John |
      | Alex |
    Then account details are successfully fetched



Feature: Subscription/Unsubscription to publication job status

  Scenario: User can subscribe/unsubscribe to a publication job status
    Given a workspace account "Alice" is created
    Then "Alice" subscribe to the publication "Test publication" successfully
    Then "Alice" unsubscribe to the publication "Test publication" successfully

  Scenario: User cannot subscribe to a missing publication
    Given a workspace account "Alice" is created
    Then "Alice" cannot subscribe to the missing publication "Test publication"

  Scenario: User cannot unsubscribe to a missing publication
    Given a workspace account "Alice" is created
    Then "Alice" cannot unsubscribe to the missing publication "Test publication"

Feature: Create a Feedback

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And "Alice" has created workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: The user should get an error message on feedback creation if an interactive does not exist
    When "Alice" creates a feedback for random interactive
    Then feedback creation fails with message "Unauthorized: Unauthorized permission level" and code 401

  Scenario: User should be able to create a feedback and save configuration for it
    Given "Alice" has created activity "activity" inside project "TRO"
    And "Alice" has created a pathway
    And "Alice" has created an interactive
    When "Alice" has created a feedback for the interactive
    Then the feedback can be successfully fetched
    When "Alice" has saved configuration for the feedback
    """
    {"title":"Feedback", "desc":"Feedback Description"}
    """
    Then the feedback can be successfully fetched with config
    """
    {"title":"Feedback", "desc":"Feedback Description"}
    """

  Scenario: User should be able to see created feedback as a child for interactive
    Given "Alice" has created activity "activity" inside project "TRO"
    And "Alice" has created a pathway
    And "Alice" has created an interactive
    When "Alice" has created a feedback for the interactive
    Then the interactive has this feedback as a child

  Scenario: User should be able to delete feedback
    Given "Alice" has created activity "activity" inside project "TRO"
    And "Alice" has created a pathway
    And "Alice" has created an interactive
    And "Alice" has created a feedback for the interactive
    When "Alice" has deleted a feedback
    Then the interactive does not have this feedback as a child
    And "Alice" can not fetch feedback due to error: code 401 message "Unauthorized: Unauthorized permission level"

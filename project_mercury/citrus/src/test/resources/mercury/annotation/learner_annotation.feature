Feature: Test the learner annotation api

  Background:
    Given a workspace account "Alice" is created
    And "Alice" has created and published course plugin
    And an ies account "Bob" is provisioned
    And an ies account "Charlie" is provisioned
    And "Alice" has created workspace "one"
    And "Alice" has created a cohort in workspace "one"
    And "Alice" has created and published course plugin
    And "Alice" has created project "TRO" in workspace "one"

  Scenario: It should allow a cohort instructor to create a deployment annotation
    And "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    When "Alice" creates a deployment "commenting" annotation for element "SCREEN_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the deployment annotation "ONE" is created succesfully

  Scenario: It should allow an enrolled student to create a deployment annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    When "Bob" creates a deployment "commenting" annotation for element "SCREEN_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the deployment annotation "ONE" is created succesfully

  Scenario: It should not allow a user with no permission to create a deployment annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    Given an ies account "Diana" is provisioned
    When "Diana" creates a deployment "commenting" annotation for element "SCREEN_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the deployment annotation is not created due to missing permission level

  Scenario: It should allow either enrolled student or instructor to fetch deployment annotations
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    And "Charlie" autoenroll to cohort "cohort"
    Given "Bob" has created a deployment "commenting" annotation "ONE" for element "SCREEN_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Charlie" has created a deployment "commenting" annotation "TWO" for element "LINEAR_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Charlie" has created a deployment "commenting" annotation "THREE" for element "UNIT_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Charlie" fetches deployment account annotation by deployment "DEPLOYMENT_ONE", motivation "commenting" and element "UNIT_ONE"
    Then the following deployment account annotations are returned
      | THREE |
    When "Alice" fetches deployment account annotation by deployment "DEPLOYMENT_ONE" and motivation "commenting"
    Then the following deployment account annotations are returned
      | ONE   |
      | TWO   |
      | THREE |

  Scenario: It should allow instructor or enrolled student to fetch deployment annotations without account
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    And "Charlie" autoenroll to cohort "cohort"
    Given "Bob" has created a deployment "identifying" annotation "ONE" for element "SCREEN_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Charlie" has created a deployment "identifying" annotation "TWO" for element "LINEAR_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Charlie" has created a deployment "identifying" annotation "THREE" for element "UNIT_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Charlie" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "identifying"
    Then the following deployment annotations are returned
      | ONE   |
      | TWO   |
      | THREE |
    When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "identifying"
    Then the following deployment annotations are returned
      | ONE   |
      | TWO   |
      | THREE |

  Scenario: It should not allow instructor or enrolled student to fetch deployment annotations without account for unsupported motivations
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    And "Charlie" autoenroll to cohort "cohort"
    Given "Bob" has created a deployment "commenting" annotation "ONE" for element "SCREEN_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Charlie" has created a deployment "commenting" annotation "TWO" for element "LINEAR_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    And "Charlie" has created a deployment "commenting" annotation "THREE" for element "UNIT_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    When "Charlie" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "commenting"
    Then the deployment annotation is not returned due to unsupported motivation
    When "Alice" fetches deployment annotation by deployment "DEPLOYMENT_ONE" and motivation "commenting"
    Then the deployment annotation is not returned due to unsupported motivation

    # RTM message

  Scenario: It should not allow a user with no permission to create a deployment annotation
    Given "Alice" has created activity "SAMPLE_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "SAMPLE_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "SAMPLE_ONE" activity to "DEPLOYMENT_ONE"
    When "Bob" creates a deployment "commenting" annotation for element "SAMPLE_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the deployment annotation is not created through RTM due to missing permission level

  Scenario: It should allow a cohort instructor to create and update a deployment annotation
    Given "Alice" has created activity "SAMPLE_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "SAMPLE_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "SAMPLE_ONE" activity to "DEPLOYMENT_ONE"
    When instructor "Alice" creates a deployment "commenting" annotation for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the deployment annotation "ONE" is created succesfully through RTM
    Then "Alice" can list following fields from deployment annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" created by "Alice"
      | motivation | commenting |
      | target     | target     |
      | body       | body       |
    When instructor "Alice" updates a deployment annotation "ONE" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then the deployment annotation "ONE" is updated successfully
    Then "Alice" can list following fields from deployment annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" created by "Alice"
      | motivation | commenting |
      | target     | target1    |
      | body       | body1      |

  Scenario: It should allow an enrolled student to create a deployment annotation
    Given "Alice" has created activity "SAMPLE_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "SAMPLE_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "SAMPLE_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    When "Bob" creates a deployment "commenting" annotation for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the deployment annotation "ONE" is created succesfully through RTM
    Then "Alice" can list following fields from deployment annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" created by "Bob"
      | motivation | commenting |
      | target     | target     |
      | body       | body       |

  Scenario: A user with no permission should not be able to fetch a deployment annotation
    Given "Alice" has created activity "SAMPLE_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "SAMPLE_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "SAMPLE_ONE" activity to "DEPLOYMENT_ONE"
    When instructor "Alice" creates a deployment "commenting" annotation for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the deployment annotation "ONE" is created succesfully through RTM
    When "Charlie" tries to fetch fields from deployment annotation "one" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" created by "Alice"
    Then the deployment annotation fetch fails due to missing permission level

  Scenario: An enrolled user should not be able to update a deployment annotation
    Given "Alice" has created activity "SAMPLE_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "SAMPLE_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "SAMPLE_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    And "Charlie" autoenroll to cohort "cohort"
    When "Bob" creates a deployment "commenting" annotation for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the deployment annotation "ONE" is created succesfully through RTM
    When "Charlie" updates a deployment annotation "ONE" with motivation "commenting" for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target1"} |
      | body   | {"json":"body1"}   |
    Then the deployment annotation update fails due to missing permission level

  Scenario: It should allow a owner to delete a deployment annotation
    Given "Alice" has created activity "SAMPLE_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "SAMPLE_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "SAMPLE_ONE" activity to "DEPLOYMENT_ONE"
    When instructor "Alice" creates a deployment "commenting" annotation for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the deployment annotation "ONE" is created succesfully through RTM
    When instructor "Alice" deletes deployment annotation "ONE"
    Then the learner annotation "ONE" is deleted successfully

  Scenario: It should not allow an enrolled student to delete a deployment annotation
    Given "Alice" has created activity "SAMPLE_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "SAMPLE_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "SAMPLE_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    When instructor "Alice" creates a deployment "commenting" annotation for element "SCREEN_ONE" of type "INTERACTIVE" and deployment "DEPLOYMENT_ONE" with
      | target | {"json":"target"} |
      | body   | {"json":"body"}   |
    Then the deployment annotation "ONE" is created succesfully through RTM
    When "Bob" deletes deployment annotation "ONE"
    Then the learner annotation delete fails for "ONE" due to missing permission level

  Scenario: It should allow a cohort instructor to delete a deployment annotation
    Given "Alice" has created activity "UNIT_ONE" inside project "TRO"
    And "Alice" has created a "LINEAR_ONE" pathway for the "UNIT_ONE" activity
    And "Alice" has created a "SCREEN_ONE" interactive for the "LINEAR_ONE" pathway
    And "Alice" has published "UNIT_ONE" activity to "DEPLOYMENT_ONE"
    And "Bob" autoenroll to cohort "cohort"
    When "Bob" creates a deployment "commenting" annotation for element "SCREEN_ONE" and deployment "DEPLOYMENT_ONE" with
      | target | {\"json\":\"target\"} |
      | body   | {\"json\":\"body\"}   |
    Then the deployment annotation "ONE" is created succesfully
    When instructor "Alice" deletes deployment annotation "ONE"
    Then the learner annotation "ONE" is deleted successfully


#Feature: Log learnspace plugins (learner.plugin.log)
#
# These tests shouldn't be run in the pipeline until fixed
#  Background:
#    Given a workspace account "Alice" is created
#    And "Alice" has created workspace "one"
#    And "Alice" has created a plugin with name "TextInput"
#
#  Scenario: User should not be able to log learn interactive through incomplete RTM
#    When "Alice" logs the learner plugin "TextInput" with missing data
#    Then mercury should respond with: "learner.plugin.log.error"
#
#  Scenario: User should be able to log learn interactive through RTM
#    When "Alice" logs the learner plugin "TextInput"
#    Then mercury should respond with: "learner.plugin.log.ok"

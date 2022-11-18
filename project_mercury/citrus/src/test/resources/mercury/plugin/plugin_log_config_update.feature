#Feature: Plugin log config update (plugin.log.config.update)
#
# These tests shouldn't be run in the pipeline until fixed
#  Background:
#    Given a support role account "Alice" exists
#    And an account "Bob" is created
#
#  Scenario: A non support role user should not be able to update log config through RTM
#    When "Bob" updates plugin log config
#    Then mercury should respond with: "plugin.log.config.update.error" and code "401" and reason "Unauthorized: Forbidden"
#
#  Scenario: User should not be able to update log config through incomplete RTM
#    When "Alice" updates plugin log config with missing data
#    Then mercury should respond with: "plugin.log.config.update.error" and code "400" and reason "missing tableName"
#
#  Scenario: User should not be able to update log config through wrong RTM values
#    When "Alice" updates plugin log config with wrong data
#    Then mercury should respond with: "plugin.log.config.update.error" and code "400" and reason "maxRecordCount is more than or equal 50000"
#
#  Scenario: A support role user should be able to update log config through RTM
#    When "Alice" updates plugin log config
#    Then mercury should respond with: "plugin.log.config.update.ok"

Feature: Granting and revoking plugin permission

  Background:
    Given a workspace account "Bob" is created
    And a workspace account "Tom" is created
    And a workspace account "Jack" is created

  Scenario: Grant a plugin permission not authorized
    Given "Bob" has created a plugin
    When "Tom" tries sharing this plugin with "Jack" as "CONTRIBUTOR"
    Then the plugin is not shared due to missing permission level

  Scenario: Grant a contributor permission over a plugin
    Given "Bob" has created a plugin
    When "Bob" shares this plugin with "Tom" as "CONTRIBUTOR"
    Then "Tom" can share this plugin with "Jack"

  Scenario: Grant a reviewer permission over a plugin
    Given "Bob" has created a plugin
    When "Bob" shares this plugin with "Tom" as "REVIEWER"
    Then "Tom" cannot share the plugin with "Jack"

  Scenario: It should not allow to grant a plugin permission is higher than the requester
    Given "Bob" has created a plugin
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    When "Tom" tries sharing this plugin with "Jack" as "OWNER"
    Then the plugin is not shared due to missing permission level

  Scenario: Revoke a plugin permission
    Given "Bob" has created a plugin
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    When "Bob" revokes "Tom"'s plugin permission
    Then "Tom" cannot share the plugin with "Jack"

  Scenario: Revoke a plugin permission equal permission level
    Given "Bob" has created a plugin
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    And "Bob" has shared a plugin with "Jack" as "CONTRIBUTOR"
    Then "Tom" can revoke "Jack"'s permission

  Scenario: Revoke a plugin permission higher permission level
    Given "Bob" has created a plugin
    And "Bob" has shared a plugin with "Tom" as "OWNER"
    And "Bob" has shared a plugin with "Jack" as "CONTRIBUTOR"
    Then "Tom" can revoke "Jack"'s permission

  Scenario: Revoke a plugin permission lower permission level
    Given "Bob" has created a plugin
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    And "Bob" has shared a plugin with "Jack" as "OWNER"
    When "Tom" tries revoking "Jack"'s permission
    Then the permission is not revoked due to unauthorized permission level

  #Sharing with teams
  Scenario: It should not allow to grant a team with plugin permission higher than the requester's permission
    Given "Bob" has created a plugin
    And "Bob" has created a "Marketing" team
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    When "Tom" tries sharing this plugin with "Marketing" team as "OWNER"
    Then the plugin is not shared due to missing permission level

  Scenario: Revoke a team plugin permission with lower permission level
    Given "Bob" has created a plugin
    And "Bob" has created a "Marketing" team
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    And "Bob" has shared a plugin with "Marketing" team as "OWNER"
    When "Tom" tries revoking "Marketing" team's permission
    Then the permission is not revoked due to unauthorized permission level

  Scenario: It should allow to grant/revoke plugin permission using the highest permission level
    Given "Bob" has created a plugin
    And "Bob" has created a "Marketing" team
    And "Bob" adds "Jack" to the "Marketing" team as "REVIEWER"
    And "Bob" has shared a plugin with "Marketing" team as "CONTRIBUTOR"
    When "Jack" shares this plugin with "Tom" as "CONTRIBUTOR"
    Then "Tom" can share this plugin with "Jack"

  Scenario: It should allow revoking an account plugin permission using an equal or higher team plugin permission
    Given "Bob" has created a plugin
    And "Bob" has created a "Marketing" team
    And "Bob" adds "Jack" to the "Marketing" team as "REVIEWER"
    And "Bob" adds "Tom" to the "Marketing" team as "OWNER"
    And "Bob" has shared a plugin with "Marketing" team as "CONTRIBUTOR"
    And "Bob" has shared a plugin with "Tom" as "REVIEWER"
    When "Jack" tries revoking "Tom"'s permission
    Then the plugin permission is successfully revoked

  Scenario: It should allow revoking a team plugin permission using an equal or higher account plugin permission
    Given "Bob" has created a plugin
    And "Bob" has created a "Marketing" team
    And "Bob" adds "Jack" to the "Marketing" team as "OWNER"
    And "Bob" has shared a plugin with "Marketing" team as "CONTRIBUTOR"
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    When "Tom" tries revoking "Marketing" team's permission
    Then the plugin permission is successfully revoked

  Scenario: It should not allow to override an existing permission when the requester has lower permission level
    Given "Bob" has created a plugin
    And "Bob" has shared a plugin with "Tom" as "CONTRIBUTOR"
    When "Tom" tries sharing this plugin with "Bob" as "CONTRIBUTOR"
    Then the plugin is not shared due to missing permission level

  Scenario: It should allow to override an existing permission when the requester has an higher permission level
    Given "Bob" has created a plugin
    And "Bob" has shared a plugin with "Tom" as "OWNER"
    When "Tom" shares this plugin with "Bob" as "CONTRIBUTOR"
    Then "Tom" can share this plugin with "Jack"



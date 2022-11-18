Feature: Create and delete asset signature configurations

  Background:
    Given a support role account "Alice" exists
    And an account "Chuck" is created

  Scenario: It should allow a SUPPORT role user to create asset signature configuration
    When "Alice" creates asset signature for host "https://url.tdl" path "/foo" with type "AKAMAI_TOKEN_AUTHENTICATION" and config
    """
    {"tokenName": "a token", "key": "secretKey"}
    """
    Then the "AKAMAI_ONE" asset signature configuration is successfully created

  Scenario: It should allow a SUPPORT role user to delete asset signature configuration
    Given "Alice" has created "AKAMAI_ONE" asset signature for host "https://url.tdl" path "/foo" with type "AKAMAI_TOKEN_AUTHENTICATION" and config
    """
    {"tokenName": "a token", "key": "secretKey"}
    """
    When "Alice" deletes "AKAMAI_ONE" asset signature configuration
    Then the asset signature configuration is deleted

  Scenario: It should allow a SUPPORT role user to create/delete asset signature configuration with an empty path
    Given "Alice" has created "AKAMAI_ONE" asset signature for host "https://url.tdl" path "" with type "AKAMAI_TOKEN_AUTHENTICATION" and config
    """
    {"tokenName": "a token", "key": "secretKey"}
    """
    When "Alice" deletes "AKAMAI_ONE" asset signature configuration
    Then the asset signature configuration is deleted

  Scenario: It should not allow a non support role user to create asset signature configuration
    When "Chuck" creates asset signature for host "https://url.tdl" path "" with type "AKAMAI_TOKEN_AUTHENTICATION" and config
    """
    {"tokenName": "a token", "key": "secretKey"}
    """
    Then the asset signature configuration is not "create" due to missing permission level

  Scenario: It should not allow a non support role user to delete asset signature configuration
    Given "Alice" has created "AKAMAI_ONE" asset signature for host "https://url.tdl" path "/foo" with type "AKAMAI_TOKEN_AUTHENTICATION" and config
    """
    {"tokenName": "a token", "key": "secretKey"}
    """
    When "Chuck" deletes "AKAMAI_ONE" asset signature configuration
    Then the asset signature configuration is not "delete" due to missing permission level

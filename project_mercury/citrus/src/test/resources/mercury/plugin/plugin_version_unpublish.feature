Feature: Unpublishing the plugin version

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Chuck" is created

  Scenario: User can not unpublish a plugin version without owner permission
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    And "Alice" has published version "1.2.2" for "TextInput" plugin
    When "Chuck" unpublishes version "1.2.2" for "TextInput" plugin
    Then the plugin version unpublish fails due to invalid permission level

  Scenario: Plugin owner can unpublish the latest version successfully
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    And "Alice" has published version "1.2.2" for "TextInput" plugin
    When "Alice" unpublishes version "1.2.2" for "TextInput" plugin
    Then the "TextInput" plugin version unpublished successfully to latest version "1.2.1"

  Scenario: Plugin owner can unpublish the older version successfully
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    And "Alice" has published version "1.2.2" for "TextInput" plugin
    When "Alice" unpublishes version "1.2.0" for "TextInput" plugin
    Then the "TextInput" plugin version unpublished successfully to latest version "1.2.2"

  Scenario: Plugin owner can unpublish any version successfully
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    And "Alice" has published version "1.2.2" for "TextInput" plugin
    And "Alice" has published version "1.2.3" for "TextInput" plugin
    And "Alice" has published version "1.2.4" for "TextInput" plugin
    And "Alice" has unpublished version "1.2.1" for "TextInput" plugin
    And "Alice" has unpublished version "1.2.3" for "TextInput" plugin
    When "Alice" unpublishes version "1.2.4" for "TextInput" plugin
    Then the "TextInput" plugin version unpublished successfully to latest version "1.2.2"

  Scenario: Plugin owner can unpublish the version successfully
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    When "Alice" unpublishes version "1.2.0" for "TextInput" plugin
    Then the "TextInput" plugin version unpublished successfully to latest version "1.2.0"
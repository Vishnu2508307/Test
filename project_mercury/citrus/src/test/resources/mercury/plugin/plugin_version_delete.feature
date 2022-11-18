Feature: Deleting the plugin version

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Chuck" is created

  Scenario: User can not delete a plugin version without owner permission
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    And "Alice" has published version "1.2.2" for "TextInput" plugin
    When "Chuck" tries to delete version "1.2.2" for "TextInput" plugin
    Then the plugin version delete fails due to invalid permission level

  Scenario: User can not delete a plugin version without unpublishing
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    And "Alice" has published version "1.2.2" for "TextInput" plugin
    When "Alice" tries to delete version "1.2.2" for "TextInput" plugin
    Then the plugin version delete fails with message "Unpublishing the plugin version is required to delete" and code "405"

  Scenario: User can not delete a plugin version if the direct version is referenced to a courseware element
    Given "Alice" has created and published "TextInput" plugin of type "unit" with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin of type "unit"
    And "Alice" has published version "1.2.2" for "TextInput" plugin of type "unit"
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT" inside project "TRO" with plugninName "TextInput"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created an activity "LESSON" for the "LINEAR" pathway with plugin "TextInput" with version "1.2.2"
    And "Alice" has unpublished version "1.2.2" for "TextInput" plugin
    When "Alice" tries to delete version "1.2.2" for "TextInput" plugin
    Then the plugin version delete fails with message "Plugin version has been referenced to courseware element" and code "405"

  Scenario: User can delete a plugin version if it is referenced and there is a fallback version
    Given "Alice" has created and published "TextInput" plugin of type "unit" with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin of type "unit"
    And "Alice" has published version "1.2.2" for "TextInput" plugin of type "unit"
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT" inside project "TRO" with plugninName "TextInput"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created an activity "LESSON" for the "LINEAR" pathway with plugin "TextInput" with version "1.2.*"
    And "Alice" has unpublished version "1.2.2" for "TextInput" plugin
    When "Alice" tries to delete version "1.2.2" for "TextInput" plugin
    Then the plugin version deleted successfully

  Scenario: User can not delete a plugin version if it is referenced to a courseware element
    Given "Alice" has created and published "TextInput" plugin of type "course" with version "1.2.0"
    And "Alice" has published version "1.3.1" for "TextInput" plugin of type "course"
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT" inside project "TRO" with plugninName "TextInput"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created an activity "LESSON" for the "LINEAR" pathway with plugin "TextInput" with version "1.3.*"
    And "Alice" has unpublished version "1.3.1" for "TextInput" plugin
    When "Alice" tries to delete version "1.3.1" for "TextInput" plugin
    Then the plugin version delete fails with message "Plugin version has been referenced to courseware element with 1.3.* version" and code "405"

  Scenario: User can delete a plugin version if it is referenced and there is a fallback version
    Given "Alice" has created and published "TextInput" plugin of type "course" with version "1.2.0"
    And "Alice" has published version "1.3.1" for "TextInput" plugin of type "course"
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT" inside project "TRO" with plugninName "TextInput"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created an activity "LESSON" for the "LINEAR" pathway with plugin "TextInput" with version "1.*"
    And "Alice" has unpublished version "1.3.1" for "TextInput" plugin
    When "Alice" tries to delete version "1.3.1" for "TextInput" plugin
    Then the plugin version deleted successfully

  Scenario: User can not delete a plugin version if it is referenced and there is no fallback version
    Given "Alice" has created and published "TextInput" plugin of type "unit" with version "1.2.0"
    And "Alice" has created workspace "one"
    And "Alice" has created project "TRO" in workspace "one"
    And "Alice" has created activity "UNIT" inside project "TRO" with plugninName "TextInput"
    And "Alice" has created a "LINEAR" pathway for the "UNIT" activity
    And "Alice" has created an activity "LESSON" for the "LINEAR" pathway with plugin "TextInput" with version "1.*"
    And "Alice" has unpublished version "1.2.0" for "TextInput" plugin
    When "Alice" tries to delete version "1.2.0" for "TextInput" plugin
    Then the plugin version delete fails with message "Plugin version has been referenced to courseware element with 1.* version" and code "405"

  Scenario: User can delete a plugin version successfully when there are no references
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    And "Alice" has published version "1.2.2" for "TextInput" plugin
    And "Alice" has unpublished version "1.2.2" for "TextInput" plugin
    When "Alice" tries to delete version "1.2.2" for "TextInput" plugin
    Then the plugin version deleted successfully

Feature: Fetching a list of published plugins visible for a user (author.plugin.list)

  Background:
    Given workspace accounts are created
      | Alice |
      | Bob   |

  Scenario: User can see only published plugins in author.plugin.list
    When "Alice" has created a plugin with name "TextInput"
    Then "Alice" can not see any plugin in the list of published plugins
    When "Alice" has published version "1.0.0" for "TextInput" plugin
    Then "Alice" can see in the list of published plugins
      | TextInput | 1.0.0 |

  Scenario: A user can see published plugins shared with him
    Given "Alice" has created and published "TextInput" plugin with version "1.0.0"
    When "Alice" has shared "TextInput" plugin with "Bob" as "REVIEWER"
    Then "Bob" can see in the list of published plugins
      | TextInput | 1.0.0 |

  Scenario: A user can see published plugins shared with his team
    Given "Alice" has created and published "TextInput" plugin with version "1.0.0"
    And "Alice" has created a "Development" team
    And "Alice" adds "Bob" to the "Development" team as "REVIEWER"
    When "Alice" has shared plugin "TextInput" with team "Development" as "REVIEWER"
    Then "Bob" can see in the list of published plugins
      | TextInput | 1.0.0 |

  Scenario: A user can filter published plugins by type
    When "Alice" has created and published "TextInput" plugin with version "1.0.0" and type "component"
    And "Alice" has created and published "Lesson" plugin with version "1.0.0" and type "lesson"
    Then "Alice" can see in the list of published plugins filtered by type "component"
      | TextInput | 1.0.0 |
    Then "Alice" can see in the list of published plugins filtered by type "lesson"
      | Lesson | 1.0.0 |

  Scenario: A user can see the latest published version of the plugin in a list
    When "Alice" has created a plugin with name "TextInput"
    And "Alice" has published version "1.0.0" for "TextInput" plugin
    And "Alice" has published version "2.0.0" for "TextInput" plugin
    Then "Alice" can see in the list of published plugins
      | TextInput | 2.0.0 |

  Scenario: A user can list published plugin if matching plugin filters of type 'ID' are found
    When "Alice" has created and published "TextInput" plugin with version "1.0.0" and type "component"
    And "Alice" has created and published "Lesson" plugin with version "1.0.0" and type "lesson"
    Then "Alice" tries to list published plugins filtered by type "component" and filters
      | pluginName   | TextInput     |
      | version      | 1.0.0         |
      | filterType   | ID            |
      | filterValues | TextInput     |
    Then "Alice" list following published plugins filtered by type "component" and plugin filters
      | TextInput | 1.0.0 |

  Scenario: A user cannot list published plugin if no matching plugin filters of type 'ID' are found
    When "Alice" has created and published "TextInput" plugin with version "1.0.0" and type "component"
    And "Alice" has created and published "Lesson" plugin with version "1.0.0" and type "lesson"
    When "Alice" tries to list published plugins filtered by type "component" and filters
      | pluginName   | TextInput  |
      | version      | 1.0.0      |
      | filterType   | ID         |
      | filterValues | Lesson     |
    Then "Alice" list empty published plugins filtered by type "component" and plugin filters

  Scenario: A user can list published plugin if matching plugin filters of type 'TAGS' are found
    When "Alice" has created and published eText "eTextInput" plugin with version "1.0.0" and type "component"
    And "Alice" has created and published eText "Lesson" plugin with version "1.0.0" and type "lesson"
    Then "Alice" tries to list published plugins filtered by type "component" and filter values
      | pluginName   | eTextInput     |
      | version      | 1.0.0         |
      | filterType   | TAGS            |
      | filterValues | eTextAllowed     |
    Then "Alice" list following published plugins filtered by type "component" and plugin filters
      | eTextInput | 1.0.0 |

  Scenario: A user cannot list published plugin if no matching plugin filters of type 'TAGS' are found
    When "Alice" has created and published eText "eTextInput" plugin with version "1.0.0" and type "component"
    And "Alice" has created and published eText "Lesson" plugin with version "1.0.0" and type "lesson"
    When "Alice" tries to list published plugins filtered by type "component" and filter values
      | pluginName   | eTextInput  |
      | version      | 1.0.0      |
      | filterType   | TAGS         |
      | filterValues | Lesson     |
    Then "Alice" list empty published plugins filtered by type "component" and plugin filters

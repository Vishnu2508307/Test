Feature: Plugin Publishing and Fetching

  Background:
    Given a workspace account "Alice" is created
    And a workspace account "Chuck" is created

  Scenario: User is able to create and fetch plugin
    Given "Alice" has created a plugin with name "Citrus Plugin"
    Then "Alice" can fetch "Citrus Plugin" plugin summary in workspace

  Scenario: User is able to create plugin with supplied id
    When "Alice" creates a plugin with a supplied id
    Then the plugin is successfully created with the supplied id

  Scenario: User can not create plugin with same id if id already exists
    Given "Alice" has created a plugin with a supplied id
    When "Alice" creates a plugin with the same supplied id as before
    Then the plugin is not created due to conflict

#  Scenario: Successfully publish and upload plugin
#    Given "Alice" has created a plugin with name "Citrus Plugin"
#    When "Alice" publishes version for "Citrus Plugin" plugin with values
#      | name        | Citrus Plugin                                         |
#      | version     | 1.2.0                                                 |
#      | description | Sample Plugin for testing in Citrus integration tests |
#    Then the "Citrus Plugin" plugin is published and uploaded successfully with values
#      | name        | Citrus Plugin                                         |
#      | version     | 1.2.0                                                 |
#      | description | Sample Plugin for testing in Citrus integration tests |

  Scenario: User can not publish plugin with the same version
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    When "Alice" publishes version "1.2.0" for "TextInput" plugin
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "New version '1.2.0' for plugin '${plugin_id_TextInput}' should be greater than the existing '1.2.0'"

  Scenario: User can list all versions for plugin
    Given "Alice" has created and published "TextInput" plugin with version "1.2.0"
    And "Alice" has published version "1.2.1" for "TextInput" plugin
    Then "Alice" can list "TextInput" plugin versions
      | 1.2.1 |
      | 1.2.0 |

  Scenario: Authorization error is thrown when invalid dev key is used to publish plugin
    Given "Alice" has created a plugin with name "Citrus Plugin"
    When "Alice" publishes version "1.2.0" for "Citrus Plugin" plugin with invalid dev key
    Then mercury should respond with http status "401" "NOT_AUTHORIZED" and error message "Invalid developer key supplied"

  Scenario: Bad request error is thrown when manifest is not presented
    Given "Alice" has created a plugin with name "Citrus Plugin"
    When "Alice" publishes version "1.2.0" for "Citrus Plugin" plugin without a manifest file
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "manifest.json file missing"

  Scenario: Bad request error is thrown when a plugin id is not specified
    Given "Chuck" has created a plugin with name "Citrus Plugin"
    When "Chuck" publishes version "1.2.0" for "Citrus Plugin" plugin without an id specified
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "missing required plugin id"

  Scenario: Bad request error is thrown when plugin is not created before publishing
    When "Alice" publishes version "1.2.0" for invalid plugin
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "@startsWith('Plugin summary not found for id')@"

  Scenario: Unprocessable entity error is thrown when publishing plugin with invalid schema
    When "Alice" publishes version "1.2.0" with invalid schema plugin
    Then mercury should respond with http status "UNPROCESSABLE_ENTITY"

  # This one always fails assertion in local, but works fine in sandbox
  Scenario: Publishing a plugin should update name, null type and description
    Given "Alice" has created a plugin with name "Cosmic"
    When "Alice" has published version for "Cosmic" plugin with values
      | name        | The cosmic game |
      | version     | 1.2.0           |
      | description | wow             |
      | type        | screen          |
    Then "Alice" can fetch "Cosmic" plugin info in author with values
      | name        | The cosmic game |
      | version     | 1.2.0           |
      | description | wow             |
      | type        | screen          |

  Scenario: Publishing a plugin should succeed when manifest type matches the summary type
    Given "Alice" has created a plugin with name "Cosmic" and type "screen"
    Then "Alice" can publish version for "Cosmic" plugin with values
      | name        | The cosmic game |
      | version     | 1.2.0           |
      | description | wow             |
      | type        | screen          |

  Scenario: Publishing a plugin should fail when there is a type mismatch
    Given "Alice" has created a plugin with name "Cosmic" and type "pathway"
    When "Alice" publishes version for "Cosmic" plugin with values
      | name        | The cosmic game |
      | version     | 1.2.0           |
      | description | wow             |
      | type        | screen          |
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "Plugin type must be the same. Found `screen` expected `pathway`."

#  Scenario: User should be able to override pluginId by parameter in the URL
#    Given "Alice" has created a plugin with name "Citrus Plugin Dev"
#    And "Alice" has created a plugin with name "Citrus Plugin Prod"
#    When "Alice" publishes version for "Citrus Plugin Dev" plugin with pluginId for "Citrus Plugin Prod" and values
#      | name    | Citrus Plugin Prod |
#      | version | 1.0.0              |
#    Then the "Citrus Plugin Prod" plugin is published and uploaded successfully with values
#      | name    | Citrus Plugin Prod |
#      | version | 1.0.0              |


  Scenario: User can  publish plugin with package.json file
    Given "Alice" has created and published "TextInput" plugin with version "3.2.2" from package.json file
    When "Alice" has published version "3.2.2" for "TextInput" plugin with filename "plugin_package.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 3.2.2 |

  Scenario: Bad request error is thrown when bronte sub section is not present in package.json file
    Given "Alice" has created and published "TextInput" plugin with version "3.2.2" from package.json file
    When "Alice" publishes version "1.2.0" for "TextInput" plugin with filename "plugin_package_no_bronte.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "bronte is missing in package.json"

  Scenario: Bad request error is thrown when a plugin id is missing in bronte section of package.json
    Given "Chuck" has created a plugin with name "Citrus Plugin"
    When "Alice" publishes version "1.2.0" for "Citrus Plugin" plugin with filename "plugin_package_noPluginId.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "missing required plugin id"

  Scenario: Bad request error is thrown when a configuration schema is not specified in bronte section of package.json
    Given "Alice" has created and published "TextInput" plugin with version "2.1.1" from package.json file
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "plugin_package_schemaFieldMissing.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "configuration schema field missing in manifest"

  Scenario: Bad request error is thrown when a plugin type is not specified in bronte section of package.json
    Given "Chuck" has created a plugin with name "Citrus Plugin"
    When "Chuck" publishes version "1.2.0" for "Citrus Plugin" plugin with filename "plugin_package_noPluginType.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "type missing from manifest"

  Scenario: Bad request error is thrown when a name is not specified in bronte section of package.json
    Given "Chuck" has created a plugin with name "Citrus Plugin"
    When "Chuck" publishes version "1.2.0" for "Citrus Plugin" plugin with filename "plugin_package_noName.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "name missing from manifest"


  Scenario: User can publish plugin from package.json file which has searchable fields
    Given "Alice" has created and published "TextInput" plugin with version "2.1.1" from package.json file
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "plugin_package.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 2.1.1 |

  Scenario: User can publish plugin from manifest.json file which has searchable field
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "plugin_success.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 2.1.1 |

  Scenario: User cannot publish plugin in case searchable fields are missing in package.json file
    Given "Alice" has created and published "TextInput" plugin with version "2.1.1" from package.json file
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "plugin_package_NoContentTypeInSearchableField.zip"
    Then mercury should respond with http status "422" "UNPROCESSABLE_ENTITY" and error message "Searchable object is missing contentType field"

  Scenario: User cannot publish plugin from manifest.json file which has invalid searchable fields
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "plugin_ManifestInvalidSearchableFields.zip"
    Then mercury should respond with http status "422" "UNPROCESSABLE_ENTITY" and error message "@startsWith('Searchable field doesn't exist in configuration schema')@"

  Scenario: User can publish plugin from package.json file which has guide field
    Given "Alice" has created and published "TextInput" plugin with version "2.1.1" from package.json file
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "plugin_package.zip"
    Then "Alice" can fetch "TextInput" plugin info in author with following field values
      | name          | TextInput |
      | version       | 2.1.1     |
      | guide         | /guide.md |
      | defaultHeight | 100       |

  Scenario: User can publish plugin from manifest.json file which has guide field
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "plugin_success.zip"
    Then "Alice" can fetch "TextInput" plugin info in author with following field values
      | name            | TextInput |
      | version         | 2.1.1     |
      | guide           | /guide.md |
      | defaultHeight   | auto      |

  Scenario: User cannot publish plugin from manifest.json file which has guide field but package has no guide.md file
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "plugin_missingGuideFile.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "File guide.md not found inside the package"

  Scenario: User can publish plugin for minor version release with schema containing changed properties other than type field as that of previous schema
    Given "Alice" has created and published "TextInput" plugin with version "1.2.1"
    And "Alice" publishes version "1.3.1" for "TextInput" plugin with filename "ChangedPropertiesNotTypeField.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 1.3.1 |
      | 1.2.1 |

  Scenario: User can publish plugin for patch version release with schema containing changed properties other than type field as that of previous schema
    Given "Alice" has created and published "TextInput" plugin with version "1.2.1"
    And "Alice" publishes version "1.2.2" for "TextInput" plugin with filename "ChangedPropertiesNotTypeField.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 1.2.2 |
      | 1.2.1 |

  Scenario: User can publish plugin for minor version release irrespective of schema check if major version is less than 1
    Given "Alice" has created and published "TextInput" plugin with version "0.2.1"
    And "Alice" has published version "0.3.1" for "TextInput" plugin with filename "MinorOrPatchMissingProperties.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 0.3.1 |
      | 0.2.1 |

  Scenario: User can publish plugin for minor version release with same schema properties as before but with additional properties
    Given "Alice" has created and published "TextInput" plugin with version "1.2.1"
    And "Alice" has published version "1.3.1" for "TextInput" plugin with filename "MinorOrPatchAdditionalProperties.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 1.3.1 |
      | 1.2.1 |

  Scenario: User cannot publish plugin for minor version release with schema containing changed type field properties as that of previous schema
    Given "Alice" has created and published "TextInput" plugin with version "1.2.1"
    And "Alice" publishes version "1.3.1" for "TextInput" plugin with filename "ChangedPropertiesTypeField.zip"
    Then mercury should respond with http status "UNPROCESSABLE_ENTITY"

  Scenario: User cannot publish plugin for minor version release with schema having missing properties as that of previous schema
    Given "Alice" has created and published "TextInput" plugin with version "1.2.1"
    And "Alice" publishes version "1.3.1" for "TextInput" plugin with filename "MinorOrPatchMissingProperties.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "@startsWith('A property of type')@"

  Scenario: User can publish plugin from manifest.json file which has missing plugin name but it extract plugin name from package.json file
    Given "Alice" has created a plugin with name "Plugin Name"
    When "Alice" has published version "2.1.1" for "Plugin Name" plugin with filename "noPluginNameInManifest_extractfromPackage.zip"
    Then "Alice" can fetch "Plugin Name" plugin info with following field values
      | name    | Plugin Name |
      | version | 2.1.1       |

  Scenario: User cannot publish plugin from manifest.json if plugin name is missing from manifest.json and missing package.json files
    Given "Alice" has created and published "TextInput" plugin with version "1.2.1"
    And "Alice" publishes version "1.2.1" for "TextInput" plugin with filename "noNameInManifestMissingPackage.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "@startsWith(package.json file missing)@"

  Scenario: User cannot publish plugin from manifest.json if plugin name is missing from both manifest.json and package.json files
    Given "Alice" has created and published "TextInput" plugin with version "1.2.1"
    And "Alice" publishes version "1.2.1" for "TextInput" plugin with filename "noName_manifestAndPackage.zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "name missing from manifest"

  Scenario: User can publish plugin from manifest.json file without optional fields
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "pluginManifest_NoOptionalField.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 2.1.1 |

  Scenario: User can publish plugin with package.json file without optional fields
    Given "Alice" has created and published "TextInput" plugin with version "3.2.2" from package.json file
    When "Alice" has published version "3.2.2" for "TextInput" plugin with filename "plugin_package_NoOptionalfield.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 3.2.2 |

  Scenario: User cannot publish plugin new version as reviewer in default mode
    Given "Alice" has created and published "Cosmic" plugin with version "1.2.0" in "DEFAULT" mode
    And "Alice" shares "Cosmic" plugin with "Chuck" as "REVIEWER"
    When "Chuck" publishes version "1.2.1" for "Cosmic" plugin
    Then mercury should respond with http status "403" "FORBIDDEN" and error message "Only plugin contributor or higher permission level can publish a new version in DEFAULT mode"

  Scenario: User cann publish plugin new version as contributor in default mode
    Given "Alice" has created and published "Cosmic" plugin with version "1.2.0" in "DEFAULT" mode
    And "Alice" shares "Cosmic" plugin with "Chuck" as "CONTRIBUTOR"
    When "Chuck" publishes version "1.2.1" for "Cosmic" plugin
    Then "Alice" can list "Cosmic" plugin versions
      | 1.2.1 |
      | 1.2.0 |

  Scenario: User cannot publish plugin new version as contributor in strict mode
    Given "Alice" has created and published "Cosmic" plugin with version "1.2.0" in "STRICT" mode
    And "Alice" shares "Cosmic" plugin with "Chuck" as "CONTRIBUTOR"
    When "Chuck" publishes version "1.2.1" for "Cosmic" plugin
    Then mercury should respond with http status "403" "FORBIDDEN" and error message "Only plugin owner can publish a new version in STRICT mode"

  Scenario: User cann publish plugin new version as owner in strict mode
    Given "Alice" has created and published "Cosmic" plugin with version "1.2.0" in "STRICT" mode
    And "Alice" shares "Cosmic" plugin with "Chuck" as "OWNER"
    When "Chuck" publishes version "1.2.1" for "Cosmic" plugin
    Then "Alice" can list "Cosmic" plugin versions
      | 1.2.1 |
      | 1.2.0 |

  Scenario: User cannot publish plugin new version without valid permission
    Given "Alice" has created and published "Cosmic" plugin with version "1.2.0" in "DEFAULT" mode
    When "Chuck" publishes version "1.2.1" for "Cosmic" plugin
    Then mercury should respond with http status "403" "FORBIDDEN" and error message "User must have valid permission to publish a plugin"
    When "Alice" shares "Cosmic" plugin with "Chuck" as "CONTRIBUTOR"
    And "Chuck" publishes version "1.2.1" for "Cosmic" plugin
    Then "Alice" can list "Cosmic" plugin versions
      | 1.2.1 |
      | 1.2.0 |

  Scenario: User can update and publish the plugin with permission level as Owner
    Given "Alice" has created and published "Cosmic" plugin with version "1.2.0" in "STRICT" mode
    Then "Alice" can list "Cosmic" plugin versions
      | 1.2.0 |
    When "Alice" updates publish mode to "DEFAULT" and publishes "Cosmic" plugin with version "1.2.1"
    Then "Alice" can list "Cosmic" plugin versions
      | 1.2.1 |
      | 1.2.0 |

  Scenario: User cannot update and publish the plugin with permission level as Contributor
    Given "Alice" has created and published "Cosmic" plugin with version "1.2.0" in "DEFAULT" mode
    Then "Alice" can list "Cosmic" plugin versions
      | 1.2.0 |
    And "Alice" shares "Cosmic" plugin with "Chuck" as "CONTRIBUTOR"
    When "Chuck" tries to update publish mode to "DEFAULT" and publishes "Cosmic" plugin with version "1.2.1"
    Then the update plugin fails due to invalid permission level

  Scenario: User cannot publish plugin in case filter field has missing filter values node
    Given "Alice" has created and published "TextInput" plugin with version "2.1.1" from package.json file
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "package_missingFilterValueNode.zip"
    Then mercury should respond with http status "422" "UNPROCESSABLE_ENTITY" and error message "@startsWith('Filter object has missing values field')@"

  Scenario: User cannot publish plugin in case filter field has missing type node
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "manifest_missingFilterTypeNode.zip"
    Then mercury should respond with http status "422" "UNPROCESSABLE_ENTITY" and error message "@startsWith('Filter object has missing type field')@"

  Scenario: User cannot publish plugin in case filter field has missing type node value
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "manifest_missingFilterTypeValue.zip"
    Then mercury should respond with http status "422" "UNPROCESSABLE_ENTITY" and error message "@startsWith('Filter object has missing or empty type field value')@"

  Scenario: User cannot publish plugin in case filter field is not an array
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "manifest_filterFieldNotAnArray.zip"
    Then mercury should respond with http status "422" "UNPROCESSABLE_ENTITY" and error message "@startsWith('Field 'filters' should be an array')@"

  Scenario: User cannot publish plugin in case filter field has not valid filter type
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" publishes version "2.1.1" for "TextInput" plugin with filename "manifest_NotValidFilterType.zip"
    Then mercury should respond with http status "422" "UNPROCESSABLE_ENTITY" and error message "@startsWith('Filter object has not a valid type')@"

  Scenario: User cannot publish plugin from config.schema.json file which has removed fields of latest schema for minor version update
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "plugin_success.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 2.1.1 |
    When "Alice" publishes version "2.1.2" for "TextInput" plugin with filename "MissingPropertiesInConfigSchemaForMinorVersionUpdate..zip"
    Then mercury should respond with http status "400" "BAD_REQUEST" and error message "@startsWith('A property of type')@"

  Scenario: User can publish plugin from config.schema.json file which has new schema
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "plugin_success.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 2.1.1 |
    When "Alice" has published version "3.0.0" for "TextInput" plugin with filename "MissingPropertiesInConfigSchemaForMinorVersionUpdate..zip"
    Then "Alice" can list "TextInput" plugin versions
      | 3.0.0 |
      | 2.1.1 |

  Scenario: User can publish plugin from manifest.json file which has filterType TAGS
    Given "Alice" has created a plugin with name "TextInput"
    When "Alice" has published version "2.1.1" for "TextInput" plugin with filename "eText_plugin_success.zip"
    Then "Alice" can list "TextInput" plugin versions
      | 2.1.1 |
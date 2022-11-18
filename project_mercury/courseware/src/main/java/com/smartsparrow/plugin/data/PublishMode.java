package com.smartsparrow.plugin.data;

/**
 * Defines the publishing modes
 */
public enum PublishMode {
    /**
     * Sets the publishing mode to default behavior
     * Plugin contributor or higher can publish new plugin version
     */
    DEFAULT,
    /**
     * Sets the publishing mode to strict behavior
     * Only plugin owner can publish new version
     */
    STRICT
}
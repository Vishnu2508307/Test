package com.smartsparrow.courseware.data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Interface that allows an object to hold a reference to a plugin.
 */
public interface PluginReference extends Serializable {

    /**
     * Get the plugin id.
     *
     * @return the plugin id
     */
    UUID getPluginId();

    /**
     * Get the SemVer matcher expression for coursewareElements but a locked version for learnerElements.
     *
     * @return the SemVer matcher expression
     */
    String getPluginVersionExpr();

}

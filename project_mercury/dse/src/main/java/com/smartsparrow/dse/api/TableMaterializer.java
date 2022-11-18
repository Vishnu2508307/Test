package com.smartsparrow.dse.api;

/**
 * Marker interface for concrete classes which essentially perform select statements. 
 * 
 */
public interface TableMaterializer {

    /**
     *
     */
    default boolean isForceLocalCL() {
        return "1".equals(System.getProperty("forceLocalCL", "0"));
    }
}

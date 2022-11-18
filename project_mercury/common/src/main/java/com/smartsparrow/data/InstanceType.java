package com.smartsparrow.data;

/**
 * Describe the Mercury instance type. This is currently used to drive infrastructure separation where we require
 * certain features to be disabled based on the instance type
 */
public enum InstanceType {
    // instance type running in the learner cluster
    LEARNER,
    // instance type running in the workspace cluster
    WORKSPACE,
    // instance type running in the operations cluster
    OPERATIONS,
    // unspecified instance type, full funcionalities enabled by default
    DEFAULT
}

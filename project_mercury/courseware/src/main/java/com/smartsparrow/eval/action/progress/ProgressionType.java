package com.smartsparrow.eval.action.progress;

public enum ProgressionType {

    INTERACTIVE_REPEAT,
    INTERACTIVE_COMPLETE,
    ACTIVITY_REPEAT,
    ACTIVITY_COMPLETE,
    INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE,
    INTERACTIVE_COMPLETE_AND_GO_TO,
    ACTIVITY_COMPLETE_AND_GO_TO,
    ACTIVITY_COMPLETE_AND_PATHWAY_COMPLETE;

    /**
     * Return a boolean indicating if the interactive should be marked as completed or not
     *
     * @return true for:
     * <ul>
     *     <li>{@link this#INTERACTIVE_COMPLETE}</li>
     *     <li>{@link this#INTERACTIVE_COMPLETE_AND_GO_TO}</li>
     *     <li>{@link this#INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE}</li>
     * </ul>
     * or false for any other progression type
     */
    public boolean interactiveCompleted() {
        switch (this) {
            case INTERACTIVE_COMPLETE:
            case INTERACTIVE_COMPLETE_AND_GO_TO:
            case INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Return a boolean indicating if the activity should be marked as completed or not
     *
     * @return true for:
     * <ul>
     *     <li>{@link this#ACTIVITY_COMPLETE}</li>
     *     <li>{@link this#ACTIVITY_COMPLETE_AND_GO_TO}</li>
     *     <li>{@link this#ACTIVITY_COMPLETE_AND_PATHWAY_COMPLETE}</li>
     * </ul>
     * or false for any other progression type
     */
    public boolean activityCompleted() {
        switch (this) {
            case ACTIVITY_COMPLETE:
            case ACTIVITY_COMPLETE_AND_GO_TO:
            case ACTIVITY_COMPLETE_AND_PATHWAY_COMPLETE:
                return true;
            default:
                return false;
        }
    }

    /**
     * @return a boolean indicating if the walkable should be marked as completed or not
     */
    public boolean walkableCompleted() {
        return interactiveCompleted() || activityCompleted();
    }
}

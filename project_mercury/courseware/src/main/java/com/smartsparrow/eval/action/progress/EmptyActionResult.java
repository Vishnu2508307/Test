package com.smartsparrow.eval.action.progress;

import java.util.Objects;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionResult;

/**
 * An action result that simply returns the action that generated this result.
 */
@SuppressWarnings("rawtypes")
public class EmptyActionResult implements ActionResult<Action> {

    private final Action action;

    public EmptyActionResult(Action action) {
        this.action = action;
    }

    @Override
    public Action.Type getType() {
        return action.getType();
    }

    @Override
    public Action getValue() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmptyActionResult that = (EmptyActionResult) o;
        return Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action);
    }

    @Override
    public String toString() {
        return "EmptyActionResult{" +
                "action=" + action +
                '}';
    }
}

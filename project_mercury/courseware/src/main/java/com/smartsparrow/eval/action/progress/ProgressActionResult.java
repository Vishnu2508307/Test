package com.smartsparrow.eval.action.progress;

import java.util.ArrayList;
import java.util.List;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionResult;
import com.smartsparrow.learner.progress.Progress;

public class ProgressActionResult implements ActionResult<List<Progress>> {

    List<Progress> value = new ArrayList<>();

    @Override
    public Action.Type getType() {
        return Action.Type.CHANGE_PROGRESS;
    }

    @Override
    public List<Progress> getValue() {
        return value;
    }

    public ProgressActionResult setValue(List<Progress> value) {
        this.value = value;
        return this;
    }
}

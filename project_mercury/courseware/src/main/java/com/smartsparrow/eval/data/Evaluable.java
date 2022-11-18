package com.smartsparrow.eval.data;

import com.smartsparrow.courseware.data.EvaluationMode;

/**
 * Represents an object that can be Evaluated
 */
public interface Evaluable {

    /**
     * @return the evaluation mode for this evaluable object
     */
    EvaluationMode getEvaluationMode();
}

package com.smartsparrow.learner.service;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.action.score.ChangeScoreActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.eval.parser.LiteralContext;

public class ChangeScoreActionDataStub {

    public static ChangeScoreAction buildChangeScoreAction(Double value, UUID elementId, CoursewareElementType elementType,
                                                    MutationOperator operator) {

        ChangeScoreActionContext changeActionContext = new ChangeScoreActionContext()
                .setValue(value)
                .setElementId(elementId)
                .setElementType(elementType)
                .setOperator(operator);

        return new ChangeScoreAction()
                .setResolver(new LiteralContext())
                .setContext(changeActionContext)
                .setResolvedValue(value);
    }
}

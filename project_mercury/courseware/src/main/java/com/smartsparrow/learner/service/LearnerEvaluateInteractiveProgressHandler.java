package com.smartsparrow.learner.service;

import javax.inject.Inject;

import org.apache.camel.Body;
import org.apache.camel.Handler;

import com.smartsparrow.learner.event.EvaluationEventMessage;

public class LearnerEvaluateInteractiveProgressHandler {

    @Inject
    public LearnerEvaluateInteractiveProgressHandler() {
    }

    @Handler
    public void processEvaluationEvent(@Body EvaluationEventMessage event) {

        // TODO: do something.

    }
}

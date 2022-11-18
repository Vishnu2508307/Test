package com.smartsparrow.eval.action;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.eval.resolver.Resolvable;

@JsonDeserialize(using = ActionDeserializer.class)
public interface Action<T extends ActionContext> extends Resolvable {

    enum Type {
        CHANGE_PROGRESS,
        CHANGE_SCOPE,
        SEND_FEEDBACK,
        GRADE,
        @Deprecated
        SET_COMPETENCY,
        CHANGE_COMPETENCY,
        UNSUPPORTED_ACTION,
        CHANGE_SCORE,
        GRADE_PASSBACK
    }

    Type getType();

    ResolverContext getResolver();

    T getContext();

}

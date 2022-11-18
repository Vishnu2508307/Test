package com.smartsparrow.courseware.lang;

import com.smartsparrow.exception.Fault;

public class ActionConditionParserFault extends Fault {
    private static final String ERROR_MESSAGE = "unable to parse scenario action: %s";

    public ActionConditionParserFault(Throwable cause) {
        super(String.format(ERROR_MESSAGE, cause.getMessage()));
    }

    @Override
    public int getResponseStatusCode() {
        return 422;
    }

    @Override
    public String getType() {
        return "UNPROCESSABLE_ENTITY";
    }
}

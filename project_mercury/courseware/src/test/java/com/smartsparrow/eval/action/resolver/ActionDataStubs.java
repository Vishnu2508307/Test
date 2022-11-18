package com.smartsparrow.eval.action.resolver;

import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.eval.resolver.Resolvable;

public class ActionDataStubs {

    public static Action buildAction(ActionContext context, ResolverContext resolverContext) {
        return new Action() {

            private Object resolvedValue;

            @Override
            public Type getType() {
                return Type.UNSUPPORTED_ACTION;
            }

            @Override
            public ResolverContext getResolver() {
                return resolverContext;
            }

            @Override
            public ActionContext getContext() {
                return context;
            }

            @Override
            public Object getResolvedValue() {
                return resolvedValue;
            }

            @Override
            public Resolvable setResolvedValue(Object object) {
                resolvedValue = object;
                return this;
            }
        };
    }

    public final static String DATA = "{\n" +
                                        "\"context\": {\n" +
                                            "\"data\": {" +
                                                "\"type\":\"list\"" +
                                            "},\n" +
                                        "\"value\": \"a\"\n" +
                                        "}\n" +
                                        "}";
}

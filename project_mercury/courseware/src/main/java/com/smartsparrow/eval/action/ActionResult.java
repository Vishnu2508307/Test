package com.smartsparrow.eval.action;

public interface ActionResult<T> {

    public Action.Type getType();

    public T getValue();

}

package com.smartsparrow.learner.service;

import static org.mockito.ArgumentMatchers.any;

import org.mockito.Mockito;

public class UpdateProgressHandlerDataStub {

    @SuppressWarnings("unchecked")
    public static <T extends UpdateProgressHandler> T mockProgressHandler(T progressHandler) {
        UpdateProgressHandler spy = Mockito.spy(progressHandler);

        Mockito.doNothing().when(spy).propagateProgressChangeUpwards(any(), any(), any());
        Mockito.doNothing().when(spy).broadcastProgressEventMessage(any(), any());
        return (T) spy;
    }
}

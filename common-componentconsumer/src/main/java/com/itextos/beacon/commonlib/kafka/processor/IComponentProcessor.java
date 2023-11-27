package com.itextos.beacon.commonlib.kafka.processor;

import com.itextos.beacon.commonlib.messageobject.IMessage;

public interface IComponentProcessor
        extends
        Runnable
{

    boolean isInProcess();

    void processMessage(
            IMessage aMessage);

    void stopProcessing();

    void doCleanup();

    boolean isCompleted();

}
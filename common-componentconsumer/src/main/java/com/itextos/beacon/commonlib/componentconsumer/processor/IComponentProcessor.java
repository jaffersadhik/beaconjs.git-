package com.itextos.beacon.commonlib.componentconsumer.processor;

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
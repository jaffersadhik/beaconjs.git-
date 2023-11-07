package com.itextos.beacon.commonlib.kafka.processor;

import com.itextos.beacon.commonlib.message.IMessage;

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
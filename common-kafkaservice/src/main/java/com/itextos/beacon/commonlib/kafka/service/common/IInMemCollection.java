package com.itextos.beacon.commonlib.kafka.service.common;

import java.util.List;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.messageobject.IMessage;

public interface IInMemCollection
{

    KafkaType getType();

    void addMessage(
            IMessage aIMessage)
            throws ItextosException;

    IMessage getMessage();

    String getTopicName();

    int getInMemSize();

    void shutdown();

    List<IMessage> getRemainingMessages();

}

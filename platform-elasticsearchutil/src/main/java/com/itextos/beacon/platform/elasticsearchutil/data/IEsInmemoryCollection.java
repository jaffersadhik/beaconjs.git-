package com.itextos.beacon.platform.elasticsearchutil.data;

import java.util.List;

import com.itextos.beacon.commonlib.messageobject.IMessage;

public interface IEsInmemoryCollection
{

    boolean add(
            IMessage aMessage);

    boolean add(
            List<IMessage> aMessageList);

    void processRemainingData();

}
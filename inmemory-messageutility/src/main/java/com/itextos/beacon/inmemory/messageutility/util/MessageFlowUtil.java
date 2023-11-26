package com.itextos.beacon.inmemory.messageutility.util;

import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.messageutility.cache.MessageSuffixPrefix;

public class MessageFlowUtil
{

    private MessageFlowUtil()
    {}

    public static MessageSuffixPrefix getMessageSuffixPrefixInfo()
    {
        return (MessageSuffixPrefix) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ACCOUNT_MSG_PREFIX_SUFFIX);
    }

   

}
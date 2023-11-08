package com.itextos.beacon.inmemory.msgutil.util;

import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircle;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircles;
import com.itextos.beacon.inmemory.msgutil.cache.MessageSuffixPrefix;

public class MessageFlowUtil
{

    private MessageFlowUtil()
    {}

    public static MessageSuffixPrefix getMessageSuffixPrefixInfo()
    {
        return (MessageSuffixPrefix) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ACCOUNT_MSG_PREFIX_SUFFIX);
    }

    public static CarrierCircle getCarrierCircle(
            String aMNumberSeries)
    {
        final CarrierCircles lCarrierCircle = (CarrierCircles) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CARRIER_CIRCLE);
        return lCarrierCircle.getCarrierCircle(aMNumberSeries);
    }

    public static CarrierCircle getCarrierCircle(
            String aMNumberSeries,
            boolean aReturnDefault)
    {
        final CarrierCircles lCarrierCircle = (CarrierCircles) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CARRIER_CIRCLE);
        return lCarrierCircle.getCarrierCircle(aMNumberSeries, aReturnDefault);
    }

}
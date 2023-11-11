package com.itextos.beacon.platform.esutil;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.platform.esutil.types.EsOperation;

public class AgingDn
{

    static void insertAgingDn(
            SubmissionObject aSubmissionObject)
    {
        EsInmemoryCollectionFactory.getInstance().getInmemCollection(EsOperation.AGING_INSERT).add(aSubmissionObject);
    }

    static void updateAgingDn(
            DeliveryObject aDeliveryObject)
    {
        EsInmemoryCollectionFactory.getInstance().getInmemCollection(EsOperation.AGING_UPDATE).add(aDeliveryObject);
    }

    static void deleteAgingDn(
            BaseMessage aItextosMessage)
    {
        EsInmemoryCollectionFactory.getInstance().getInmemCollection(EsOperation.AGING_DELETE).add(aItextosMessage);
    }

}
package com.itextos.beacon.platform.elasticsearchutil;

import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.DeliveryObject;
import com.itextos.beacon.commonlib.messageobject.SubmissionObject;
import com.itextos.beacon.platform.elasticsearchutil.types.EsOperation;

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
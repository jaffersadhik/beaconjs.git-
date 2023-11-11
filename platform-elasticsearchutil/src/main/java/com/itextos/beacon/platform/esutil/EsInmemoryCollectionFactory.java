package com.itextos.beacon.platform.esutil;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.exception.ItextosRuntimeException;
import com.itextos.beacon.platform.esutil.data.AgingDelete;
import com.itextos.beacon.platform.esutil.data.AgingInsert;
import com.itextos.beacon.platform.esutil.data.AgingUpdate;
import com.itextos.beacon.platform.esutil.data.DlrQueryDnBulk;
import com.itextos.beacon.platform.esutil.data.DlrQuerySubBulk;
import com.itextos.beacon.platform.esutil.data.IEsInmemoryCollection;
import com.itextos.beacon.platform.esutil.types.EsOperation;

class EsInmemoryCollectionFactory
{

    private static final Log log = LogFactory.getLog(EsInmemoryCollectionFactory.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final EsInmemoryCollectionFactory INSTANCE = new EsInmemoryCollectionFactory();

    }

    static EsInmemoryCollectionFactory getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<EsOperation, IEsInmemoryCollection> mCollections = new EnumMap<>(EsOperation.class);

    IEsInmemoryCollection getInmemCollection(
            EsOperation aElasticSearchType)
    {
        return mCollections.computeIfAbsent(aElasticSearchType, k -> getNewObject(aElasticSearchType));
    }

    private static IEsInmemoryCollection getNewObject(
            EsOperation aElasticSearchType)
    {

        switch (aElasticSearchType)
        {
            case AGING_INSERT:
                return new AgingInsert();

            case AGING_DELETE:
                return new AgingDelete();

            case AGING_UPDATE:
                return new AgingUpdate();

            case DLR_QUERY_DN_INSERT:
                return new DlrQueryDnBulk();

            case DLR_QUERY_SUB_INSERT:
                return new DlrQuerySubBulk();

            case AGING_QUERY:
            case DLR_QUERY_QUERY:
            case R3_INSERT:
            case SINGLE_DN_INSERT:
            case SINGLE_DN_QUERY:
            case SINGLE_DN_DELETE:
            default:
                throw new ItextosRuntimeException(aElasticSearchType + " is not supported for Async process.");
        }
    }

}
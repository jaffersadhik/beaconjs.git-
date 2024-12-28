package com.itextos.beacon.platform.dnaging.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.dnaging.util.AgingDlrProcessUtil;
import com.itextos.beacon.platform.dnutil.process.DlrRequestProcess;

public class DlrProcessUtil
{

    private static final Log log = LogFactory.getLog(DlrProcessUtil.class);

    private DlrProcessUtil()
    {}

    
    public static Map<Component, DeliveryObject> processAgeingDNProcessQ(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        final Map<Component, DeliveryObject> lNextComponentMap = new HashMap<>();

        final String                         lMessageId        = aDeliveryObject.getMessageId();

        if (log.isDebugEnabled())
            log.debug("Begin Message Id:" + lMessageId);

        final AgingDlrProcessUtil ageingProcessor = new AgingDlrProcessUtil(aDeliveryObject, lNextComponentMap);
        ageingProcessor.processAgeingDn();

        if (log.isDebugEnabled())
            log.debug(" O/P from ageingdn Message Id:" + lMessageId + " lNextComponentMap:" + (lNextComponentMap != null ? lNextComponentMap.keySet() : lNextComponentMap));

        if (!lNextComponentMap.isEmpty())
        {
            final Iterator<Component>            iterator     = lNextComponentMap.keySet().iterator();
            final Map<Component, DeliveryObject> nextQueueMap = new HashMap<>();

            while (iterator.hasNext())
            {
                final Component      lComponent      = iterator.next();
                final DeliveryObject lDeliveryObject = lNextComponentMap.get(lComponent);
                if (lComponent.equals(Component.T2DB_DELIVERIES))
                    DlrRequestProcess.processDNQueueReq(lDeliveryObject, nextQueueMap);
                else
                    if (lComponent.equals(Component.T2DB_INTERIM_DELIVERIES))
                        nextQueueMap.put(lComponent, lDeliveryObject);
                    else
                        nextQueueMap.put(lComponent, lDeliveryObject);
            }
            if (log.isDebugEnabled())
                log.debug("End Message Id:" + lMessageId + " nextQueueMap:" + (nextQueueMap != null ? nextQueueMap.keySet() : nextQueueMap));
            return nextQueueMap;
        }
        return null;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

}

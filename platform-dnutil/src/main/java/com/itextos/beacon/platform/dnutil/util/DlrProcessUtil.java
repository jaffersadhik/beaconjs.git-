package com.itextos.beacon.platform.dnutil.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.dnutil.process.DlrRequestProcess;
import com.itextos.beacon.smslog.DNPLog;

public class DlrProcessUtil
{

    private static final Log log = LogFactory.getLog(DlrProcessUtil.class);

    private DlrProcessUtil()
    {}

    public static Map<Component, DeliveryObject> processDnReceiverQ(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
   
        final Map<Component, DeliveryObject> lNextComponentMap = new HashMap<>();

        final List<Component>                lProcessDnRetryLs = DlrRetryProcess.processRetry(aDeliveryObject);
     
        DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " : next Level Components : "+lProcessDnRetryLs );

        aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(aDeliveryObject.getMessageId()+  " : Begin processDnRetryLs:" + lProcessDnRetryLs + " Message Id:" + aDeliveryObject.getMessageId() + " status_id:" + aDeliveryObject.getDnOrigianlstatusCode());

        for (final Component lComponentName : lProcessDnRetryLs)
            if (lComponentName == Component.T2DB_DELIVERIES)
                DlrRequestProcess.processDNQueueReq(aDeliveryObject, lNextComponentMap);
            else
                lNextComponentMap.put(lComponentName, aDeliveryObject);
     
        
        aDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" End processDnRetryLs:" + lProcessDnRetryLs + " Message Id:" + aDeliveryObject.getMessageId() + " lNextComponentMap keyset:"
                + (lNextComponentMap != null ? lNextComponentMap.keySet() : lNextComponentMap));

        return lNextComponentMap;
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

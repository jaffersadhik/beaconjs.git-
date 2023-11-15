package com.itextos.beacon.platform.dnr.process;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.exception.ItextosException;
import com.itextos.beacon.commonlib.kafka.process.MessageProcessor;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.dnrfallback.DlrFallbackProcessor;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class DlrProcess
{

    private static final Log log = LogFactory.getLog(DlrProcess.class);

    public static void doProcess(
            HttpServletRequest aDLRRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("DLR QueryString from Carrier - " + aDLRRequest.getQueryString());

            final String lDr       = aDLRRequest.getParameter("dr");
            final String lSmscid   = aDLRRequest.getParameter("smscid");
            final String lAddInfo  = aDLRRequest.getParameter("add_info");
            final String lSystemid = aDLRRequest.getParameter("systemid");
            final String lStatuscd = aDLRRequest.getParameter("statuscd");
            final String lCarrierAcknowledgeId = aDLRRequest.getParameter("carrierackid");

            if (log.isDebugEnabled())
            {
                log.debug("dr       : '" + lDr + "'");
                log.debug("add_info : '" + lAddInfo + "'");
                log.debug("systemid : '" + lSystemid + "'");
                log.debug("smscid   : '" + lSmscid + "'");
                log.debug("statuscd : '" + lStatuscd + "'");
                log.debug("lCarrierAcknowledgeId : '" + lCarrierAcknowledgeId + "'");

            }

            final DeliveryObject deliveryObject = new DeliveryObject(lAddInfo);
            deliveryObject.setCarrierFullDn(lDr);
            deliveryObject.setCarrierSystemId(lSystemid);
            deliveryObject.setSmscId(lSmscid);
            deliveryObject.setCarrierAcknowledgeId(lCarrierAcknowledgeId);
            
            if (log.isDebugEnabled())
                log.debug("Delivery Object :" + deliveryObject.getJsonString());

            // PrometheusMetrics.platformIncrement(Component.DNR,
            // deliveryObject.getClusterType(), "");

            ReceivedCounter.getInstance().add();

            sendToDnProcessTopic(deliveryObject);

            if (log.isDebugEnabled())
                log.debug("processRequest() - Successfully Send to DN processor ..");
        }
        catch (final Exception e)
        {
            log.error("processRequest() - Exception occer while processing the request ..", e);
        }
    }

    private static void sendToDnProcessTopic(
            DeliveryObject aDeliveryObject)
    {
        final boolean isKafkaAvailable = CommonUtility.isEnabled(getConfigParamsValueAsString(ConfigParamConstants.IS_KAFKA_AVAILABLE));

        if (!isKafkaAvailable)
        {
            if (log.isDebugEnabled())
                log.debug("Unable to push kafka, Hence sending to Mysql ..");

            sendToFallback(aDeliveryObject);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Kafka available flag is disabled..., Hence pushing to kafak..");

            try
            {
                MessageProcessor.writeMessage(Component.DNR, Component.DNP, aDeliveryObject);
            }
            catch (final ItextosException e)
            {
                log.error("Message sending to kafka is failed, Hence sending to Dlr Receive Fallback table..", e);
                sendToFallback(aDeliveryObject);
            }
        }
    }

    private static void sendToFallback(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            DlrFallbackProcessor.sendToFallBack(aDeliveryObject);
        }
        catch (final Exception e1)
        {
            log.error("Message storing in DB failed..", e1);
            sendToErrorLog(aDeliveryObject, e1);
        }
    }

    public static void sendToErrorLog(
            BaseMessage aBaseMessage,
            Exception aErrorMsg)
    {

        try
        {
            PlatformUtil.sendToErrorLog(Component.DNR, aBaseMessage, aErrorMsg);
        }
        catch (final Exception e21)
        {
            log.error("Exception while sending request to error log. " + aBaseMessage, e21);
        }
    }

    public static String getConfigParamsValueAsString(
            ConfigParamConstants aKey)
    {
        final ApplicationConfiguration lAppConfigValues = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfigValues.getConfigValue(aKey.getKey());
    }

}
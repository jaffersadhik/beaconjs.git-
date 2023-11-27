package com.itextos.beacon.platform.ic.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.Services;
import com.itextos.beacon.commonlib.constants.SubServices;
import com.itextos.beacon.commonlib.kafka.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.kafka.service.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageobject.BaseMessage;
import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.commonlib.messageobject.MessageRequest;
import com.itextos.beacon.commonlib.messageobject.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.ic.util.HeaderValidaton;
import com.itextos.beacon.platform.ic.util.ICProducer;
import com.itextos.beacon.platform.ic.util.ICUtility;

public class ICProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(ICProcessor.class);

    public ICProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    }

    @Override
    public void doProcess(
            BaseMessage aBaseMessage)
            throws Exception
    {
        final MessageRequest lMessageRequest = (MessageRequest) aBaseMessage;

        try
        {
            setClientHeader(lMessageRequest);
            setClietDltParams(lMessageRequest);

            final boolean lDoInterfaceValidation = doInterfaceValidation(lMessageRequest);
            if(log.isDebugEnabled()) {
            
            	log.debug(lMessageRequest.getBaseMessageId()+" lDoInterfaceValidation : "+lDoInterfaceValidation);
            }
            if (!lDoInterfaceValidation)
                return;

            final boolean lDoPlatformValidation = doPlatformValidation(lMessageRequest);
            
            if(log.isDebugEnabled()) {
        	log.debug(lMessageRequest.getBaseMessageId()+" lDoPlatformValidation : "+lDoPlatformValidation);
            }
            if (!lDoPlatformValidation)
                return;

            doVLCheck(lMessageRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the request .." + lMessageRequest, e);
            ICProducer.sendToErrorLog(lMessageRequest, e);
        }
    }

    private static void setClietDltParams(
            MessageRequest aMessageRequest)
    {
        final String lDltEntityId   = CommonUtility.nullCheck(aMessageRequest.getDltEntityId(), true);
        final String lDltTemplateId = CommonUtility.nullCheck(aMessageRequest.getDltTemplateId(), true);

        if(log.isDebugEnabled()) {
        	
        	log.debug(aMessageRequest.getBaseMessageId()+" lDltEntityId By Client : "+lDltEntityId);
        	log.debug(aMessageRequest.getBaseMessageId()+" lDltTemplateId By Client : "+lDltTemplateId);

        }
        aMessageRequest.setClientDltEntityId(lDltEntityId);
        aMessageRequest.setClientDltTemplateId(lDltTemplateId);
    }

    private static void setClientHeader(
            MessageRequest aMessageRequest)
    {
        final String lHeader = CommonUtility.nullCheck(MessageUtil.getHeaderId(aMessageRequest), true);

        if(log.isDebugEnabled()) {
        	log.debug(aMessageRequest.getBaseMessageId()+" Header Sent By Client : "+lHeader);
        }
        if (!lHeader.isEmpty())
            aMessageRequest.setClientHeader(lHeader);
    }

    private static void doVLCheck(
            MessageRequest aMessageRequest)
    {

        if (!aMessageRequest.isIsIntl())
        {
            if (log.isDebugEnabled())
                log.debug("Validating Series lookup for Domestic Number ...");

            if (!ICUtility.isValidDomesticNumber(aMessageRequest))
            {
                if (log.isDebugEnabled())
                    log.debug("Rejecting due to Invalid Destination..");
                aMessageRequest.setPlatfromRejected(true);
                aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.MOBILE_NUMBER_IS_NOT_VALID.getKey());
                processRejections(aMessageRequest);
                return;
            }
        }

        if (!ICUtility.updatePriceInfo(aMessageRequest))
        {
            aMessageRequest.setPlatfromRejected(true);
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.PRICE_CONVERSION_FAILED.getKey());
            processRejections(aMessageRequest);
            return;
        }

        if (ICUtility.isVLFeatureEnabled(aMessageRequest))
        {
            if (log.isDebugEnabled())
                log.debug("Request sending to R3C...");

            ICProducer.sendToUrlShortner(aMessageRequest);
        }
        else
            if (aMessageRequest.isIsIntl())
                handleIntlService(aMessageRequest);
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Handling non domestic cases. Base Message Id :" + aMessageRequest.getBaseMessageId());

                /*
                 * if (aMessageRequest.getValue(MiddlewareConstant.MW_UDHI) == null)
                 * aMessageRequest.putValue(MiddlewareConstant.MW_UDHI, "0");
                 */
                if (log.isDebugEnabled())
                    log.debug("Request sent to Schedule/Blockout Verify Component ..");

                ICProducer.sendToSBCVProcessor(aMessageRequest);
            }
    }

    private static boolean doPlatformValidation(
            MessageRequest aMessageRequest)
    {
        setDltEnable(aMessageRequest);
        ICUtility.setBypassDLTTemplateCheck(aMessageRequest);

        boolean isSuccess       = ICUtility.doCommonValidation(aMessageRequest);
        boolean lPlatformReject = aMessageRequest.isPlatfromRejected();

        if(log.isDebugEnabled()) {
        	
        	log.debug(aMessageRequest.getBaseMessageId()+" ICUtility.doCommonValidation(aMessageRequest) :"+isSuccess);
        	log.debug(aMessageRequest.getBaseMessageId()+" lPlatformReject :"+lPlatformReject);
        	log.debug(aMessageRequest.getBaseMessageId()+" aMessageRequest.isIsIntl() :"+aMessageRequest.isIsIntl());

        }
        if (!isSuccess && lPlatformReject)
        {
            processRejections(aMessageRequest);
            return false;
        }

        if (!aMessageRequest.isIsIntl())
        {
            isSuccess       = new HeaderValidaton(aMessageRequest).validate();
            lPlatformReject = aMessageRequest.isPlatfromRejected();

            if(log.isDebugEnabled()) {
            	
            	log.debug(aMessageRequest.getBaseMessageId()+" HeaderValidaton(aMessageRequest).validate() :"+isSuccess);
            	log.debug(aMessageRequest.getBaseMessageId()+" lPlatformReject :"+lPlatformReject);

            }
      
            if (!isSuccess && lPlatformReject)
            {
                processRejections(aMessageRequest);
                return false;
            }
        }else {
        	
        	   if(log.isDebugEnabled()) {
               	
               	log.debug(aMessageRequest.getBaseMessageId()+" SKIP HeaderValidaton(aMessageRequest).validate() :");

               }
        }
        return true;
    }

    private static void setDltEnable(
            MessageRequest aMessageRequest)
    {
        boolean      isDLTEnable         = CommonUtility.isEnabled(ICUtility.getAppConfigValueAsString(ConfigParamConstants.DLT_ENABLE));
        final String lDefaultCountryCode = CommonUtility.nullCheck(ICUtility.getAppConfigValueAsString(ConfigParamConstants.DEFAULT_COUNTRY_CODE), true).toUpperCase();
        if (!lDefaultCountryCode.equals("IND"))
            isDLTEnable = false;

        aMessageRequest.setDltCheckEnabled(isDLTEnable);
    }

    private static boolean doInterfaceValidation(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+" Calling Interface Validation ..............");

        ICUtility.doInterfaceValidation(aMessageRequest);

        final boolean lInterfaceReject = aMessageRequest.isInterfaceRejected();

        
        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+" After Basic Validation Interface Reject " + lInterfaceReject);

        if (lInterfaceReject)
        {
            processRejections(aMessageRequest);
            return false;
        }
        return true;
    }

    private static void processRejections(
            MessageRequest aMessageRequest)
    {
        final String lStatusId = CommonUtility.nullCheck(aMessageRequest.getSubOriginalStatusCode(), true);

        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+" Rejected Message. Sending to Platfrom Rejection  StatusId:" + lStatusId);

        if (lStatusId.isEmpty())
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.BAD_REQUEST.getStatusCode());

        if (log.isDebugEnabled())
            log.debug(aMessageRequest.getBaseMessageId()+"Sending to Platform Rejection Queue : " );
        ICProducer.sendToPlatformRejection(aMessageRequest);
    }

    private static void handleIntlService(
            MessageRequest aMessageRequest)
    {

        if (!CommonUtility.isEnabled(aMessageRequest.getServiceInfo(Services.SMS.getKey(), SubServices.INTERNATIONAL.getKey())))
        {
            aMessageRequest.setPlatfromRejected(true);
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INTL_SERVICE_DISABLED.getStatusCode());

            if (log.isDebugEnabled())
                log.debug("Intl service disabled: rejecting req sending to biller: " + aMessageRequest.getBaseMessageId());

            ICProducer.sendToPlatformRejection(aMessageRequest);
        }
        else
            ICProducer.sendToSBCVProcessor(aMessageRequest);
    }

    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}

package com.itextos.beacon.platform.ic.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.Services;
import com.itextos.beacon.commonlib.constants.SubServices;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.ic.util.HeaderValidaton;
import com.itextos.beacon.platform.ic.util.ICProducer;
import com.itextos.beacon.platform.ic.util.ICUtility;
import com.itextos.beacon.smslog.SMSLog;

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

        	lMessageRequest.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" LOG START");
        	
        	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append("##########"+lMessageRequest.getBaseMessageId()+"######################");
          
           long starttime=System.currentTimeMillis();
           
           		ICProcessor.forIC(lMessageRequest);
      	   
      	   lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" Time Taken For process :"+(System.currentTimeMillis()-starttime)+" Milli Second"); 
      	
     	
      	   
      	   lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append("##########"+lMessageRequest.getBaseMessageId()+"######################");

           SMSLog.log(lMessageRequest.getLogBuffer().toString());
    }


    public static void forIC(MessageRequest lMessageRequest) {
    	
    	try
        {
            setClientHeader(lMessageRequest);
            setClietDltParams(lMessageRequest);

            final boolean lDoInterfaceValidation = doInterfaceValidation(lMessageRequest);
           
            
            lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" lDoInterfaceValidation : "+lDoInterfaceValidation);
           
            if (!lDoInterfaceValidation)
                return;

            final boolean lDoPlatformValidation = doPlatformValidation(lMessageRequest);
            
            lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" lDoPlatformValidation : "+lDoPlatformValidation);
            
            if (!lDoPlatformValidation)
                return;

            ICUtility.setMccMnc(lMessageRequest);
            
            doVLCheck(lMessageRequest);
        }
        catch (final Exception e)
        {
        	
        	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" Exception occer while processing the request \t "+ErrorMessage.getStackTraceAsString(e));
            log.error("Exception occer while processing the request ." + lMessageRequest, e);
            ICProducer.sendToErrorLog(lMessageRequest, e);
        }

   	//   log.debug(" smslog : "+lMessageRequest.getLogBuffer().toString());

    }
  

	private static void setClietDltParams(
            MessageRequest aMessageRequest)
    {
        final String lDltEntityId   = CommonUtility.nullCheck(aMessageRequest.getDltEntityId(), true);
        final String lDltTemplateId = CommonUtility.nullCheck(aMessageRequest.getDltTemplateId(), true);

        	
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" lDltEntityId By Client : "+lDltEntityId);
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" lDltTemplateId By Client : "+lDltTemplateId);

        
        aMessageRequest.setClientDltEntityId(lDltEntityId);
        aMessageRequest.setClientDltTemplateId(lDltTemplateId);
    }

    private static void setClientHeader(
            MessageRequest aMessageRequest)
    {
        final String lHeader = CommonUtility.nullCheck(MessageUtil.getHeaderId(aMessageRequest), true);

        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" Header Sent By Client : "+lHeader);
       
        if (!lHeader.isEmpty())
            aMessageRequest.setClientHeader(lHeader);
    }

    private static void doVLCheck(
            MessageRequest aMessageRequest)
    {

        if (!aMessageRequest.isIsIntl())
        {
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Validating Series lookup for Domestic Number ..");

            if (!ICUtility.isValidDomesticNumber(aMessageRequest))
            {
            	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" :Rejecting due to Invalid Destination.");
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
            	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" : Request sending to R3C..");

            	
            ICProducer.sendToUrlShortner(aMessageRequest);
        }
        else
            if (aMessageRequest.isIsIntl())
                handleIntlService(aMessageRequest);
            else
            {
            	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append("Handling non domestic cases. Base Message Id :" + aMessageRequest.getBaseMessageId());

                /*
                 * if (aMessageRequest.getValue(MiddlewareConstant.MW_UDHI) == null)
                 * aMessageRequest.putValue(MiddlewareConstant.MW_UDHI, "0");
                 */
                aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId() +" : Request sent to Schedule/Blockout Verify Component .");

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

        	
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" ICUtility.doCommonValidation(aMessageRequest) :"+isSuccess);
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" lPlatformReject :"+lPlatformReject);
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" aMessageRequest.isIsIntl() :"+aMessageRequest.isIsIntl());

        if (!isSuccess && lPlatformReject)
        {
            processRejections(aMessageRequest);
            return false;
        }

        if (!aMessageRequest.isIsIntl())
        {
            isSuccess       = new HeaderValidaton(aMessageRequest).validate();
            lPlatformReject = aMessageRequest.isPlatfromRejected();

            	
            	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" HeaderValidaton(aMessageRequest).validate() :"+isSuccess);
            	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" lPlatformReject :"+lPlatformReject);

      
            if (!isSuccess && lPlatformReject)
            {
                processRejections(aMessageRequest);
                return false;
            }
        }else {
        	
               	
        		   aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" SKIP HeaderValidaton(aMessageRequest).validate() :");

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
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" Calling Interface Validation .......");

        ICUtility.doInterfaceValidation(aMessageRequest);

        final boolean lInterfaceReject = aMessageRequest.isInterfaceRejected();

        
        	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" After Basic Validation Interface Reject " + lInterfaceReject);

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

        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" Rejected Message. Sending to Platfrom Rejection  StatusId:" + lStatusId);

        if (lStatusId.isEmpty())
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.BAD_REQUEST.getStatusCode());

       
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getBaseMessageId()+" Sending to Platform Rejection Queue : " );
        ICProducer.sendToPlatformRejection(aMessageRequest);
    }

    private static void handleIntlService(
            MessageRequest aMessageRequest)
    {

        if (!CommonUtility.isEnabled(aMessageRequest.getServiceInfo(Services.SMS.getKey(), SubServices.INTERNATIONAL.getKey())))
        {
            aMessageRequest.setPlatfromRejected(true);
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INTL_SERVICE_DISABLED.getStatusCode());

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append("Intl service disabled: rejecting req sending to biller: " + aMessageRequest.getBaseMessageId());

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

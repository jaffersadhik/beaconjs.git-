package com.itextos.beacon.interfaces.generichttpapi.processor.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaInterfaceAsyncProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.AsyncRequestObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.http.interfaceutil.MessageSource;
import com.itextos.beacon.interfaces.generichttpapi.processor.request.JSONRequestProcessor;
import com.itextos.beacon.interfaces.generichttpapi.processor.request.XMLRequestProcessor;

public class AsyncRequestProcessor
        extends
        AbstractKafkaInterfaceAsyncProcessor
{

    private static final Log log = LogFactory.getLog(AsyncRequestProcessor.class);

    StringBuffer sb=null;
    
    public AsyncRequestProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis,
            StringBuffer sb)
    {
        super(aThreadName, Component.INTERFACES, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    	this.sb=sb;

    }

    @Override
    public void doProcess(
            AsyncRequestObject aRequestObject)
            throws ParseException
    {
        if (log.isDebugEnabled())
            log.debug("Consume from Async : " + aRequestObject);

        if (aRequestObject.getMessageSource().equals(MessageSource.GENERIC_XML))
            doXmlParsing(aRequestObject);
        else
            doJsonParsing(aRequestObject);
    }

    private  void doJsonParsing(
            AsyncRequestObject aRequestObject)
            throws ParseException
    {

        try
        {
            final JSONRequestProcessor lJsonRrequestProcessor = new JSONRequestProcessor(aRequestObject.getMessageContent(), aRequestObject.getCustomerIp(), aRequestObject.getRequestedTime(),
                    aRequestObject.getMessageSource(), aRequestObject.getMessageSource(),sb);
            final JSONObject           lParsedJson            = Utility.parseJSON(aRequestObject.getMessageContent());
            lJsonRrequestProcessor.processFromQueue(lParsedJson, aRequestObject.getMessageId(), aRequestObject.getCustomerId());
        }
        catch (final ParseException e)
        {
            log.error("Exception while parsing the JSON.", e);
            throw e;
        }
    }

    private void doXmlParsing(
            AsyncRequestObject aRequestObject)
    {

        try
        {
            final XMLRequestProcessor lXmlRequestProcessor = new XMLRequestProcessor(aRequestObject.getMessageContent(), aRequestObject.getCustomerIp(), aRequestObject.getRequestedTime(),sb);
            lXmlRequestProcessor.continueFromQueue(aRequestObject.getMessageContent(), aRequestObject.getMessageId(), aRequestObject.getCustomerId());
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing the XML.", e);
            throw e;
        }
    }

    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aArg0)
    {}

}
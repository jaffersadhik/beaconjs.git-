package com.itextos.beacon.httpclienthandover.process;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.httputility.HttpResult;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverMaster;
import com.itextos.beacon.httpclienthandover.data.URLResult;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;
import com.itextos.beacon.httpclienthandover.utils.TopicSenderUtility;

public class SingleDLRProcess
        extends
        AbstractDLRProcess
{

    private static final Log log = LogFactory.getLog(SingleDLRProcess.class);

    @Override
    public void processDLR(
            List<BaseMessage> aMessageList)
    {

        for (final BaseMessage message : aMessageList)
        {
            final ClientHandoverData clientHandoverData = ClientHandoverUtils.getClientHandoverData(message.getValue(MiddlewareConstant.MW_CLIENT_ID));

            if (clientHandoverData == null)
            {
                final UUID       uniqueId   = UUID.randomUUID();

                final HttpResult failResult = ClientHandoverUtils.getCustomResult("No Client Configuration found for " + message.getValue(MiddlewareConstant.MW_CLIENT_ID), -999);
                ClientHandoverUtils.setResultInMessage(message, failResult);

                message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);
                TopicSenderUtility.sendToMasterLogQueue(message);

                message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_MASTER_RECORD, "" + 1);
                TopicSenderUtility.sendToChildLogQueue(message);

                log.error("Single DLR | Client Handover Configuration Missing for customer ID : '" + message.getValue(MiddlewareConstant.MW_CLIENT_ID) + "'");

                continue;
            }

            final List<ClientHandoverMaster> customerEndPointInfos = clientHandoverData.getClientHandoverMaster();

            doWeNeedToCheckLatency(message);

            for (final ClientHandoverMaster customerEndPoint : customerEndPointInfos)
            {
                final String    processedTemplate    = processTemplate(customerEndPoint, message);

                long            httpProcessStartTime = System.currentTimeMillis();
                final URLResult httpResult           = processHTTPRequest(processedTemplate, customerEndPoint, customerEndPoint.getPrimaryUrl());
                long            httpProcessEndTime   = System.currentTimeMillis();

                long            totalHttpProcessTime = httpProcessEndTime - httpProcessStartTime;

                final boolean   retrySecondaryUrl    = (!ClientHandoverUtils.isHttpProcessSuccess(httpResult.getHttpResult())) && StringUtils.isNotEmpty(customerEndPoint.getSecondaryUrl());

                if (retrySecondaryUrl)
                {
                    retryOrLogTheData(message, httpProcessStartTime, httpProcessEndTime, totalHttpProcessTime, httpResult, clientHandoverData, processedTemplate, retrySecondaryUrl, true);

                    httpProcessStartTime = System.currentTimeMillis();
                    final URLResult httpResultSecondary = processHTTPRequest(processedTemplate, customerEndPoint, customerEndPoint.getSecondaryUrl());
                    httpProcessEndTime   = System.currentTimeMillis();
                    totalHttpProcessTime = httpProcessEndTime - httpProcessStartTime;

                    retryOrLogTheData(message, httpProcessStartTime, httpProcessEndTime, totalHttpProcessTime, httpResultSecondary, clientHandoverData, processedTemplate, false, false);
                }
                else
                    retryOrLogTheData(message, httpProcessStartTime, httpProcessEndTime, totalHttpProcessTime, httpResult, clientHandoverData, processedTemplate, retrySecondaryUrl, true);
            }
        }
    }

    private static void retryOrLogTheData(
            BaseMessage message,
            long aHttpProcessStartTime,
            long aHttpProcessEndTime,
            long aTotalHttpProcessTime,
            URLResult aHttpResult,
            ClientHandoverData aClientHandoverData,
            String aProcessedTemplate,
            boolean onlyLog,
            boolean updateRetryCount)
    {
        final String startTime = DateTimeUtility.getFormattedDateTime(aHttpProcessStartTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
        final String endTime   = DateTimeUtility.getFormattedDateTime(aHttpProcessEndTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);

        if (log.isDebugEnabled())
            log.debug("HttpResult : '" + aHttpResult + "' | Request Start time: '" + startTime + "' | EndTime: '" + endTime + "'");

        addResultAndTimeToBaseMessage(message, aHttpResult, aTotalHttpProcessTime, false, startTime, endTime, aProcessedTemplate);

        final UUID        uniqueId   = UUID.randomUUID();
        final BaseMessage masterData = message.getClonedObject();
        masterData.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);

        processRecordBasedOnResult(aHttpResult, masterData, aClientHandoverData, false, true, onlyLog);
        message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_UNIQUE_ID, "" + uniqueId);

        if (masterData.getValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK) != null)
            message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK, masterData.getValue(MiddlewareConstant.MW_AALPHA));

        processRecordBasedOnResult(aHttpResult, message, aClientHandoverData, false, false, onlyLog);
        message.putValue(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_CHECK, null);
    }

}
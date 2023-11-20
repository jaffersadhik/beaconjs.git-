package com.itextos.beacon.interfaces.generichttpapi.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.errorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.prometheus.PrometheusMetrics;
import com.itextos.beacon.interfaces.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.interfaces.generichttpapi.common.utils.Utility;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.RequestReader;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.XMLRequestReader;
import com.itextos.beacon.interfaces.interfaceutil.MessageSource;

public class XMLReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = 6516855303392970928L;

    private static final Log  log              = LogFactory.getLog(XMLReceiver.class);

    public XMLReceiver()
    {}

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        if (log.isDebugEnabled())
            log.debug("XML request received in doGet");

        final long lProcessStart = System.currentTimeMillis();
        PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_XML, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

        final RequestReader reader = new XMLRequestReader(aRequest, aResponse);
        reader.processGetRequest();

        final long lProcessEnd = System.currentTimeMillis();

        if (log.isInfoEnabled())
            log.info("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '"
                    + (lProcessEnd - lProcessStart) + "' milliseconds");
    }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        final long lProcessStart = System.currentTimeMillis();

        try
        {
            if (log.isDebugEnabled())
                log.debug("XML request received in doPost");
            PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_XML, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

            final RequestReader reader = new XMLRequestReader(aRequest, aResponse);
            reader.processPostRequest();
        }
        catch (final Exception e)
        {
            log.error("Exception while processing request in Post method.", e);
        }
        finally
        {
            final long lProcessEnd = System.currentTimeMillis();

            if (log.isInfoEnabled())
                log.info("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '"
                        + (lProcessEnd - lProcessStart) + "' milliseconds");
        }
    }

}
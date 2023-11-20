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
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.JSONRequestReader;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.RequestReader;
import com.itextos.beacon.interfaces.interfaceutil.MessageSource;

public class JSONGenericReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = -4880462426737762823L;

    private static final Log  log              = LogFactory.getLog(JSONGenericReceiver.class);

    public JSONGenericReceiver()
    {}

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        if (log.isDebugEnabled())
            log.debug("Generic JSON  request received in doGet");

        final long start = System.currentTimeMillis();
        PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

        final RequestReader reader = new JSONRequestReader(aRequest, aResponse, MessageSource.GENERIC_JSON, null);
        reader.processGetRequest();

        final long end    = System.currentTimeMillis();
        final long result = end - start;

        if (log.isInfoEnabled())
            log.info("Request Start time : '" + Utility.getFormattedDateTime(start) + "' End time : '" + Utility.getFormattedDateTime(end) + "' Processing time : '" + result + "' milliseconds");
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
                log.debug("Generic JSON request received in doPost");

            PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_JSON, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());

            final RequestReader reader = new JSONRequestReader(aRequest, aResponse, MessageSource.GENERIC_JSON, null);

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

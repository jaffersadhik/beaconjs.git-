package com.itextos.beacon.interfaces.generichttpapi.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheus.PrometheusMetrics;
import com.itextos.beacon.interfaces.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.interfaces.generichttpapi.common.utils.Utility;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.QSRequestReader;
import com.itextos.beacon.interfaces.generichttpapi.processor.reader.RequestReader;
import com.itextos.beacon.interfaces.interfaceutil.MessageSource;

public class QSGenericReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = -188048290390545145L;

    private static final Log  log              = LogFactory.getLog(QSGenericReceiver.class);

    public QSGenericReceiver()
    {}

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        if (log.isDebugEnabled())
            log.debug("QS request received in doGet");

        final long lProcessStart = System.currentTimeMillis();
        PrometheusMetrics.apiIncrementAcceptCount(InterfaceType.HTTP_JAPI, MessageSource.GENERIC_QS, APIConstants.CLUSTER_INSTANCE, aRequest.getRemoteAddr());
        final RequestReader reader = new QSRequestReader(aRequest, aResponse, getServletName(), null);
        reader.processGetRequest();

        final long lProcessEnd   = System.currentTimeMillis();
        final long lProcessTaken = lProcessEnd - lProcessStart;

        if (log.isInfoEnabled())
            log.info("Request Start time : '" + Utility.getFormattedDateTime(lProcessStart) + "' End time : '" + Utility.getFormattedDateTime(lProcessEnd) + "' Processing time : '" + lProcessTaken
                    + "' milliseconds");
    }

    @Override
    protected void doPost(
            HttpServletRequest arg0,
            HttpServletResponse arg1)
            throws ServletException,
            IOException
    {
        doGet(arg0, arg1);
    }

}

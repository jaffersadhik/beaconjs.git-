package com.itextos.beacon.web.generichttpapi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.interfacefallback.inmem.FallbackQReaper;
import com.itextos.beacon.interfaces.generichttpapi.processor.pollers.FilePoller;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class InitServlet
        extends
        BasicServlet
{

    private static final long serialVersionUID = -922457646505165311L;


    public InitServlet()
    {
        super();
    }

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {
    	
    

     

        try
        {
            
            final MessageIdentifier lMsgIdentifier = MessageIdentifier.getInstance();
            lMsgIdentifier.init(InterfaceType.HTTP_JAPI);

            final String lAppInstanceId = lMsgIdentifier.getAppInstanceId();

         
            PrometheusMetrics.registerServer();
            PrometheusMetrics.registerApiMetrics();

            APIConstants.setAppInstanceId(lAppInstanceId);

            if (APIConstants.CLUSTER_INSTANCE == null)
            {
              //  System.exit(-1);
            }

            
            String module=System.getenv("module");
            if(module!=null&&(module.equals("japi")||module.equals("all"))) {
            	
            	FallbackQReaper.getInstance();

   //         	startConsumers();
            }
        }
        catch (final Exception e)
        {
        }
    }

    private static void startConsumers()
    {

        if (CommonUtility.isEnabled(APIConstants.START_CONSUMER))
        {
           
            try
            {
                final ProcessorInfo lProcessor = new ProcessorInfo(Component.INTERFACE_ASYNC_PROCESS, false);
                lProcessor.process();
            }
            catch (final Exception e)
            {
                System.exit(-1);
            }

            final FilePoller lFilePoller = new FilePoller();
        }
        
    }

    @Override
    public void destroy()
    {
        MessageIdentifier.getInstance().resetMessageIdentifier();
    }

    @Override
    public void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws IOException
    {

        try (
                PrintWriter writer = aResponse.getWriter();)
        {
            writer.write("uphttpapiweb InitServlet. Time : " + new Date());
            writer.flush();
            aResponse.setStatus(HttpURLConnection.HTTP_OK);
        }
        catch (final Exception e)
        {
            aResponse.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    @Override
    public void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws IOException
    {
        doGet(aRequest, aResponse);
    }

}
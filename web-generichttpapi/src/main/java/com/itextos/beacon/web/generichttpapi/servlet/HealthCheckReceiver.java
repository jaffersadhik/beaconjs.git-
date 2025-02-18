package com.itextos.beacon.web.generichttpapi.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;

public class HealthCheckReceiver
        extends
        BasicServlet
{

    private static final long serialVersionUID = -188048290390545145L;

    private static final Log  log              = LogFactory.getLog(HealthCheckReceiver.class);

    public HealthCheckReceiver()
    {}

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    { try
    {
       

        final PrintWriter writer = aResponse.getWriter();
        writer.println("ok");
        writer.flush();
        writer.close();
    }
    catch (final IOException e)
    {
    }
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

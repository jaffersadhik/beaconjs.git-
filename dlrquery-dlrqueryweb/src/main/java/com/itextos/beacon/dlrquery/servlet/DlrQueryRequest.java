package com.itextos.beacon.dlrquery.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.dlrquery.reqresp.DlrQueryReqProcessor;

/**
 * Servlet implementation class DlrQueryRequest
 */
public class DlrQueryRequest
        extends
        BasicServlet
{

    private static final long serialVersionUID = 1L;

    private static Log        log              = LogFactory.getLog(DlrQueryRequest.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DlrQueryRequest()
    {
        super();
    }

    

 
	@Override
	protected void doGet(jakarta.servlet.http.HttpServletRequest request,
			jakarta.servlet.http.HttpServletResponse response) throws jakarta.servlet.ServletException, IOException {
		
        DlrQueryReqProcessor.processRequest(request, response);

		
	}

	@Override
	protected void doPost(jakarta.servlet.http.HttpServletRequest request,
			jakarta.servlet.http.HttpServletResponse response) throws jakarta.servlet.ServletException, IOException {
		
		doGet(request,response);
		
	}

}

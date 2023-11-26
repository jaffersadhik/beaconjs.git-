package com.itextos.beacon.unittestweb.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.apperrorhandler.servlets.ErrorCodeStatus;
import com.itextos.beacon.commonlib.modulecheck.WildFlyModuleAvailabilityChecker;
import com.itextos.beacon.commonlib.unittest.PatternTest;

@WebServlet(
        value = "/get",
        loadOnStartup = 1)
public class InitServlet
        extends
        BasicServlet
{

    private static final long serialVersionUID = -896811591722186822L;

    public InitServlet()
    {
        super();
    }

    @Override
    public void init(
            ServletConfig config)
            throws ServletException
    {
    	PatternTest patternTest =new PatternTest();
    	System.out.println("Result  : patternTest.doSuccessTest() : "+patternTest.doSuccessTest());
    	System.out.println("Result  : patternTest.doFailTest() : "+patternTest.doFailTest());
    
    

    	

    }

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		doProcess(request,response);

	}



	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
  	
    	
		doProcess(request,response);

	}

	
	private void doProcess(HttpServletRequest request, HttpServletResponse response) {
		 try (
                final PrintWriter writer = response.getWriter();)
        {
   			
   			WildFlyModuleAvailabilityChecker module=new WildFlyModuleAvailabilityChecker();

   	    	module.doProcess();
   	  
            final StringBuilder sb = new StringBuilder();
            Map<String,String> data=new HashMap<String,String>();
            data.put("date", new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS z").format(new Date()));
            data.put("data", WildFlyModuleAvailabilityChecker.module.toString());
            writer.print(sb.toString());
            writer.flush();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
		
	}
}
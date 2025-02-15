package com.itextos.beacon.commonlib.password.servlet;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;

import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;

@WebServlet(name = "InitializerServlet", loadOnStartup = 1)
public class InitializerServlet extends GenericServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	private static final String className = "[InitializeExcludeConsumer]";
	
	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {

	}
	
	public void init() throws ServletException {
		super.init();
		


			try {
				
				PrometheusMetrics.registerServer();
		        PrometheusMetrics.registerApiMetrics();
			} catch (Exception e) {
			}

		
	}

}

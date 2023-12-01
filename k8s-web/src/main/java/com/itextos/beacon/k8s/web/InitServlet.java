package com.itextos.beacon.k8s.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.k8s.namespace.KubernetesNamespace;

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

    	KubernetesNamespace k8snamespace=new KubernetesNamespace();
    	
    	try {
			k8snamespace.docreateNameSpaceIfNotExsits();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

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
			 
			 
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
		
	}
}
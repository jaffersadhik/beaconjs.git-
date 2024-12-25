package com.itextos.beacon.jettyserver.dnr;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.errorlog.QPRLog;

public class QueryEngine
{

    private static final QPRLog log           = QPRLog.getInstance();

    public static void main(
            String[] args)
    {

        try
        {
      
        	Server     server        = null;
          	 PropertiesConfiguration pc   = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.JETTY_SERVER_PROPERTIES, true);

             //    final ConnectionPoolSingleton connPool    = ConnectionPoolSingleton.createInstance(mySQL_cfg_val);
                
                 final int                     server_port = Integer.parseInt( pc.getString("server.port"));
                 final int                     min_threads = Integer
                         .parseInt( pc.getString("server.min.threads"));
                 final int                     max_threads = Integer
                         .parseInt(pc.getString("server.max.threads"));
                 log.info("Query Processor Server Port: " + server_port);
                 log.info("Query Processor Server Min Threads: " + min_threads);
                 log.info("Query Processor Server Max Threads: " + max_threads);

           
            final QueuedThreadPool threadPool = new QueuedThreadPool(max_threads, min_threads);
            server = new Server(threadPool);
            final ServerConnector connector = new ServerConnector(server);
            connector.setPort(server_port);
            server.addConnector(connector);

            addDNR(server);
            
            server.start();

            log.info("Query Processor Server started");

            server.join();
        }
        catch (final Exception ex)
        {
            log.error("Error Occurred", ex);
            ex.printStackTrace();
        }
    }

	public static void addDNR(Server server2) {
		
		final ServletContextHandler handler = new ServletContextHandler(server2, "/dnr");

        log.info("Preparing api routes");
        handler.addServlet(com.itextos.beacon.platform.dnr.servlet.InitServlet.class.getName(), "/InitServlet");
        
        handler.addServlet(com.itextos.beacon.commonlib.apperrorhandler.servlets.ExceptionServlet.class.getName(), "/exceptionservlet");

        handler.addServlet(com.itextos.beacon.commonlib.apperrorhandler.servlets.ErrorServlet.class.getName(), "/errorservlet");

        handler.addServlet(com.itextos.beacon.platform.dnr.servlet.DLRReceiver.class.getName(), "/dlrreceiver");

      

	}

	


}

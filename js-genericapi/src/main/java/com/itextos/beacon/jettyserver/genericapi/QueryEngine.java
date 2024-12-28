package com.itextos.beacon.jettyserver.genericapi;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.errorlog.QPRLog;
import com.itextos.beacon.web.generichttpapi.servlet.InitializationSingleton;

public class QueryEngine
{

    public static Server     server        = null;
    private static final QPRLog log           = QPRLog.getInstance();

    public static void main(
            String[] args)
    {

        try
        {
      
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

            addGenericAPI(server);
            
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

	public static void addGenericAPI(Server server2) {
		
		final ServletContextHandler handler = new ServletContextHandler(server2, "/genericapi");

        log.info("Preparing api routes");
    //    handler.addServlet(com.itextos.beacon.web.generichttpapi.servlet.InitServlet.class.getName(), "/InitServlet");
        InitializationSingleton.getInstance();

        handler.addServlet(com.itextos.beacon.web.generichttpapi.servlet.JSONCustomReceiver.class.getName(), "/JSONCustomReceiver");

        handler.addServlet(com.itextos.beacon.web.generichttpapi.servlet.JSONGenericReceiver.class.getName(), "/JSONGenericReceiver");

        handler.addServlet(com.itextos.beacon.web.generichttpapi.servlet.QSCustomReceiver.class.getName(), "/QSCustomReceiver");

        handler.addServlet(com.itextos.beacon.web.generichttpapi.servlet.QSGenericReceiver.class.getName(), "/QSGenericReceiver");

        handler.addServlet(com.itextos.beacon.web.generichttpapi.servlet.XMLReceiver.class.getName(), "/XMLReceiver");

        handler.addServlet(com.itextos.beacon.web.migration.servlet.MJsonRequestReceiver.class.getName(), "/MJsonRequestReceiver");

        handler.addServlet(com.itextos.beacon.web.migration.servlet.MQSRequestReceiver.class.getName(), "/MQSRequestReceiver");

        handler.addServlet(com.itextos.beacon.commonlib.apperrorhandler.servlets.ExceptionServlet.class.getName(), "/exceptionservlet");

        handler.addServlet(com.itextos.beacon.commonlib.apperrorhandler.servlets.ErrorServlet.class.getName(), "/errorservlet");

	}

	


}

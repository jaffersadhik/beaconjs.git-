package com.itextos.beacon.jettyserver.fileupload;

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

    public static Server     server        = null;
   // static final Properties  mySQL_cfg_val = new Properties();
    
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

            addFileupload(server);
            
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

	public static void addFileupload(Server server2) {
		
		final ServletContextHandler handler = new ServletContextHandler(server2, "/fileupload");

        log.info("Preparing api routes");
        handler.addServlet(com.winnovature.fileuploads.servlets.CampaignTemplateDltFilesSaver.class.getName(), "/dlttemplateplaceholders");
        handler.addServlet(com.winnovature.fileuploads.servlets.CampaignTemplateFilesSaver.class.getName(), "/templateplaceholders");
        handler.addServlet(com.winnovature.fileuploads.servlets.CORSFilter.class.getName(), "/*");
        handler.addServlet(com.winnovature.fileuploads.servlets.FilesSaver.class.getName(), "/save");
        handler.addServlet(com.winnovature.fileuploads.servlets.MobileValidator.class.getName(), "/validatemobile");
        handler.addServlet(com.winnovature.fileuploads.servlets.TemplateFilesSaver.class.getName(), "/template");

    

		
	}

}

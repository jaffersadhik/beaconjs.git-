package com.itextos.beacon.jetty.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class MultiWarDeployment {

    public static boolean deploy(String module) {
        int port = 8080; // Specify the port you want Jetty to run on

        try {
            // Create Jetty server
            Server server = new Server(port);


            if(module.equals("japi")) {
            deployWar(server, "/genericapi", "/web-generichttpapi-1.0.war");
            }else if(module.equals("japi")) {
            	
            }else {
            	
                return false;

            }
            
            
            // Add more deployments as needed

            // Start the server
            server.start();
            server.join(); // This line is optional and keeps the main thread from exiting


        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;

    }

    private static void deployWar(Server server, String contextPath, String warFilePath) {
        try {
            // Create a WebAppContext for the application
            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath(contextPath);
            webAppContext.setWar(warFilePath);

            // Add the WebAppContext to the server
            server.setHandler(webAppContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



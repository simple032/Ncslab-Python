package com.ncslab.server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Application Lifecycle Listener implementation class ServerListener
 *
 */
@WebListener
public class ServerListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public ServerListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  {
         // Shutdown the SimulationServer to prevent thread leaks
        SimulationServer server = SimulationServer.instance;
        if (server != null) {
            server.shutdown();
            System.out.println("SimulationServer stopped.");
        }
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    	//System.out.println("Hello");
    	
    	SimulationServer simulationServer=SimulationServer.instance;
    	simulationServer.start();
    }
	
}

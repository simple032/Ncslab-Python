package com.ncslab.server.pythonServer;

import com.ncslab.server.octaveserver.OctaveServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class PythonServerListener implements ServletContextListener{
	public PythonServerListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  {
         // TODO Auto-generated method stub
        PythonServer server = PythonServer.instance;
        if (server != null) {
            server.stop();
            System.out.println("PythonServer stopped.");
        }
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  {
         // TODO Auto-generated method stub
    	//System.out.println("Hello");

    	PythonServer server=PythonServer.instance;
        server.start();
    }
}

package com.ncslab.server.mfcalcServer;

import com.ncslab.server.pythonServer.PythonServer;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;


@WebListener
public class MfcalcServerListener implements ServletContextListener{
	public MfcalcServerListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  {
         // TODO Auto-generated method stub
        MfcalcServer server = MfcalcServer.instance;
        if (server != null) {
            server.stop();
            System.out.println("MfcalcServer stopped.");
        }
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  {
         // TODO Auto-generated method stub
    	//System.out.println("Hello");

    	MfcalcServer server=MfcalcServer.instance;
    	server.start();
    }
}

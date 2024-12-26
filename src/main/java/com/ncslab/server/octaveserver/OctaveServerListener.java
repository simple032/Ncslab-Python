package com.ncslab.server.octaveserver;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class OctaveServerListener implements ServletContextListener{
	public OctaveServerListener() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // 释放资源
        OctaveServer server = OctaveServer.instance;
        if (server != null) {
            server.stop();
            System.out.println("OctaveServer stopped.");
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // 初始化资源
        try {
            OctaveServer server = OctaveServer.instance;
            server.start();
            System.out.println("OctaveServer started successfully.");
        } catch (Exception e) {
            System.err.println("Failed to start OctaveServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.ncslab.listener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.ncslab.block.instrument.SerialConfiguration;
import com.ncslab.util.TemplateManager;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("ApplicationLifecycleListener: Starting application shutdown cleanup...");

        try {
            // 关闭所有串口连接，释放jSerialComm.dll
            // Close all serial ports to release jSerialComm.dll native library
            SerialConfiguration.clearRegistry();
            System.out.println("ApplicationLifecycleListener: Serial ports closed successfully");
        } catch (Exception e) {
            System.err.println("ApplicationLifecycleListener: Error closing serial ports: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // 关闭TemplateManager中的线程池
            TemplateManager.shutdownExecutor();
            System.out.println("ApplicationLifecycleListener: TemplateManager shutdown successfully");
        } catch (Exception e) {
            System.err.println("ApplicationLifecycleListener: Error shutting down TemplateManager: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // 关闭MySQL连接清理线程
            AbandonedConnectionCleanupThread.checkedShutdown();
            System.out.println("ApplicationLifecycleListener: MySQL cleanup thread shutdown successfully");
        } catch (Exception e) {
            System.err.println("ApplicationLifecycleListener: Error shutting down MySQL cleanup: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("ApplicationLifecycleListener: Application shutdown cleanup completed");
    }
}

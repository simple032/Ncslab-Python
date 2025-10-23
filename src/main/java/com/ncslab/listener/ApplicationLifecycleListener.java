package com.ncslab.listener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.ncslab.util.TemplateManager;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 关闭TemplateManager中的线程池
        TemplateManager.shutdownExecutor();

        // 关闭MySQL连接清理线程
        AbandonedConnectionCleanupThread.checkedShutdown();
    }
}

package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONObject;
import java.io.*;
import java.util.*;

import code.Solver;
import code.c.CodeModelC;
import code.m.CodeModelM;

import ncslablink.ModelException;
import ncslablink.ModelMode;

/**
 * Servlet implementation class simulate
 */
@WebServlet("/simulate")
public class simulate extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public simulate() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		//读取Post的JSON配置
		InputStreamReader insr = new InputStreamReader(request.getInputStream(),"utf-8");
        String result = "";
        int respInt = insr.read();
        while(respInt!=-1) {
            result +=(char)respInt;
            respInt = insr.read();
        }  
        JSONObject jsonIn = new JSONObject(result);
		
        try {
        	//建立M语言的生成器CodeModelM
        	CodeModelM model=CodeModelM.createFromJSON(jsonIn,ModelMode.Simulation);
        	model.setSolver(Solver.ode4);
        	model.generate();
        	model.showErrorMessages();

        	System.out.println();

        	/*
        	if(model.getErrorList().size()==0) {
        		System.out.println(model.getCode());
        	}*/
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully。。。");
        }
        
        
        System.out.println();
        
        try {
        	//建立C语言的生成器CodeModelC
        	CodeModelC modelC=CodeModelC.createFromJSON(jsonIn,ModelMode.Compilation);
        	modelC.setSolver(Solver.ode4);
        	modelC.generate();

        	System.out.println();

        	if(modelC.getErrorList().size()==0) {
        		modelC.makeExeFile();
        		modelC.saveToDatabase();
        	}
        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully。。。");
        }
        
        
        /*
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("piscesPU");
        
        long s = System.currentTimeMillis();
        // 数据库连接失败这里会抛出异常
        final EntityManager em = emf.createEntityManager();
        long e = System.currentTimeMillis();
        System.out.println("连接数据库耗时: " + (e - s) + "毫秒");
        // 获取数据
        @SuppressWarnings("unchecked")
        List<main.database.Algorithms> list = em.createQuery("SELECT a FROM algorithms a").getResultList();
        int i = 0;
        for (main.database.Algorithms info : list) {
            //System.out.println("第" + (++i) + "个值为: " + info);
        }
        em.close();
        */
	}

}

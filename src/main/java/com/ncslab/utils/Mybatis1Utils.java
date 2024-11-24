package com.ncslab.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class Mybatis1Utils {
	private static SqlSessionFactory sqlSessionFactory=null;
	static {
		try {
			String osName = System.getProperty("os.name").toLowerCase();
            String environmentId = "ncslab";

            // 根据操作系统选择 environment id
//            if (osName.contains("win")) {
//                environmentId = "windows";
//            } else if (osName.contains("mac")) {
//                environmentId = "mac";
//            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
//                environmentId = "linux";
//            } else {
//                throw new UnsupportedOperationException("Unsupported OS: " + osName);
//            }

            // 加载配置文件
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);

            // 使用指定的环境 id 构建 SqlSessionFactory
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, environmentId);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static SqlSession getSqlSession() {
		return sqlSessionFactory.openSession();
	}
}


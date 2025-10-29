package com.ncslab.code.c.windows.simulation;

import com.google.common.io.LittleEndianDataInputStream;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.utils.Property;
import org.json.JSONObject;

import jakarta.websocket.Session;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

public class CodeModelCWindowsSimulation extends CodeModelC{

	private CodeStructCWindowsSimulation codeStructC = new CodeStructCWindowsSimulation(this);

	// 原有JSONObject构造函数
	CodeModelCWindowsSimulation(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	// DTO构造函数 - 现代化直接DTO支持
	CodeModelCWindowsSimulation(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	@Override
	protected CodeStructC getCodeStructC() {
		return codeStructC;
	}

	// 原有JSONObject工厂方法
	public static CodeModelCWindowsSimulation createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
        return new CodeModelCWindowsSimulation(jsonIn,mode);
	}
	
	// DTO工厂方法 - 现代化直接DTO支持
	public static CodeModelCWindowsSimulation createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
        return new CodeModelCWindowsSimulation(modelDto, mode);
	}

	private void sendSimulatingMessage(Session session, double time) throws IOException{
		JSONObject jb = new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", time);
		jb.put("timeLength", this.getConfig().getStopTime());
        if(session != null)
		    session.getBasicRemote().sendText(jb.toString());
	}

    public void copyLibraryFiles(){

    }

	public void simulate(Session session) throws ModelException {
		Process process = null;
		System.out.println("Executing simulation codes...");
		try {
			// run the executable ncslab file

            File dir = new File(codeStructC.getCodePath());
            File exeFile = new File(dir, "ncslab.exe");
            System.out.println("The executing file is on " + exeFile);

            String exeFilePath = exeFile.getAbsolutePath();

            // 获取当前环境变量
//            Map<String, String> currentEnv = System.getenv();
//
//            // 创建一个新的环境变量映射
//            Map<String, String> env = new HashMap<>(currentEnv);
//
//            // 添加或修改环境变量
//            env.put("Path", "%M2PLAB_ROOT%/server/cruntime/bin;" + env.get("Path")); // 例如，添加新的路径
//
//            // 将环境变量映射转换为字符串数组
//            String[] envArray = new String[env.size()];
//            int i = 0;
//            for (Map.Entry<String, String> entry : env.entrySet()) {
//                envArray[i++] = entry.getKey() + "=" + entry.getValue();
//            }
//            String dllFolderName = Optional.ofNullable(Property.instance.getProperty("DllFolder"))
//                .orElse(codeStructC.getM2plabRoot()+"/server/cruntime/bin");
//
//            File dllFolder = new File(dllFolderName);
//            if(!dllFolder.exists()){
//                dllFolder = new File(codeStructC.getM2plabRoot()+"/server/cruntime/bin");
//            }
//            File[] files = dllFolder.listFiles();
//            if(files != null){
//                for(File file : files){
//                    if(file.isFile() && file.getName().endsWith(".dll")){
//                        try{
//                            Path sourcePath = Paths.get(file.getAbsolutePath());
//                            Path targetPath = Paths.get(dir.getAbsolutePath(), file.getName());
//                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
//                        }catch(IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }

            // Use ProcessBuilder instead of deprecated Runtime.exec()
            ProcessBuilder processBuilder = new ProcessBuilder(
                exeFilePath,
                String.valueOf(this.getConfig().getStopTime())
            );
            processBuilder.directory(dir);
            process = processBuilder.start();

			// read primitive Java data types from an underlying InputStream in a little-endian format
			// This input stream is the stdout of the process
			LittleEndianDataInputStream out = new LittleEndianDataInputStream(process.getInputStream());

			long currentTime = new java.util.Date().getTime();

			while(true) {
                try {
                    double time = out.readDouble();

                    if (time < 0) {
                        break;
                    }

                    // send the simulation data to the client
                    sendSimulatingMessage(session, time);
                }
                // 会出现这个EOFException，但是不影响程序的运行
                // 因为EOFException是在读取完所有数据后抛出的异常
                catch (EOFException e) {
                	break;
                }

				//if((new java.util.Date().getTime())-currentTime>1000) {
				//	currentTime=new java.util.Date().getTime();
				//	sendSimulatingMessage(session,time);
				//}

				//System.out.println(time);
			}

			out.close();

			process.waitFor();
		}
		catch(InterruptedException|IOException e) {
            e.printStackTrace();
			throw new ModelException("Can not execute the exe file!");
		}
		finally {
			if(process!=null) {
				process.destroy();
			}
		}

		System.out.println("Simulation codes executed successfully!");

	}
}

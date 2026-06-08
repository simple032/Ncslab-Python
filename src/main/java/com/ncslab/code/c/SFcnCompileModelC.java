package com.ncslab.code.c;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.parquet.bytes.LittleEndianDataInputStream;
//import com.google.common.io.LittleEndianDataInputStream;
import org.json.JSONObject;

import com.ncslab.ncslablink.SFcnException;
import com.ncslab.ncslablink.SFcnModel;

public class SFcnCompileModelC extends SFcnModel{

	protected String fileName;

	public SFcnCompileModelC(JSONObject jsonData) throws SFcnException{
		super(jsonData);
	}

	public void writeSFcnFile() {
		// 生成目标文件夹的位置codePathBase/用户id/modelId
		String userPath = codePathBase + this.getUserId();

		File file = new File(userPath);
		if (file.exists() == false) {
			file.mkdir();
		}

		String modelPath = userPath + "/" + this.getModelId();
		file = new File(modelPath);
		if (file.exists() == false) {
			file.mkdir();
		}

		codePath = modelPath + "/";

		writeNCSLabFile("ncslabdefines.hpp");
		writeNCSLabFile("ncslabccode.hpp");
		writeNCSLabFile("ncslabsfun.hpp");
		writeNCSLabFile("Matrix.hpp");
		writeMainCode();
	}

	protected void writeNCSLabFile(String fileName) {
		System.out.println("Writing file " + fileName + "...");
		InputStream InputStream = this.getClass().getResourceAsStream(fileName);

		File file = new File(codePath + "/" + fileName);
		if (file.exists()) {
			return;
		}
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = InputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, len);
			}

			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void writeMainCode() {
		System.out.println("Writing file sfcncode...");

		String mainCode="\n#ifdef _S_COMPILE\n"
		+"#include <iostream>\n"
		+"#include <fstream>\n"
		+"using namespace std;\n\n"
		+"int main() {\n"
		+"SimStruct ssfunction;\n"
		+"SimStruct* sfunction=&ssfunction;\n"
		+"BLOCK sfcn;\n"
		+"double end=-1;\n"
		+"sfunction->parentBlock=&sfcn;\n"
		+"sfunction->parentBlock->parameterNum="+(getParameterNum()+1)+";\n" //+1是采样时间参数
        +"PARAMETER* parameter["+(getParameterNum()+1)+"];\n"
        +"sfunction->parentBlock->parameters=parameter;\n";
		for(int i = 0;i<getParameterNum();i++){
		    mainCode+="PARAMETER para"+i+";\n"
                +"double paravp"+i+"="+getParameterString()[i]+";\n"
                +"para"+i+".vp=&paravp"+i+";\n"
                +"parameter["+i+"]=&para"+i+";\n";
        }
		mainCode+="mdlInitializeSizes(sfunction);\n"
		+"ofstream ofs;\n"
		+"ofs.open(\"sfcncompileresult.txt\");\n"
		//sizes
		+"if(sfunction->sizes.parameterNum != (sfunction->parentBlock->parameterNum-1)){\n"
		+"ofs<<\"Error: the number of defined parameters does not match the number of parameters on the dialog.\";\n"
		+"fwrite(&end,sizeof(end),1,stdout);\n"
		+ "return 0;\n"
		+"}\n"
		+"if(sfunction->sizes.inputPortNum <= 0){\n"
		+"ofs<<\"Error: Invalid number for input ports.\";\n"
		+"fwrite(&end,sizeof(end),1,stdout);\n"
		+ "return 0;\n"
		+"}\n"
		+"if(sfunction->sizes.outputPortNum <= 0){\n"
		+"ofs<<\"Error: Invalid number for output ports.\";\n"
		+"fwrite(&end,sizeof(end),1,stdout);\n"
		+ "return 0;\n"
		+"}\n"
		+"ofs<<\"result:\"<<sfunction->sizes.parameterNum<<\";\";\n"
		+"ofs<<sfunction->sizes.inputPortNum;\n"
		//input and output
		+"for(int i = 0;i<sfunction->sizes.inputPortNum;i++){\n"
		+"if(sfunction->sizes.inputPortWidth[i]<=0){\n"
		+"ofs.close();ofs.open(\"sfcncompileresult.txt\");\n"
		+"ofs<<\"Error: Invalid width for input port \"<<(i+1);\n"
		+"fwrite(&end,sizeof(end),1,stdout);\n"
		+"return 0;\n"
		+"}\n"
		+"ofs<<(i==0? \"|\":\",\")<<sfunction->sizes.inputPortWidth[i];\n"
		+"}\n"
		+"ofs<<\";\";\n"
		+"ofs<<sfunction->sizes.outputPortNum;\n"
		+"for(int i = 0;i<sfunction->sizes.outputPortNum;i++){\n"
		+"if(sfunction->sizes.outputPortWidth[i]<=0){\n"
		+"ofs.close();ofs.open(\"sfcncompileresult.txt\");\n"
		+"ofs<<\"Error: Invalid width for output port \"<<(i+1);\n"
		+"fwrite(&end,sizeof(end),1,stdout);\n"
		+"return 0;\n"
		+"}\n"
		+"ofs<<(i==0? \"|\":\",\")<<sfunction->sizes.outputPortWidth[i];\n"
		+"}\n"
		+"ofs<<\";\";\n"
		//continuous states and discrete states
		+"ofs<<sfunction->sizes.numContStates<<\"|\"<<sfunction->sizes.numDiscStates<<\";\";\n"
		//sample times
		+"mdlInitializeSampleTimes(sfunction);\n"
		+"for(int i = 0;i<sfunction->sizes.numSampleTimes;i++){\n"
		+"if(sfunction->stInfo.sampleTimes[i] < 0 && sfunction->stInfo.sampleTimes[i]!=-1){\n"
		+"ofs.close();ofs.open(\"sfcncompileresult.txt\");\n"
		+"ofs<<\"Error: Invalid number \"<<sfunction->stInfo.sampleTimes[i]<<\" for sample time \"<<(i+1);\n"
		+"return 0;\n"
		+"}\n"
		+"ofs<<(i == 0? \"\":\",\")<<sfunction->stInfo.sampleTimes[i];\n"
		+"}\n"
		+"ofs<<\";\";\n"
		//offset times
		+"for(int i = 0;i<sfunction->sizes.numSampleTimes;i++){\n"
		+"if(sfunction->stInfo.offsetTimes[i] < 0 || sfunction->stInfo.sampleTimes[i] > 0 && sfunction->stInfo.offsetTimes[i] > sfunction->stInfo.sampleTimes[i]){;\n"
		+"ofs.close();ofs.open(\"sfcncompileresult.txt\");\n"
		+"ofs<<\"Error: Invalid number \"<<sfunction->stInfo.offsetTimes[i]<<\" for offset time \"<<(i+1);\n"
		+"return 0;\n"
		+"};\n"
		+"ofs<<(i == 0? \"\":\",\")<<sfunction->stInfo.offsetTimes[i];\n"
		+"}\n"
		+"ofs<<\";\";\n"

		+"ofs.close();\n"
		+"fwrite(&end,sizeof(end),1,stdout);\n"
		+"return 0;\n"
		+"}\n"
		+"#endif\n";

		functionCode+=mainCode;
		fileName=this.getFunctionName() + "_" + this.getBlockName().replace("S-Function", "");
		File file = new File(codePath + fileName + ".cpp");
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(functionCode.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean makeExeFile() {
		try {
			System.out.println("Making execute file...");
			System.out.println(codePath);
			String m2plabRoot = System.getenv("M2PLAB_ROOT");
			if (m2plabRoot == null || m2plabRoot.trim().isEmpty()) {
				m2plabRoot = "/data/M2PLab";
			}
			String exeString="cl.exe /nologo /EHsc /MD /O2 /std:c++14 /D_S_COMPILE /I\""
					+ m2plabRoot + "/server/cruntime/include\" /Fe:" + fileName + ".exe " + fileName + ".cpp";
			System.out.println(exeString);

			List<String> clCommand = new ArrayList<>();
			clCommand.add("cl.exe");
			clCommand.add("/nologo");
			clCommand.add("/EHsc");
			clCommand.add("/MD");
			clCommand.add("/O2");
			clCommand.add("/std:c++14");
			clCommand.add("/D_S_COMPILE");
			clCommand.add("/I" + m2plabRoot + "/server/cruntime/include");
			clCommand.add("/Fe:" + fileName + ".exe");
			clCommand.add(fileName+".cpp");

			ProcessBuilder processBuilder;
			String vsDevCmd = isWindowsHost() ? findVsDevCmd() : null;
			if (vsDevCmd != null) {
				processBuilder = new ProcessBuilder("cmd.exe", "/d", "/c",
						"call \"" + vsDevCmd + "\" -arch=x64 -host_arch=x64 >nul && " + joinCommandForCmd(clCommand));
			} else {
				processBuilder = new ProcessBuilder(clCommand);
			}
			processBuilder.directory(new File(codePath));
			Process process = processBuilder.start();

            // 创建线程读取标准输出和错误输出
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

            // 启动线程
            outputGobbler.start();
            errorGobbler.start();

            // 等待进程完成
            int exitCode = process.waitFor();

            // 确保所有输出都被读取
            outputGobbler.join();
            errorGobbler.join();

            if(exitCode == 0) {
                return true;
            }
//			//读取OutputStream和errStream。如果读取不及时，会出现阻塞
//			BufferedReader in=new BufferedReader(new InputStreamReader(process.getErrorStream()));
//			BufferedReader inOut=new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String line=null,outLine=null;
//			StringBuilder errStr=new StringBuilder();
//			StringBuilder outStr=new StringBuilder();
//
//			while((outLine=inOut.readLine())!=null||(line=in.readLine())!=null) {
//				if(outLine!=null) {
//					outStr.append(outLine);
//					System.out.println(outLine);
//				}
//				if(line!=null) {
//					errStr.append(line);
//					System.err.println(line);
//				}
//			}
//			//等待makefile的完成
//			process.waitFor();
//
//			if(process.exitValue()==0) {
//				return true;
//			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean isWindowsHost() {
		return System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("win");
	}

	private static String joinCommandForCmd(List<String> commandParts) {
		StringBuilder sb = new StringBuilder();
		for (String part : commandParts) {
			if (part == null || part.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(' ');
			}
			if (part.indexOf(' ') >= 0 || part.indexOf('&') >= 0 || part.indexOf('(') >= 0 || part.indexOf(')') >= 0) {
				sb.append('"').append(part.replace("\"", "\\\"")).append('"');
			} else {
				sb.append(part);
			}
		}
		return sb.toString();
	}

	private static String findVsDevCmd() {
		String envPath = System.getenv("VSDEVCMD_PATH");
		if (isExistingFile(envPath)) {
			return envPath;
		}

		String vsInstallDir = System.getenv("VSINSTALLDIR");
		String fromInstallDir = vsInstallDir == null ? null : vsInstallDir + File.separator + "Common7"
				+ File.separator + "Tools" + File.separator + "VsDevCmd.bat";
		if (isExistingFile(fromInstallDir)) {
			return fromInstallDir;
		}

		File vswhere = new File("C:\\Program Files (x86)\\Microsoft Visual Studio\\Installer\\vswhere.exe");
		if (!vswhere.isFile()) {
			return null;
		}
		try {
			Process process = new ProcessBuilder(
					vswhere.getAbsolutePath(),
					"-latest",
					"-products", "*",
					"-requires", "Microsoft.VisualStudio.Component.VC.Tools.x86.x64",
					"-property", "installationPath").start();
			String installPath;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				installPath = reader.readLine();
			}
			int exitCode = process.waitFor();
			if (exitCode != 0 || installPath == null || installPath.trim().isEmpty()) {
				return null;
			}
			String devCmd = installPath.trim() + File.separator + "Common7" + File.separator + "Tools"
					+ File.separator + "VsDevCmd.bat";
			return isExistingFile(devCmd) ? devCmd : null;
		} catch (Exception e) {
			return null;
		}
	}

	private static boolean isExistingFile(String path) {
		return path != null && !path.trim().isEmpty() && new File(path).isFile();
	}

	public void compile() throws SFcnException{
		Process process = null;
		System.out.println("Executing compile codes...");
		try {
			// Use ProcessBuilder instead of deprecated Runtime.exec()
			ProcessBuilder processBuilder = new ProcessBuilder(codePath+"\\"+fileName+".exe");
			processBuilder.directory(new File(codePath));
			process = processBuilder.start();
//			LittleEndianDataInputStream out = new LittleEndianDataInputStream(process.getInputStream());
//
//			while(true) {
//				double time=out.readDouble();
//				if(time<0){
//					break;
//				}
//			}
//
//			out.close();
//			try {
//				process.waitFor();
//			} catch (Exception e) {
//				throw new SFcnException("Can not execute the exe file!");
//			}
			com.google.common.io.LittleEndianDataInputStream out = new com.google.common.io.LittleEndianDataInputStream(process.getInputStream());

            while(true) {
                try {
                    double time = out.readDouble();
                    if (time < 0) {
                        break;
                    }
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
		}catch (InterruptedException|IOException e) {
            e.printStackTrace();
			throw new SFcnException("Can not execute the exe file!");
		}finally {
			if(process!=null)
				process.destroy();
		}

		System.out.println("Compile codes executed successfully!");
	}
}

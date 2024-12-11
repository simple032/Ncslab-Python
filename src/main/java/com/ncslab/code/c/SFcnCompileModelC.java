package com.ncslab.code.c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
		+"mdlInitializeSizes(sfunction);\n"
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
		+"for(int i = 0;i<sfunction->sizes.numSampleTimes;i++){;\n"
		+"if(sfunction->stInfo.sampleTimes[i] < 0 && sfunction->stInfo.sampleTimes[i]!=-1){;\n"
		+"ofs.close();ofs.open(\"sfcncompileresult.txt\");\n"
		+"ofs<<\"Error: Invalid number \"<<sfunction->stInfo.sampleTimes[i]<<\" for sample time \"<<(i+1);\n"
		+"return 0;\n"
		+"};\n"
		+"ofs<<(i == 0? \"\":\",\")<<sfunction->stInfo.sampleTimes[i];\n"
		+"};\n"
		+"ofs<<\";\";\n"
		//offset times
		+"for(int i = 0;i<sfunction->sizes.numSampleTimes;i++){;\n"
		+"if(sfunction->stInfo.offsetTimes[i] < 0 || sfunction->stInfo.sampleTimes[i] > 0 && sfunction->stInfo.offsetTimes[i] > sfunction->stInfo.sampleTimes[i]){;\n"
		+"ofs.close();ofs.open(\"sfcncompileresult.txt\");\n"
		+"ofs<<\"Error: Invalid number \"<<sfunction->stInfo.offsetTimes[i]<<\" for offset time \"<<(i+1);\n"
		+"return 0;\n"
		+"};\n"
		+"ofs<<(i == 0? \"\":\",\")<<sfunction->stInfo.offsetTimes[i];\n"
		+"};\n"
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
			String exeString="g++ -D_S_COMPILE -I /opt/eigen-3.4.0 -fpermissive -o"+fileName+" "+fileName+".cpp";
			Process process=Runtime.getRuntime().exec(exeString,null,new File(codePath));
			
			//读取OutputStream和errStream。如果读取不及时，会出现阻塞
			BufferedReader in=new BufferedReader(new InputStreamReader(process.getErrorStream()));
			BufferedReader inOut=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line=null,outLine=null;
			StringBuilder errStr=new StringBuilder();
			StringBuilder outStr=new StringBuilder();

			while((outLine=inOut.readLine())!=null||(line=in.readLine())!=null) {
				if(outLine!=null) {
					outStr.append(outLine);
					System.out.println(outLine);
				}
				if(line!=null) {
					errStr.append(line);
					System.err.println(line);
				}
			}
			//等待makefile的完成
			process.waitFor();

			if(process.exitValue()==0) {
				return true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void compile() throws SFcnException{
		Process process = null;
		System.out.println("Executing compile codes...");
		try {
			process = Runtime.getRuntime().exec("./"+fileName,null,new File(codePath));
			LittleEndianDataInputStream out = new LittleEndianDataInputStream(process.getInputStream());
			
			while(true) {
				double time=out.readDouble();
				if(time<0){
					break;
				}
			}
			
			out.close();
			try {
				process.waitFor();
			} catch (Exception e) {
				throw new SFcnException("Can not execute the exe file!");
			}
		}catch (IOException e) {
			throw new SFcnException("Can not execute the exe file!");
		}finally {
			if(process!=null)
				process.destroy();
		}
		
		System.out.println("Compile codes executed successfully!");
	}
}

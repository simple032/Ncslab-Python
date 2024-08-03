package code.c.stm;

import java.util.Vector;
import java.io.*;

import code.CodeModel;
import block.Block;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import block.io.State;
import block.io.InputPort;

import code.c.CodeStructC;
import code.c.CodeModelC;

public class CodeStructCStm32 extends CodeStructC{
	public CodeStructCStm32(CodeModelC model) {
		super(model);
	}
	
	public void writeCCodeFiles() {

		//锟斤拷锟斤拷目锟斤拷锟侥硷拷锟叫碉拷位锟斤拷codePathBase/锟矫伙拷id/modelId
		String userPath=codePathBase+model.getUserId();

		File file=new File(userPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		String modelPath=userPath+"/"+model.getModelId()+"_RT_STM";
		file=new File(modelPath);
		if(file.exists()==false) {
			file.mkdir();
		}
		
		String modelNcslabPath=modelPath+"/ncslab";
		file=new File(modelNcslabPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		codePath=modelNcslabPath+"/";//modelPath+"/";

		//写锟斤拷锟杰边碉拷锟斤拷源锟侥硷拷
//		//makefile
//		writeNCSLabFile("makefile");
//		writeStm32Makefile("Makefile");
		//锟斤拷锟斤拷锟捷结构
//		writeNCSLabFile("../ncslabccode.h","ncslabccode.h");
//		writeNCSLabFile("../ncslabmain.c","ncslabmain.c");
//		writeNCSLabFile("../DataApi.c","DataApi.c");
//		writeNCSLabFile("../DataApi.h","DataApi.h");
//		writeNCSLabFile("../util.c","util.c");
//		writeNCSLabFile("../ncslabdefines.h","ncslabdefines.h");
		
		writeNCSLabFile("ncslabccode.h");
		writeNCSLabFile("ncslabmain.c");
		writeNCSLabFile("DataApi.c");
		writeNCSLabFile("DataApi.h");
		writeNCSLabFile("util.c");
		writeNCSLabFile("ncslabdefines.h");

//		//实锟斤拷Netcon协锟斤拷锟酵拷锟斤拷募锟�
//		writeNCSLabFile("./ServerThread.c","ServerThread.c");
//		writeNCSLabFile("./ServerThread.h","ServerThread.h");
//		writeNCSLabFile("./ClientThread.c","ClientThread.c");
//		writeNCSLabFile("./ClientThread.h","ClientThread.h");
//		writeNCSLabFile("./UploadThread.c","UploadThread.c");
//		writeNCSLabFile("./UploadThread.h","UploadThread.h");

				
		
//		writeNCSLabStm32File("./stm/",0);

		//写锟斤拷锟斤拷锟缴碉拷锟斤拷锟斤拷锟斤拷ncslabccdoe.c
		writeMainCodeFileStm32();
//		writeMainCodeFileStm();
		
		wirteDefineFile();
		
		switch(model.getSolver()) {
		case ode1:
			writeNCSLabFile("./ode1.c","onestep.c",true);
			break;
		case ode2:
			writeNCSLabFile("./ode2.c","onestep.c",true);
			break;
		case ode3:
			writeNCSLabFile("./ode3.c","onestep.c",true);
			break;
		case ode4:
			writeNCSLabFile("./ode4.c","onestep.c",true);
			break;
		}
		

		File f1 = new File(this.getClass().getResource("Core").getPath());
		File f2 = new File(this.getClass().getResource("Drivers").getPath());
		File f3 = new File(this.getClass().getResource("HARDWARE").getPath());
		File f4 = new File(this.getClass().getResource("LWIP").getPath());
		File f5 = new File(this.getClass().getResource("Middlewares").getPath());
		File f6 = new File(this.getClass().getResource("SERVER").getPath());
		File f7 = new File(this.getClass().getResource("build").getPath());
		codePath=modelPath+"/";
		File nf = new File(codePath); // 要复制到的地方
		try {
			copy(f1, nf, true);
			copy(f2, nf, true);
			copy(f3, nf, true);
			copy(f4, nf, true);
			copy(f5, nf, true);
			copy(f6, nf, true);
			copy(f7, nf, true);
			writeNCSLabFile("./Makefile");
			writeNCSLabFile("./startup_stm32h750xx.s");
			writeNCSLabFile("./STM32H750XBHx_FLASH.ld");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//System.out.println("codePathBase:"+codePathBase);
		//System.out.println("userPath:"+userPath);
		//System.out.println("modelPath:"+modelPath);
		//System.out.println("codePath: "+codePath);
		
		if("deploy".equals(utils.Property.instance.getProperty("mode").trim())==true) {
			try {
				//Runtime.getRuntime().exec("python /home/pi/.config/antostart/GetPiId.py");
				String command = "sudo chmod -R 777 " + codePathBase;
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
 
	/**
	 * 创建目录
	 * @param name
	 * @return
	 */
	public static boolean Createflies(String name) {
		boolean flag=false;
		File file=new File(name);
		//创建目录
		if(file.mkdir() == true){
			System.out.println("文件夹创建成功！");
			flag=true;
		}else {
			System.out.println("文件夹创建失败！");
			flag=false;
			
		}
		
		return flag;
	}

	
	public byte[] readExeFile() {
		return readFile("/build/ncslabForStm32.hex");
	}
	
	public boolean makeExeFile() {
		try {
			//锟斤拷锟斤拷make锟斤拷锟斤拷锟缴匡拷执锟叫达拷锟斤拷
//			Process process=Runtime.getRuntime().exec("make", null, new File(codePath));
//			//锟斤拷取OutputStream锟斤拷errStream锟斤拷锟斤拷锟斤拷锟饺★拷锟斤拷锟绞憋拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
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
//			process.waitFor();
			
//			System.out.println("Current working directory: " + System.getProperty("user.dir"));
//			System.out.println("Current codePath directory: " + codePath);
			Process process2;
			ProcessBuilder pb;
			if("deploy".equals(utils.Property.instance.getProperty("mode").trim())==true) {
				Process process= Runtime.getRuntime().exec("sudo make clean", null, new File(codePath));
				process.waitFor();
//				process2=Runtime.getRuntime().exec("sudo make -j", null, new File(codePath));
				pb = new ProcessBuilder("sudo make", "-j");
			}else {
				Process process= Runtime.getRuntime().exec("make clean", null, new File(codePath));
				process.waitFor();
//				process2=Runtime.getRuntime().exec("make -j", null, new File(codePath));
				pb = new ProcessBuilder("make", "-j");
			}

//			BufferedReader in2=new BufferedReader(new InputStreamReader(process2.getErrorStream()));
//			BufferedReader inOut2=new BufferedReader(new InputStreamReader(process2.getInputStream()));
//			String line2=null,outLine2=null;
//			StringBuilder errStr2=new StringBuilder();
//			StringBuilder outStr2=new StringBuilder();
//
//			while((outLine2=inOut2.readLine())!=null||(line2=in2.readLine())!=null) {
//				if(outLine2!=null) {
//					outStr2.append(outLine2);
//					System.out.println(outLine2);
//				}
//				if(line2!=null) {
//					errStr2.append(line2);
//					System.err.println(line2);
//				}
//			}
//
//			process2.waitFor();
//			if(process2.exitValue()==0) {
//				return true;
//			}
			
			
			
			// 创建 ProcessBuilder 对象，传入命令及参数列表
            pb = new ProcessBuilder("make", "-j");
            // 设置工作目录
            pb.directory(new File(codePath));
            // 合并标准错误和标准输出
            pb.redirectErrorStream(true);
            // 启动外部进程
            Process process = pb.start();
            // 读取进程的标准输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // 等待外部进程结束，并获取其退出状态
            int exitCode = process.waitFor();
            System.out.println("External process exited with code: " + exitCode);
            // 关闭流
            reader.close();
            if(process.exitValue()==0) {
				return true;
			}
            
//			Process process3=Runtime.getRuntime().exec("make update", null, new File(codePath));
	
//			BufferedReader in3=new BufferedReader(new InputStreamReader(process3.getErrorStream()));
//			BufferedReader inOut3=new BufferedReader(new InputStreamReader(process3.getInputStream()));
//			String line3=null,outLine3=null;
//			StringBuilder errStr3=new StringBuilder();
//			StringBuilder outStr3=new StringBuilder();
//
//			while((outLine3=inOut3.readLine())!=null||(line3=in3.readLine())!=null) {
//				if(outLine3!=null) {
//					outStr3.append(outLine3);
//					System.out.println(outLine3);
//				}
//				if(line3!=null) {
//					errStr3.append(line3);
//					System.err.println(line3);
//				}
//			}
//			
//			
//			process3.waitFor();

			

		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}

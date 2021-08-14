package code.c;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.CodeModel;
import line.Line;
import ncslablink.ErrorMessage;

public class CodeModelC extends CodeModel {
	
	private static final String REAL="real_t";
	
	private CodeStructC code=new CodeStructC(this);
	
	//生成代码的时候统计singal和parameter的个数
	private int signalNum=0;
	private int parameterNum=0;
	private int stateNum=0;
	
	CodeModelC(JSONObject jsonIn){
		super(jsonIn);
	}
	
	public void setSignalNum(int signalNum) {
		this.signalNum=signalNum;
	}
	
	public int getSignalNum() {
		return this.signalNum;
	}
	
	public void setParameterNum(int parameterNum) {
		this.parameterNum=parameterNum;
	}
	
	public int getParameterNum() {
		return this.parameterNum;
	}
	
	public void setStateNum(int stateNum) {
		this.stateNum=stateNum;
	}
	
	public int getStateNum() {
		return this.stateNum;
	}
	
	public static CodeModelC createFromJSON(JSONObject jsonIn) {
		CodeModelC model=new CodeModelC(jsonIn);
		
		return model;
	}
	
	public void generate() {
		super.generate();
		writeCCodeFiles(); 
	}
	
	/*将代码变成C语言的一系列文件 */
	private void writeCCodeFiles() {
		code.writeCCodeFiles();
	}
	
	protected void generateInitCode() {
		System.out.println("Generating init codes......");
		for(Block block:blockList) {
			System.out.println("Generating init codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			block.generateBlockInitCodeC(code);
		}
		
		code.generateIncludeCode();
		code.writeCCodeFiles();
		code.generateParameterDefineCode(); 
		code.generateStateDefineCode();
		code.generateOutputSignalDefineCode(); 
		code.gnenrateDataStructureCode();
	}
	
	protected void generateBlockOutputCode(Block block) {
		block.generateBlockOutputCodeC(code);
	}
	
	protected void generateBlockUpdateCode(Block block) {
		block.generateBlockUpdateCodeC(code);
	}
	
	protected void generateUpdateCode() {
		System.out.println("Generating update codes......");
		
		for(Block block:blockList) {
			System.out.println("Generating update codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			generateBlockUpdateCode(block);
		}
	}
	
	/*调用make命令，生成可执行代码 */
	public void makeExeFile() {
		System.out.println("Making exe file ncslabccode.exe...");
		if(code.makeExeFile()) {
			System.out.println("Exe file ncslabccode.exe created!");
		}
		else {
			System.out.println("Cannot create exe file ncslabccode.exe!");
		}
	}

}

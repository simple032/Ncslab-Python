package code.m;

import org.json.JSONObject;
import java.util.Vector;

import ncslablink.ErrorMessage;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import ncslablink.NCSLabModel;
import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.CodeGenerationOption;
import code.CodeModel;
import line.Line;

public class CodeModelM extends CodeModel{
	
	private CodeStructM code=new CodeStructM(this);
	
	CodeModelM(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	public static CodeModelM createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException{
		CodeModelM model=new CodeModelM(jsonIn,mode);
		
		return model;
	}
	
	public String getCode() {
		return code.getMainCode();
	}

	//鐢熸垚浠ｇ爜鐨勬柟娉曪紝浣跨敤涓婁竴绾х殑灏卞彲浠ヤ簡
	public void generate() {
		super.generate();
		writeMCodeFiles();
	}
	
	/*将代码变成M语言的一系列文件 */
	private void writeMCodeFiles() {
		code.writeMCodeFiles();
	}
	
	protected void generateUpdateCode(CodeGenerationOption option) {
		System.out.println("Generating update mainCodes......");
		
		
		for(Block block:blockList) {
			System.out.println("Generating update mainCodes for ("+block.getBlockId()+")"+block.getBlockName());
			block.generateBlockUpdateCodeM(code);
		}
	}
	
	protected void generateInitCode(CodeGenerationOption option) {
		System.out.println("Generating init mainCodes......");
		
		for(Block block:blockList) {
			System.out.println("Generating init mainCodes for ("+block.getBlockId()+")"+block.getBlockName());
			
			block.generateBlockInitCodeM(code);
		}
		//mainCode+="for t="+this.getConfig().getStartTime()+":"+this.getConfig().getFixedStep()+":"+this.getConfig().getStopTime()+"\n";
	}
	
	
	protected void generateBlockOutputCode(Block block,CodeGenerationOption option) {
		block.generateBlockOutputCodeM(code);
	}
	
	protected void generateDerivativeCode(CodeGenerationOption option) {
		System.out.println("Generating derivative mainCodes......");
		
		for(Block block:blockList) {
			System.out.println("Generating derivative mainCodes for ("+block.getBlockId()+")"+block.getBlockName());
			
			block.generateBlockDerivativeCodeM(code);
		}
	}
	
}

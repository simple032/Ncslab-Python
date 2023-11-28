package com.ncslab.code.m;

import org.json.JSONObject;
import com.ncslab.ncslablink.*;
import com.ncslab.block.Block;
import com.ncslab.code.CodeGenerationOption;
import com.ncslab.code.CodeModel;

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
	
	public String getCodePath() {
		return code.getCodePath();
	}

	//生成代码的方法，使用上一级的就可以了
	public void generate() {
		super.generate();
		writeMCodeFiles();
	}
	
	/*组装成M文件 */
	private void writeMCodeFiles() {
		code.writeMCodeFiles();
	}
	
	protected void generateStatementCode(CodeGenerationOption option) {
		
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
	
	protected void generateTerminateCode(CodeGenerationOption option) {
		
	}
	
	protected void generateDerivativeCode(CodeGenerationOption option) {
		System.out.println("Generating derivative mainCodes......");
		
		for(Block block:blockList) {
			System.out.println("Generating derivative mainCodes for ("+block.getBlockId()+")"+block.getBlockName());
			
			block.generateBlockDerivativeCodeM(code);
		}
	}

	@Override
	protected void generateBlockSinkOutputCode(Block block, CodeGenerationOption option) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'generateBlockSinkOutputCode'");
	}

	@Override
	protected void generateArraysCode(CodeGenerationOption option) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'generateArraysCode'");
	}

	@Override
	protected void generateDiscreteUpdateCode(CodeGenerationOption option) throws MatDimException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'generateDiscreteUpdateCode'");
	}
	
}

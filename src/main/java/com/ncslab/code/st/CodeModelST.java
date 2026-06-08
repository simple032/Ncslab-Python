package com.ncslab.code.st;

import com.ncslab.block.Block;
import com.ncslab.code.CodeGenerationOption;
import com.ncslab.code.CodeModel;

import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelST extends CodeModel {

    private CodeStructST code = new CodeStructST(this);

    protected CodeModelST(JSONObject jsonIn, ModelMode mode) throws ModelException {
        super(jsonIn, mode);
    }

    @Override
    protected void generatorCircuitGloablCode(CodeGenerationOption option) {
        // ST generation does not emit circuit2 native support code.
    }

    @Override
    protected void generatorCircuitOutputCode(CodeGenerationOption option) {
        // ST generation does not emit circuit2 native support code.
    }

    @Override
    protected void generatorCircuitUpdateCode(CodeGenerationOption option) {
        // ST generation does not emit circuit2 native support code.
    }

    public static CodeModelST createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException{
		CodeModelST model=new CodeModelST(jsonIn,mode);

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
		writeSTCodeFiles();
	}

	/*组装成ST文件 */
	private void writeSTCodeFiles() {
		code.writeSTCodeFiles();
	}

	protected void generateStatementCode(CodeGenerationOption option) {

	}

	protected void generateUpdateCode(CodeGenerationOption option) {
		System.out.println("Generating update mainCodes......");


		for(Block block:getBlockList()) {
			System.out.println("Generating update mainCodes for ("+block.getBlockId()+")"+block.getBlockName());
			block.generateBlockUpdateCodeST(code);
		}
	}

	protected void generateInitCode(CodeGenerationOption option) {
		System.out.println("Generating init mainCodes......");

		for(Block block:getBlockList()) {
			System.out.println("Generating init mainCodes for ("+block.getBlockId()+")"+block.getBlockName());

			block.generateBlockInitCodeST(code);
		}
		//mainCode+="for t="+this.getConfig().getStartTime()+":"+this.getConfig().getFixedStep()+":"+this.getConfig().getStopTime()+"\n";
	}


	protected void generateBlockOutputCode(Block block,CodeGenerationOption option) {
		block.generateBlockOutputCodeST(code);
	}

    @Override
    protected void generateBlockSinkOutputCode(Block block, CodeGenerationOption option) {

    }

    @Override
	protected void generateArraysCode(CodeGenerationOption option) {
		// todo:
	}

	protected void generateTerminateCode(CodeGenerationOption option) {

	}

    @Override
    protected void generateDiscreteUpdateCode(CodeGenerationOption option) throws MatDimException {

    }

    @Override
	protected void generateDerivativeCode(CodeGenerationOption option) {
		// TODO Auto-generated method stub
		// Default implementation - no operation needed
	}


}

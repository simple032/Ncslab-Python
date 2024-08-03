package code.plc;

import block.Block;
import code.CodeGenerationOption;
import code.CodeModel;

import ncslablink.MatDimException;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelPLC extends CodeModel {

    private CodeStructPLC code = new CodeStructPLC(this);

    protected CodeModelPLC(JSONObject jsonIn, ModelMode mode) throws ModelException {
        super(jsonIn, mode);
    }

    public static CodeModelPLC createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException{
		CodeModelPLC model=new CodeModelPLC(jsonIn,mode);
		
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
		writePLCCodeFiles();
	}
	
	/*组装成PLC文件 */
	private void writePLCCodeFiles() {
		code.writePLCCodeFiles();
	}
	
	protected void generateStatementCode(CodeGenerationOption option) {
		
	}
	
	protected void generateUpdateCode(CodeGenerationOption option) {
		System.out.println("Generating update mainCodes......");
		
		
		for(Block block:blockList) {
			System.out.println("Generating update mainCodes for ("+block.getBlockId()+")"+block.getBlockName());
			block.generateBlockUpdateCodePLC(code);
		}
	}
	
	protected void generateInitCode(CodeGenerationOption option) {
		System.out.println("Generating init mainCodes......");
		
		for(Block block:blockList) {
			System.out.println("Generating init mainCodes for ("+block.getBlockId()+")"+block.getBlockName());
			
			block.generateBlockInitCodePLC(code);
		}
		//mainCode+="for t="+this.getConfig().getStartTime()+":"+this.getConfig().getFixedStep()+":"+this.getConfig().getStopTime()+"\n";
	}
	
	
	protected void generateBlockOutputCode(Block block,CodeGenerationOption option) {
		block.generateBlockOutputCodePLC(code);
	}

	@Override
	protected void generateArraysCode(CodeGenerationOption option) {
		// todo:
	}

	protected void generateTerminateCode(CodeGenerationOption option) {
		
	}

	@Override
	protected void generateDerivativeCode(CodeGenerationOption option) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'generateDerivativeCode'");
	}

	
}

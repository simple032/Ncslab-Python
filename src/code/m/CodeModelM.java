package code.m;

import org.json.JSONObject;
import java.util.Vector;

import ncslablink.ErrorMessage;
import ncslablink.NCSLabModel;
import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.CodeModel;
import line.Line;

public class CodeModelM extends CodeModel{
	
	private String code="";
	CodeModelM(JSONObject jsonIn){
		super(jsonIn);
	}
	
	public static CodeModelM createFromJSON(JSONObject jsonIn) {
		CodeModelM model=new CodeModelM(jsonIn);
		
		return model;
	}
	
	public String getCode() {
		return this.code;
	}
	
	protected void generateUpdateCode() {
		System.out.println("Generating update codes......");
		
		for(Block block:blockList) {
			System.out.println("Generating update codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			this.code+=block.generateBlockUpdateCodeM();
		}
		code+="end\n";
	}
	
	protected void generateInitCode() {
		System.out.println("Generating init codes......");
		for(Block block:blockList) {
			System.out.println("Generating init codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			this.code+=block.generateBlockInitCodeM();
		}
		code+="for t="+this.getConfig().getStartTime()+":"+this.getConfig().getFixedStep()+":"+this.getConfig().getStopTime()+"\n";
	}
	
	
	protected void generateBlockOutputCode(Block block) {
		code+=block.generateBlockOutputCodeM();
	}	
	
}

package com.ncslab.block.io;

import java.util.ArrayList;
import java.util.List;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.line.Line;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.velocity.VelocityContext;

public class OutputPort {

    @Getter
	private Block block;

    // TODO Auto-generated method stub
    //outputSignalC.setWidth(width);
    @Setter
    @Getter
    private int width=1;
    // TODO Auto-generated method stub
    @Setter
    @Getter
    private int height=1;

    @Getter
    private int number;

    @Getter
    private List<Line> linkedLineList = new ArrayList<>();

	private boolean isFeedThrough=false;

	private boolean isDimThrough=true;

    private boolean isCodeGenerated=false;

    private boolean isDimScaned=false;

	@Setter
    @Getter
    private OutputSignal outputSignalC=null;

    @Setter
    @Getter
    private String name;

    VelocityContext context = new VelocityContext();

	public OutputPort(Block block,int number){
		this.block=block;
		this.number=number;

		// Use UUID if available, otherwise fall back to block ID
		String blockUUID = "null"; // placeholder for UUID
		if (blockUUID != null && !blockUUID.equals("null") && !blockUUID.isEmpty()) {
			this.name = "_Block" + blockUUID.replace("-", "_") + "_out" + number;
		} else {
			this.name = "out" + number;
		}
	}

	public OutputPort(Block block,int number,boolean isFeedThrough){
		this.block=block;
		this.number=number;

		// Use UUID if available, otherwise fall back to block ID
		String blockUUID = "null"; // placeholder for UUID
		if (blockUUID != null && !blockUUID.equals("null") && !blockUUID.isEmpty()) {
			this.name = "_Block" + blockUUID.replace("-", "_") + "_out" + number;
		} else {
			this.name = "out" + number;
		}
		this.isFeedThrough=isFeedThrough;
		System.out.println("OutputPort created for block " + block.getBlockName() +
		                 " port#" + number + ", isFeedThrough=" + isFeedThrough +
		                 ", isDimThrough=" + this.isDimThrough);
	}

	public OutputPort(Block block,String name,int number,boolean isFeedThrough){
		this.block=block;
		this.number=number;

		// Use UUID if available, otherwise fall back to block ID
		String blockUUID = "null"; // Placeholder for UUID retrieval logic
		if (blockUUID != null && !blockUUID.equals("null") && !blockUUID.isEmpty()) {
			this.name = "Block" + blockUUID.replace("-", "_") + "_" + name;
		} else {
			this.name = name;
		}
		this.isFeedThrough=isFeedThrough;
	}

    public void addLinkedLine(Line linkedLine) {
		this.linkedLineList.add(linkedLine);
	}

	public boolean getFeedThrough() {
		return this.isFeedThrough;
	}

	public void setFeedThrough(boolean feedThrough) {
		this.isFeedThrough=feedThrough;
	}

	public boolean getDimThrough() {
		return this.isDimThrough;
	}

	public void setDimThrough(boolean isDimThrough) {
		this.isDimThrough=isDimThrough;
	}

	public boolean getIsCodeGenerated() {
		return this.isCodeGenerated;
	}

	public boolean getIsDimScaned() {
		return this.isDimScaned;
	}

    public void setIsCodeGenerated(boolean isCodeGenerated) {      this.isCodeGenerated=isCodeGenerated;}

    public void setIsDimScaned(boolean isDimScaned) {        this.isDimScaned=isDimScaned;    }

    public String getDataStructureInitCodeC() {

        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("blockId", block.getBlockId());
        context.put("blockName", block.getBlockName());
        context.put("blockPath", block.getBlockPath());
        context.put("signal", outputSignalC);
        context.put("number", getNumber());
        context.put("name", getName());

        return TemplateManager.renderTemplate("c/io/OutputPort/init.vm", context);
	}

    public void setData(Data data){
        if(data.getDataType()==DataType.MATRIX) {
            outputSignalC.setValue(data.getMatrix());
        }else if(data.getDataType()==DataType.REAL) {
        	outputSignalC.setValue(data.getInitValue());
        }
    }

    /**
     * Generate C code for OUTPUT_PORT data structure definition
     * @param code The CodeStructC instance to add the generated code to
     */
    public void generateDataStructureCodeC(CodeStructC code) {
        context.put("blockId", block.getBlockId());
        context.put("portNumber", number);
        context.put("portName", name);
        context.put("portWidth", width);
        
        String structCode = TemplateManager.renderTemplate("c/io/OutputPort/dataStructure.vm", context);
        code.dataStructureCode += structCode + "\n";
    }

    /**
     * Generate C code for signal structure definition related to this output port
     * @param code The CodeStructC instance to add the generated code to
     */
    public void generateSignalStructureCodeC(CodeStructC code) {
        context.put("blockId", block.getBlockId());
        context.put("portNumber", number);
        context.put("portName", name);
        context.put("portWidth", width);
        context.put("portHeight", height);
        
        String signalCode = TemplateManager.renderTemplate("c/io/OutputPort/signalStructure.vm", context);
        code.dataStructureCode += signalCode + "\n";
    }

}

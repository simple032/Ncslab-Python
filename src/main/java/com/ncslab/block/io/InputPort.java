package com.ncslab.block.io;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.line.Line;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.data.Data;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.velocity.VelocityContext;

public class InputPort {

	private OutputPort linkedOutputPort;

	@Getter
	private Block block;

    @Getter
    private int number;

    @Getter
    private String name;

    @Setter
    @Getter
    private Line linkedLine=null;

    VelocityContext context = new VelocityContext();

	public InputPort(Block block,int number){
		this.block=block;

		this.number=number;

		this.linkedOutputPort=null;

		// Use UUID if available, otherwise fall back to block ID
		String blockUUID = "null"; // placeholder for UUID
		if(block instanceof com.ncslab.block.sink.Scope) {
			// For Scope, use simple name without suffix
			if (blockUUID != null && !blockUUID.equals("null") && !blockUUID.isEmpty()) {
				this.name = "_Block" + blockUUID.replace("-", "_");
			} else {
				this.name = "_Block" + block.getBlockId();
			}
		} else {
			// For other blocks, add input port suffix
			if (blockUUID != null && !blockUUID.equals("null") && !blockUUID.isEmpty()) {
				this.name = "_Block" + blockUUID.replace("-", "_") + "_in" + number;
			} else {
				this.name = "in" + number;
			}
		}
	}

    public int getWidth() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		return signal.getWidth();
	}

	public int getHeight() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		return signal.getHeight();
	}

	public boolean isVector() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(signal.getDataType()==DataType.MATRIX&&signal.getWidth()==1||signal.getHeight()==1) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isReal() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(signal.getDataType()==DataType.REAL) {
			return true;
		}
		else {
			return false;
		}
	}

	public int getVectorSize() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(signal.getWidth()>1) {
			return signal.getWidth();
		}
		if(signal.getHeight()>1) {
			return signal.getHeight();
		}
		return 1;
	}

	public String getDataStructureInitCodeC() {

		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("blockId", block.getBlockId());
        context.put("blockName", block.getBlockName());
        context.put("blockPath", block.getBlockPath());

        context.put("number", getNumber());
        context.put("name", getName());
        context.put("signalName", signal.getName());
        context.put("signalWidth", signal.getWidth());
        context.put("signalHeight", signal.getHeight());
        context.put("signalDataType", signal.getDataType());

        return TemplateManager.renderTemplate("c/io/InputPort/init.vm", context);
	}

    // 为了解决方法调用链过长的问题，将getValue方法移到InputPort类中
    public Data getData() {
        return this.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getData();
    }

    /**
     * Generate C code for INPUT_PORT data structure definition
     * @param code The CodeStructC instance to add the generated code to
     */
    public void generateDataStructureCodeC(CodeStructC code) {
        context.put("blockId", block.getBlockId());
        context.put("portNumber", number);
        context.put("portName", name);
        context.put("portWidth", getWidth());
        
        String structCode = TemplateManager.renderTemplate("c/io/InputPort/dataStructure.vm", context);
        code.dataStructureCode += structCode + "\n";
    }

    /**
     * Generate C code for signal structure definition related to this input port
     * @param code The CodeStructC instance to add the generated code to
     */
    public void generateSignalStructureCodeC(CodeStructC code) {
        context.put("blockId", block.getBlockId());
        context.put("portNumber", number);
        context.put("portName", name);
        context.put("portWidth", getWidth());
        context.put("portHeight", getHeight());
        
        String signalCode = TemplateManager.renderTemplate("c/io/InputPort/signalStructure.vm", context);
        code.dataStructureCode += signalCode + "\n";
    }
}

package com.ncslab.block.io.terminal;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScopeStruct extends Terminal {
	@Setter
    private int maxDataLength=1000;
    @Getter
    private int width=1;
    @Getter
    private int height=1;

    @Getter
    private List<Double> timeList;
    @Getter
    private List<Double> dataList;
    @Getter
    private int chunkCount = 0;

	public ScopeStruct(Block block,int id,String localName){
		super(block, id, localName);
		this.name=sanitizeCIdentifier("Block" + block.getBlockId() + "_Scope_" + localName);
		this.localName=localName;
		this.timeList=new LinkedList<>();
		this.dataList=new LinkedList<>();
	}

	public String getDefineCodeC() {
		String code="";

		//code+="REAL "+this.name+"_Buffer["+this.maxDataLength*this.width*this.height+"];\n";
		//code+="REAL "+this.name+"_Time["+this.maxDataLength+"];\n";
		
		// Only declare the SCOPE variable, initialization happens in getInitCodeC()
		code+="SCOPE "+this.name+";\n";
		code+=this.getTerminalDefineCode("Scope");

		return code;
	}

	public String getInitCodeC() {
		String code="";
		
		// Check if this platform supports path and uuid fields
		String blockPath = this.block.getBlockPath();
		String blockUUID = this.block.getBlockUUID();
		
		// Generate SCOPE initialization based on backward platform capabilities
		if (blockPath == null) {
			blockPath = "";
		}
		if (blockUUID == null) {
			blockUUID = "";
		}

		// Windows/standard version with path and uuid
		code+=this.name+".name = (char *)\""+this.localName+"\";\n";
		code+=this.name+".path = (char *)\""+blockPath+"\";\n";
		code+=this.name+".uuid = (char *)\""+blockUUID+"\";\n";
		code+=this.name+".maxDataLength = "+maxDataLength+";\n";
		code+=this.name+".width = "+width+";\n";
		code+=this.name+".height = "+height+";\n";
		code+=this.name+".cursor = 0;\n";
		code+=this.name+".isFull = 0;\n";
		code+=this.name+".chunkCount = 0;\n";
		code+=this.name+".logEvery = 0;\n";
		code+=this.name+".logCounter = 0;\n";
		
		
		return code;
	}

	public void setDimension(int width,int height) {
		this.width=width;
		this.height=height;
	}

    public void addTimeSeries(double time,Data data) {
    	
    	com.ncslab.ncslablink.SimulationModel model=(com.ncslab.ncslablink.SimulationModel)this.block.getModel();
    	if(model.isMajorStep()==false) {
    		return;
    	}
    	
    	/*
    	while(timeList.size()>=maxDataLength) {
            this.timeList.remove(0);
            if(this.height==1 && this.width==1) {
            	this.dataList.remove(0);
            }else{
                for (int h=0;h<this.height;h++) {
                    for (int w=0;w<this.width;w++) {
                        this.dataList.remove(0);
                    }
                }
            }
        }*/

        
    	this.timeList.add(time);
        if(this.height==1 && this.width==1) {
        	this.dataList.add(data.getInitValue());
        }else{
            for (int h=0;h<this.height;h++) {
                for (int w=0;w<this.width;w++) {
                    this.dataList.add(data.getMatrix().get(h, w));
                }
            }
        }

    }

    public Block getBlock() {
    	return this.block;
    }

    /**
     * Reset chunk counter and clear all in-memory data.
     * Called before a new simulation run to discard previous state.
     */
    public void resetChunks() {
        this.chunkCount = 0;
        this.timeList.clear();
        this.dataList.clear();
    }

    /**
     * Flush in-memory time/data lists to a chunk JSON file.
     * Clears the lists and increments chunkCount.
     */
    public void flushChunk(String outputDir) throws IOException {
        if (timeList.isEmpty()) return;

        JSONObject scopeJson = new JSONObject();
        scopeJson.put("uuid", this.block.getBlockUUID());
        scopeJson.put("name", this.block.getBlockName());
        scopeJson.put("path", this.block.getBlockPath());
        scopeJson.put("width", this.width);
        scopeJson.put("height", this.height);
        scopeJson.put("chunkIndex", this.chunkCount);

        JSONArray timeArray = new JSONArray();
        JSONArray dataArray = new JSONArray();

        for (Double t : this.timeList) {
            timeArray.put(t);
        }
        for (Double d : this.dataList) {
            dataArray.put(d);
        }

        scopeJson.put("time", timeArray);
        scopeJson.put("data", dataArray);

        String filename = "scope_" + this.block.getBlockUUID() + "_chunk" + this.chunkCount + ".json";
        File outFile = new File(outputDir, filename);
        try (FileWriter writer = new FileWriter(outFile)) {
            writer.write(scopeJson.toString());
        }

        this.chunkCount++;
        this.timeList.clear();
        this.dataList.clear();
    }
}

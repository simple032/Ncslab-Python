package circuit;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Vector;

import ncslablink.NCSLabModel;
import circuit.block.CircuitBlock;
import circuit.block.CircuitBlockType;

import ncslablink.ModelException;

public class CircuitParser {
	
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	
	private static boolean isCircuitBlock(JSONObject blockJSON) {
		String srcBlock=blockJSON.getString("srcBlock");
		//System.out.println(srcBlock);
		if(srcBlock.startsWith("fl_lib")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public CircuitParser(NCSLabModel model) throws ModelException{
		this.model=model;
		parseCircuit();
	}
	
	public void parseCircuit() throws ModelException{
		int id=1;
		JSONArray blockJSONList=model.getBlocksJSON();
		int i=0;
		while(i<blockJSONList.length()) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);
			if(isCircuitBlock(blockJSON)) {
				//System.out.println(blockJSON);
				CircuitBlock block=CircuitBlockType.createBlock(id++, blockJSON, model);
				blockList.add(block);
				blockJSONList.remove(i);
			}
			else {
				i++;
			}
		}
		
		JSONArray lineJSONList=model.getLinesJSON();
		
		i=0;
		while(i<blockJSONList.length()) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			System.out.println(lineJSON);
			
			i++;
		}
	}
}

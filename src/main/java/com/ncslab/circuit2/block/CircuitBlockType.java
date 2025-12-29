package com.ncslab.circuit2.block;

import org.json.JSONObject;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.NCSLabModel;

public class CircuitBlockType {
	/*根据BlockType的类型，生成不同的Block */
	public static CircuitBlock createBlock(int id,JSONObject blockJSON,NCSLabModel model) throws ModelException {
		CircuitBlock block=null;
		String blockType=blockJSON.getString("blockType");
		//System.out.println(blockJSON);
		switch(blockType) {	
		//Single
			//Source
		case "DC Voltage Source":
			block=new circuit2.block.element.DCVoltageSource(id,blockJSON, model);
			break;
		case "DC Current Source":
			block=new circuit2.block.element.DCCurrentSource(id,blockJSON, model);
			break;
		case "AC Voltage Source":
			block=new circuit2.block.element.ACVoltageSource(id,blockJSON, model);
			break;
		case "AC Current Source":
			block=new circuit2.block.element.ACCurrentSource(id,blockJSON, model);
			break;
		case "Controlled Voltage Source":
			block=new circuit2.block.element.ControlledVoltageSource(id,blockJSON, model);
			break;
		case "Controlled Current Source":
			block=new circuit2.block.element.ControlledCurrentSource(id,blockJSON, model);
			break;
			//Sensor
		case "Voltage Sensor":
			block=new circuit2.block.element.VoltageSensor(id,blockJSON, model);
			break;
		case "Current Sensor":
			block=new circuit2.block.element.CurrentSensor(id,blockJSON, model);
			break;
		
		case "Resistor":
			block=new circuit2.block.element.Resistor(id,blockJSON, model);
			break;
		case "Inductor":
			block=new circuit2.block.element.Inductor(id,blockJSON, model);
			break;
		case "Capacitor":
			block=new circuit2.block.element.Capacitor(id,blockJSON, model);
			break;
		case "Variable Resistor":
			block=new circuit2.block.element.VariableResistor(id,blockJSON, model);
			break;
		case "Circuit\tSwitch":
			block=new circuit2.block.element.CircuitSwitch(id,blockJSON, model);
			break;
		case "SeriesRLCBranch":
			block=new circuit2.block.element.SeriesRLCBranch(id,blockJSON, model);
			break;
		case "Variable Inductor":
			block=new circuit2.block.element.VariableInductor(id,blockJSON, model);
			break;
		case "Variable Capacitor":
			block=new circuit2.block.element.VariableCapacitor(id,blockJSON, model);
			break;
		
		//Multi
		case "Diode":
			block=new circuit2.block.multielement.Diode(id,blockJSON, model);
			break;
		case "Op Amp":
			block=new circuit2.block.multielement.OpAmp(id,blockJSON, model);
			break;
		case "IGBT":
			block=new circuit2.block.multielement.IGBT(id,blockJSON, model);
			break;
		case "Mosfet":
			block=new circuit2.block.multielement.Mosfet(id,blockJSON, model);
			break;
			
		case "Ground":
			block=new circuit2.block.element.Ground(id,blockJSON, model);
			break;
		case "Electrical Reference":
			block=new circuit2.block.element.ElectricalReference(id,blockJSON, model);
			break;
		}
		
		if(block==null) {
			throw(new ModelException("Can not find blocktype \""+blockType+"\""));
		}
		
		//block.setBlockId(id);
		//block.updateBlock();
		
		return block;
	}
}

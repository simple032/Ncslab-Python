package com.ncslab.circuit2.block.multielement;

import org.json.JSONObject;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.io.CircuitPortType;
import com.ncslab.ncslablink.NCSLabModel;

public class OpAmp extends CircuitBlockSingle {

	//运放的增益
	private String amplitude = "1000";
	public OpAmp(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		this.blockModeType = BlockModeType.OpAmp;
		circuitPortList.get(1).setName("LConn2");
		circuitPortList.get(1).setCircuitPortType(CircuitPortType.Left);
		circuitPortList.add(new CircuitPort(this, "RConn1", CircuitPortType.Right, 3));
	}

//	public String getCurrentString() {
//		return "0";
//	}

	public String getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(String amplitude) {
		this.amplitude = amplitude;
	}
}

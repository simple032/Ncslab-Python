package com.ncslab.circuit2.block;

import org.json.JSONObject;

import com.ncslab.circuit2.block.element.ACCurrentSource;
import com.ncslab.circuit2.block.element.ACVoltageSource;
import com.ncslab.circuit2.block.element.Capacitor;
import com.ncslab.circuit2.block.element.CircuitSwitch;
import com.ncslab.circuit2.block.element.ControlledCurrentSource;
import com.ncslab.circuit2.block.element.ControlledVoltageSource;
import com.ncslab.circuit2.block.element.CurrentSensor;
import com.ncslab.circuit2.block.element.DCCurrentSource;
import com.ncslab.circuit2.block.element.DCVoltageSource;
import com.ncslab.circuit2.block.element.ElectricalReference;
import com.ncslab.circuit2.block.element.Ground;
import com.ncslab.circuit2.block.element.Inductor;
import com.ncslab.circuit2.block.element.Resistor;
import com.ncslab.circuit2.block.element.SeriesRLCBranch;
import com.ncslab.circuit2.block.element.VariableCapacitor;
import com.ncslab.circuit2.block.element.VariableInductor;
import com.ncslab.circuit2.block.element.VariableResistor;
import com.ncslab.circuit2.block.element.VoltageSensor;
import com.ncslab.circuit2.block.multielement.Diode;
import com.ncslab.circuit2.block.multielement.IGBT;
import com.ncslab.circuit2.block.multielement.Mosfet;
import com.ncslab.circuit2.block.multielement.OpAmp;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.NCSLabModel;

public class CircuitBlockType {

    private CircuitBlockType() {
    }

    public static CircuitBlock createBlock(int id, JSONObject blockJSON, NCSLabModel model) throws ModelException {
        String blockType = blockJSON.getString("blockType");
        switch (blockType) {
            case "AC Current Source":
                return new ACCurrentSource(id, blockJSON, model);
            case "AC Voltage Source":
                return new ACVoltageSource(id, blockJSON, model);
            case "Capacitor":
                return new Capacitor(id, blockJSON, model);
            case "Circuit Switch":
                return new CircuitSwitch(id, blockJSON, model);
            case "Controlled Current Source":
                return new ControlledCurrentSource(id, blockJSON, model);
            case "Controlled Voltage Source":
                return new ControlledVoltageSource(id, blockJSON, model);
            case "Current Sensor":
                return new CurrentSensor(id, blockJSON, model);
            case "DC Current Source":
                return new DCCurrentSource(id, blockJSON, model);
            case "DC Voltage Source":
                return new DCVoltageSource(id, blockJSON, model);
            case "Electrical Reference":
                return new ElectricalReference(id, blockJSON, model);
            case "Ground":
                return new Ground(id, blockJSON, model);
            case "Inductor":
                return new Inductor(id, blockJSON, model);
            case "Resistor":
                return new Resistor(id, blockJSON, model);
            case "SeriesRLCBranch":
            case "Series RLC Branch":
                return new SeriesRLCBranch(id, blockJSON, model);
            case "Variable Capacitor":
                return new VariableCapacitor(id, blockJSON, model);
            case "Variable Inductor":
                return new VariableInductor(id, blockJSON, model);
            case "Variable Resistor":
                return new VariableResistor(id, blockJSON, model);
            case "Voltage Sensor":
                return new VoltageSensor(id, blockJSON, model);
            case "Diode":
                return new Diode(id, blockJSON, model);
            case "IGBT":
                return new IGBT(id, blockJSON, model);
            case "Mosfet":
            case "MOSFET":
                return new Mosfet(id, blockJSON, model);
            case "OpAmp":
            case "Op Amp":
                return new OpAmp(id, blockJSON, model);
            default:
                throw new ModelException("Can not find circuit2 blocktype \"" + blockType + "\"");
        }
    }
}

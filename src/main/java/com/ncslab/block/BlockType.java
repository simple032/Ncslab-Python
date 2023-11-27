package com.ncslab.block;

import org.json.JSONObject;


import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.ncslablink.ModelException;

public class BlockType {
	/*根据BlockType的类型，生成不同的Block */
	public static Block createBlock(int id,JSONObject blockJSON,NCSLabModel model) throws ModelException {
		Block block=null;
		String blockType=blockJSON.getString("blockType");
		
		switch(blockType) {		
		//Sink
		case "Scope":
			block=new com.ncslab.block.sink.Scope(blockJSON,model);
			break;
		case "Terminator":
			block=new com.ncslab.block.sink.Terminator(blockJSON,model);
			break;
		//continuous
		case "PID Controller (s)":
			block=new com.ncslab.block.continuous.PIDController(blockJSON,model);
			break;
		case "PID Controller":
			block=new com.ncslab.block.continuous.OldPIDController(blockJSON,model);
			break;	
		case "Derivative":
			block=new com.ncslab.block.continuous.Derivative(blockJSON,model);
			break;
		case "WaterLevel":
			block=new com.ncslab.block.testrig.WaterLevel(blockJSON,model);
			break;
		//source
		case "Constant":
			block=new com.ncslab.block.source.Constant(blockJSON,model);
			break;
		case "Sum":
			block=new com.ncslab.block.math.Sum(blockJSON,model);
			break;
		case "Gain":
			block=new com.ncslab.block.math.Gain(blockJSON,model);
			break;
		case "Integrator":
			block=new com.ncslab.block.continuous.Integrator(blockJSON,model);
			break;
		case "Transport Delay":
			block=new com.ncslab.block.continuous.TransportDelay(blockJSON, model);
			break;
		case "Transfer Fcn":
			block=new com.ncslab.block.continuous.TransferFcn(blockJSON, model);
			break;
		case "newMotor":
			block=new com.ncslab.block.testrig.NewMotor(blockJSON, model);
			break;
		case "DCMotorAngle":
			block=new com.ncslab.block.testrig.DCMotorAngle(blockJSON, model);
			break;
		case "ServoMotorSlider":
			block=new com.ncslab.block.testrig.ServoMotorSlider(blockJSON, model);
			break;
		case "ALP":
			block=new com.ncslab.block.testrig.Alp(blockJSON, model);
			break;	
		case "Fans":
			block=new com.ncslab.block.testrig.RaspFan(blockJSON, model);
			break;	
		case "S-Function":
			block=new com.ncslab.block.function.SFunction(blockJSON, model);
			break;
		case "S-FunctionBuilder":
			block=new com.ncslab.block.function.SFunctionBuilder(blockJSON, model);
			break;
		case "UDPSender":
			block=new com.ncslab.block.comm.UDPSender(blockJSON, model);
			break;
		case "UDPReceiver":
			block=new com.ncslab.block.comm.UDPReceiver(blockJSON, model);
			break;
		//discrete
		case "DiscreteStateSpace":
			block=new com.ncslab.block.discrete.DiscreteStateSpace(blockJSON, model);
			break;	
		case "Zero-Order Hold":
			block=new com.ncslab.block.discrete.Zero_Order_Hold(blockJSON,model);
			break;
		case "Delay":
			block=new com.ncslab.block.discrete.Delay(blockJSON,model);
			break;
		case "Unit Delay":
			block=new com.ncslab.block.discrete.UnitDelay(blockJSON,model);
			break;
		case "Discrete-Time Integrator":
			block=new com.ncslab.block.discrete.Discrete_Time_Integrator(blockJSON,model);
			break;
		case "Discrete Transfer Fcn":
			block=new com.ncslab.block.discrete.Discrete_Transfer_Fcn(blockJSON,model);
			break;
		case "Discrete Transfer Fcn (z)":
			block=new com.ncslab.block.discrete.Discrete_Transfer_Fcnz(blockJSON,model);
			break;
		//routing
		case "Mux":
			block=new com.ncslab.block.route.Mux(blockJSON, model);
			break;	
		case "Demux":
			block=new com.ncslab.block.route.Demux(blockJSON, model);
			break;	
		case "State-Space":
			block=new com.ncslab.block.continuous.StateSpace(blockJSON,model);
			break;
		case "Saturation":
			block=new com.ncslab.block.discontinuous.Saturation(blockJSON,model);
			break;
		case "Relay":
			block=new com.ncslab.block.discontinuous.Relay(blockJSON,model);
			break;
		case "Dead Zone":
			block=new com.ncslab.block.discontinuous.DeadZone(blockJSON,model);
			break;
		case "Coulomb Viscous Friction":
			block=new com.ncslab.block.discontinuous.Coulomb(blockJSON,model);
			break;
		case "Backlash":
			block=new com.ncslab.block.discontinuous.Backlash(blockJSON,model);
			break;
		case "Trigonometric Function":
			block=new com.ncslab.block.math.TrigFunction(blockJSON,model);
			break;
		case "Add":
			block=new com.ncslab.block.math.Add(blockJSON,model);
			break;
		case "Sign":
			block=new com.ncslab.block.math.Sign(blockJSON,model);
			break;
		case "Product":
			block=new com.ncslab.block.math.Product(blockJSON,model);
			break;
		case "Math Function":
			block=new com.ncslab.block.math.MathFunction(blockJSON,model);
			break;
		case "Step":
			block=new com.ncslab.block.source.Step(blockJSON,model);
			break;
		case "Pulse Generator":
			block=new com.ncslab.block.source.Pulse(blockJSON,model);
			break;
		case "Repeating Sequence":
			block=new com.ncslab.block.source.RepeatingSequence(blockJSON,model);
			break;
		case "Ramp":
			block=new com.ncslab.block.source.Ramp(blockJSON,model);
			break;
		case "Sine Wave":
			block=new com.ncslab.block.source.SineWave(blockJSON,model);
			break;
		case "In":
			block=new com.ncslab.block.subsystem.In(blockJSON,model);
			break;
		case "Out":
			block=new com.ncslab.block.subsystem.Out(blockJSON,model);
			break;	
		case "Subsystem":
			block=new com.ncslab.block.subsystem.Subsystem(blockJSON,model);
			break;
		case "Switch":
			block=new com.ncslab.block.route.Switch(blockJSON,model);
			break;
		case "Clock":
			block=new com.ncslab.block.source.Clock(blockJSON,model);
			break;
		case "TestPoint":
			block=new com.ncslab.block.math.TestPoint(blockJSON, model);
			break;
		case "PS-Simulink Converter":
			blockJSON.put("blockType", "TestPoint");
			block=new com.ncslab.block.math.TestPoint(blockJSON, model);
			break;
		case "Simulink-PS Converter":
			blockJSON.put("blockType", "TestPoint");
			block=new com.ncslab.block.math.TestPoint(blockJSON, model);
			break;
		case "Substitution":
			block=new com.ncslab.block.testrig.Substitution(blockJSON,model);
			break;
		case "Superposition":
			block=new com.ncslab.block.testrig.Superposition(blockJSON,model);
			break;
		case "Telegenic":
			block=new com.ncslab.block.testrig.Telegenic(blockJSON,model);
			break;
		case "Kirchhoff":
			block=new com.ncslab.block.testrig.Kirchhoff(blockJSON,model);
			break;
		case "AD":
			block=new com.ncslab.block.route.AD(blockJSON,model);
			break;
		case "DA":
			block=new com.ncslab.block.route.DA(blockJSON,model);
			break;
		case "GPIO":
			block=new com.ncslab.block.route.GPIO(blockJSON,model);
			break;
		case "Compare To Constant":
			block=new com.ncslab.block.logicAndBit.CompareToConstant(blockJSON,model);
			break;
		case "Shift Arithmetic":
			block=new com.ncslab.block.logicAndBit.ShiftArithmetic(blockJSON,model);
			break;
		case "Logical Operator":
			block=new com.ncslab.block.logicAndBit.LogicOperator(blockJSON,model);
			break;
		case "Relational Operator":
			block=new com.ncslab.block.logicAndBit.RelationalOperator(blockJSON,model);
			break;
		case "Compare To Zero":
			block=new com.ncslab.block.logicAndBit.CompareToZero(blockJSON,model);
			break;
		}
		
		if(block==null) {
			throw(new ModelException("Can not find blocktype \""+blockType+"\""));
		}
		
		block.setBlockId(id);
		block.updateBlock();
		
		return block;
	}
}

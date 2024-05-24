package com.ncslab.block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.json.JSONObject;


import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.ModelException;


/**
 * Generate corresponding <code>Block</code> according to <code>BlockType</code>.
 * If your block is an instance of <code>Block</code> but not an instance of <code>SoughtedBlock</code>, 
 * ples put the operation manually in the hashmap <code>blockFactory</code>; 
 * If your block is an instance of <code>SoughtedBlock</code>, you do not need to do any operation here, 
 * but follow the javadoc mentioned in <code>SearchableBlock</code>
 * @see SearchableBlock
 */
public class BlockType{
	private static final Map<String, BiFunction<JSONObject, NCSLabModel, Block>> blockFactory = new HashMap<>();

	/**
	 * put key-value set into the map.
	 * @param key instance of String, corresponding to that in the frontend.
	 * @param value instance of a <code>BiFunction<JSONObject, NCSLabModel, Block></code>.
	 */
	public static void put(String key, BiFunction<JSONObject, NCSLabModel, Block> value){
		blockFactory.put(key, value);
	}

	/**
	 * Check whether the key you want to use has been used in the map.
	 * @param key the key you want to use
	 * @return whether the key has already exist in the map. Exists, return true; else return false.
	 */
	public static boolean isKeyExist(String key){
		return blockFactory.containsKey(key);
	}

	/**
	 * Existing projects
	 */
	static {
		// Sink
		blockFactory.put("Scope", com.ncslab.block.sink.Scope::new);
		blockFactory.put("Terminator", com.ncslab.block.sink.Terminator::new);

		// Continuous
		blockFactory.put("PID Controller (s)", com.ncslab.block.continuous.PIDController::new);
		blockFactory.put("PID Controller", com.ncslab.block.continuous.OldPIDController::new);
		blockFactory.put("Derivative", com.ncslab.block.continuous.Derivative::new);
		blockFactory.put("WaterLevel", com.ncslab.block.testrig.WaterLevel::new);


		//source
		blockFactory.put("Constant", com.ncslab.block.source.Constant::new);
		blockFactory.put("Sum", com.ncslab.block.math.Sum::new);
		blockFactory.put("Gain", com.ncslab.block.math.Gain::new);
		blockFactory.put("Integrator", com.ncslab.block.continuous.Integrator::new);
		blockFactory.put("Transport Delay", com.ncslab.block.continuous.TransportDelay::new);
		blockFactory.put("Transfer Fcn", com.ncslab.block.continuous.TransferFcn::new);
		blockFactory.put("newMotor", com.ncslab.block.testrig.NewMotor::new);
		blockFactory.put("DCMotorAngle", com.ncslab.block.testrig.DCMotorAngle::new);
		blockFactory.put("ServoMotorSlider", com.ncslab.block.testrig.ServoMotorSlider::new);
		blockFactory.put("ALP", com.ncslab.block.testrig.Alp::new);
		blockFactory.put("Fans", com.ncslab.block.testrig.RaspFan::new);
		blockFactory.put("S-Function", com.ncslab.block.function.SFunction::new);
		blockFactory.put("S-FunctionBuilder", com.ncslab.block.function.SFunctionBuilder::new);
		blockFactory.put("UDPSender", com.ncslab.block.comm.UDPSender::new);
		blockFactory.put("UDPReceiver", com.ncslab.block.comm.UDPReceiver::new);

		//discrete
		blockFactory.put("DiscreteStateSpace", com.ncslab.block.discrete.DiscreteStateSpace::new);
		blockFactory.put("Zero-Order Hold", com.ncslab.block.discrete.Zero_Order_Hold::new);
		blockFactory.put("Delay", com.ncslab.block.discrete.Delay::new);
		blockFactory.put("Unit Delay", com.ncslab.block.discrete.UnitDelay::new);
		blockFactory.put("Discrete-Time Integrator", com.ncslab.block.discrete.Discrete_Time_Integrator::new);
		blockFactory.put("Discrete Transfer Fcn", com.ncslab.block.discrete.Discrete_Transfer_Fcn::new);
		blockFactory.put("Discrete Transfer Fcn (z)", com.ncslab.block.discrete.Discrete_Transfer_Fcnz::new);

		//routing
		blockFactory.put("Mux", com.ncslab.block.route.Mux::new);
		blockFactory.put("Demux", com.ncslab.block.route.Demux::new);
		blockFactory.put("State-Space", com.ncslab.block.continuous.StateSpace::new);
		blockFactory.put("Saturation", com.ncslab.block.discontinuous.Saturation::new);
		blockFactory.put("Relay", com.ncslab.block.discontinuous.Relay::new);
		blockFactory.put("Dead Zone", com.ncslab.block.discontinuous.DeadZone::new);
		blockFactory.put("Coulomb Viscous Friction", com.ncslab.block.discontinuous.Coulomb::new);
		blockFactory.put("Backlash", com.ncslab.block.discontinuous.Backlash::new);
		blockFactory.put("Trigonometric Function", com.ncslab.block.math.TrigFunction::new);
		blockFactory.put("Add", com.ncslab.block.math.Add::new);	
		blockFactory.put("Sign", com.ncslab.block.math.Sign::new);
		blockFactory.put("Product", com.ncslab.block.math.Product::new);
		blockFactory.put("Math Function", com.ncslab.block.math.MathFunction::new);
		blockFactory.put("Step", com.ncslab.block.source.Step::new);
		blockFactory.put("Pulse Generator", com.ncslab.block.source.Pulse::new);
		blockFactory.put("Repeating Sequence", com.ncslab.block.source.RepeatingSequence::new);
		blockFactory.put("Ramp", com.ncslab.block.source.Ramp::new);
		blockFactory.put("Sine Wave", com.ncslab.block.source.SineWave::new);
		blockFactory.put("In", com.ncslab.block.subsystem.In::new);
		blockFactory.put("Out", com.ncslab.block.subsystem.Out::new);
		blockFactory.put("Subsystem", com.ncslab.block.subsystem.Subsystem::new);
		blockFactory.put("Switch", com.ncslab.block.route.Switch::new);
		blockFactory.put("Clock", com.ncslab.block.source.Clock::new);
		blockFactory.put("TestPoint", com.ncslab.block.math.TestPoint::new);
		// blockFactory.put("PS-Simulink Converter", com.ncslab.block.math.TestPoint::new);//for test
		// blockFactory.put("Simulink-PS Converter", com.ncslab.block.math.TestPoint::new);
		blockFactory.put("Substitution", com.ncslab.block.testrig.Substitution::new);
		blockFactory.put("Superposition", com.ncslab.block.testrig.Superposition::new);
		blockFactory.put("Telegenic", com.ncslab.block.testrig.Telegenic::new);
		blockFactory.put("Kirchhoff", com.ncslab.block.testrig.Kirchhoff::new);
		blockFactory.put("AD", com.ncslab.block.route.AD::new);
		blockFactory.put("DA", com.ncslab.block.route.DA::new);
		blockFactory.put("GPIO", com.ncslab.block.route.GPIO::new);
		blockFactory.put("Compare To Constant", com.ncslab.block.logicAndBit.CompareToConstant::new);
		blockFactory.put("Shift Arithmetic", com.ncslab.block.logicAndBit.ShiftArithmetic::new);
		blockFactory.put("Logical Operator", com.ncslab.block.logicAndBit.LogicOperator::new);
		blockFactory.put("Relational Operator", com.ncslab.block.logicAndBit.RelationalOperator::new);
		blockFactory.put("Compare To Zero", com.ncslab.block.logicAndBit.CompareToZero::new);
		blockFactory.put("Transpose",com.ncslab.block.matrix.Transpose::new);
		// case "Transpose":
		// 	block = new com.ncslab.block.matrix.Transpose(blockJSON,model);
		// 	break;
		//matrix
		//todo: the part of matrix calculation has not included.
		blockFactory.put("Create Diagonal Matrix", com.ncslab.block.matrix.CreateDiagonalMatrix::new);
		blockFactory.put("Cross Product", com.ncslab.block.matrix.CrossProduct::new);
		blockFactory.put("Extract Diagonal", com.ncslab.block.matrix.ExtractDiagonal::new);
		blockFactory.put("Identity Matrix", com.ncslab.block.matrix.IdentityMatrix::new);
		blockFactory.put("IsHermitian", com.ncslab.block.matrix.IsHermitian::new);
		blockFactory.put("IsSymmetric", com.ncslab.block.matrix.IsSymmetric::new);
		blockFactory.put("IsTriangular", com.ncslab.block.matrix.IsTriangular::new);
		blockFactory.put("Matrix Multiply", com.ncslab.block.matrix.MatrixMultiply::new);
		blockFactory.put("Matrix Concatenate", com.ncslab.block.matrix.MatrixConcatenate::new);
		blockFactory.put("Matrix Square", com.ncslab.block.matrix.MatrixSquare::new);
		blockFactory.put("Permute Matrix", com.ncslab.block.matrix.PermuteMatrix::new);
		blockFactory.put("Submatrix", com.ncslab.block.matrix.Submatrix::new);
		blockFactory.put("Transpose", com.ncslab.block.matrix.Transpose::new);
		//advanced control
		blockFactory.put("LQR Controller", com.ncslab.block.advancedControl.LQRController::new);

		//machine learning
		blockFactory.put("LinearRegression", com.ncslab.block.machineLearning.LinearRegression::new);
		blockFactory.put("LogisticRegression", com.ncslab.block.machineLearning.LogisticRegression::new);

		//New models that extends SoughtedBlock do not need to do any operation here!
	}
	

	public static Block createBlock(int id, JSONObject blockJSON, NCSLabModel model) throws ModelException {
		String blockType = blockJSON.getString("blockType");
		BiFunction<JSONObject, NCSLabModel, Block> constructor = blockFactory.get(blockType);
		//Operation to suit previous version.
		//Todo: preprocess this in the original class corresponding to "PS-Simulink Converter" and "Simulink-PS Converter".
		// switch (blockType) {
		// 	case "PS-Simulink Converter":
		// 		blockJSON.put("blockType", "TestPoint");
		// 		break;

		// 	case "Simulink-PS Converter":
		// 		blockJSON.put("blockType", "TestPoint");
		// 		break;
		// }

		if (constructor == null) {
			throw(new ModelException("Can not find blocktype in mapped function \""+blockType+"\""));
		}
		Block block = constructor.apply(blockJSON, model);
		if(block==null) {
			throw(new ModelException("Can not find blocktype in block\""+blockType+"\""));
		}
		block.setBlockId(id);
		block.updateBlock();
		return block;
		// return null;
	}
}

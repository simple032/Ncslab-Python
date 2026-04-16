package com.ncslab.code;


import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import com.ncslab.dto.core.ModelDto;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.line.Line;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.circuit.block.electblock.ElectBlock;

import com.ncslab.circuit2.CircuitModel2;

abstract public class CodeModel extends NCSLabModel {
	
	abstract protected void generatorCircuitGloablCode(CodeGenerationOption option);
	abstract protected void generatorCircuitOutputCode(CodeGenerationOption option);
	abstract protected void generatorCircuitUpdateCode(CodeGenerationOption option);


    @Getter
    @Setter
    protected Solver solver=Solver.ode4;

	// 原有JSONObject构造函数
	protected CodeModel(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
		setupSolver();
		System.out.println(this.solver);
	}
	
	// 原有JSONObject构造函数带solver参数
	protected CodeModel(JSONObject jsonIn,ModelMode mode,Solver solver) throws ModelException{
		super(jsonIn,mode);
		this.solver=solver;
	}
	
	// 新增ModelDto DTO构造函数
	protected CodeModel(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
		setupSolver();
		System.out.println(this.solver);
	}
	
	// 新增ModelDto DTO构造函数带solver参数
	protected CodeModel(ModelDto modelDto, ModelMode mode, Solver solver) throws ModelException{
		super(modelDto, mode);
		this.solver = solver;
	}

	private void setupSolver() {
		String solverString=this.getConfig().getSolver();
		switch(solverString) {
		case "VariableStepAuto":
			solver=autoSelectSolver();
			break;
		case "ode45":
			solver=Solver.ode45;
			break;
		case "ode23":
			solver=Solver.ode23;
			break;
		case "ode15s":
			solver=Solver.ode15s;
			break;
		case "ode113":
			solver=Solver.ode113;
			break;
		case "ode23s":
			solver=Solver.ode15s;
			break;
		case "ode23t":
			solver=Solver.ode23;
			break;
		case "ode23tb":
			solver=Solver.ode15s;
			break;
		case "ode5":
			solver=Solver.ode5;
			break;
		case "ode8":
			solver=Solver.ode6;
			break;
		case "ode4":
			solver=Solver.ode4;
			break;
		case "ode3":
			solver=Solver.ode3;
			break;
		case "ode2":
			solver=Solver.ode2;
			break;
		case "ode1":
			solver=Solver.ode1;
			break;
		}
		/*if(solverString.equals("VariableStepAuto")||solverString.equals("ode45")) {
			solver=Solver.ode45;
		}
		if(solverString.equals("ode5")) {
			solver=Solver.ode5;
		}
		if(solverString.equals("ode8")) {
			solver=Solver.ode6;
		}
		if(solverString.equals("ode23")) {

		if(solverString.equals("VariableStepAuto")||solverString.equals("ode23")) {
			solver=Solver.ode23;
		}*/
	}

    private Solver autoSelectSolver() {
        // TODO: Auto select solver based on the model stiffness
        return Solver.ode45;
    }

	private void generateOutputCodeFromChain(CodeGenerationOption option) {
		for(Block block:getOutputChain()) {
			//根据输出链，建立Ouput的代码
			if(block instanceof com.ncslab.block.sink.SinkBlock) {
				generateBlockSinkOutputCode(block,option);
			}
			else {
				generateBlockOutputCode(block,option);
			}

		}
	}




    /**
	 * general paradigm of code generation
	 * 1. generate init code
	 * 2. generate output code
	 * 3. generate derivative code
	 * 4. generate update code
	 * 5. generate statement code
	 * 6. generate terminate code
	 *
	 * @throws MatDimException
	 */
	public void generate() {

		CodeGenerationOption option = new CodeGenerationOption();
		
		System.out.println("Generating codes......");
		
		// Check for algebraic loops before generating code
		if (this.isAlgebraicLoop()) {
			String errorMsg = "Algebraic loop detected in the model. Please check your feedback connections.";
			System.err.println(errorMsg);
			errorList.add(new ErrorMessage(ErrorMessage.AlgebraicLoop, errorMsg));
			return;
		}
		
		if(this.getCircuitModel()!=null) {
			CircuitModel2 circuitModel=this.getCircuitModel();
			circuitModel.generate();
		}

		//首先生成初始化代码
		generateInitCode(option);
		//根据输出链，建立Ouput的代码
		generateOutputCodeFromChain(option);
		//计算为分量的代码
		generateDerivativeCode(option);

		//author:xiazhiqiang
		generateArraysCode(option);
		//end

		//根据微分量，建立Update的代码
		try {
			generateUpdateCode(option);
			generateDiscreteUpdateCode(option);
		} catch (MatDimException e) {
			// TODO Auto-generated catch block
			errorList.add(new ErrorMessage(100, e.getMessage()));
		}

		generateStatementCode(option);

		generateTerminateCode(option);
		
		if(this.getCircuitModel()!=null) {
			generatorCircuitGloablCode(option);
			generatorCircuitOutputCode(option);
			generatorCircuitUpdateCode(option);
		}
	}



	//初始化的代码，继承的类可以重载
	abstract protected void generateInitCode(CodeGenerationOption option);
	//Update的代码，继承的类可以重载
	abstract protected void generateUpdateCode(CodeGenerationOption option) throws MatDimException;
	//某个模块输出的代码，继承的类可以重载
	abstract protected void generateBlockOutputCode(Block block,CodeGenerationOption option);

	abstract protected void generateBlockSinkOutputCode(Block block,CodeGenerationOption option);

	//author:xiazhiqiang
	abstract protected void generateArraysCode(CodeGenerationOption option);
	//end

	abstract protected void generateDerivativeCode(CodeGenerationOption option);

	abstract protected void generateStatementCode(CodeGenerationOption option);

	abstract protected void generateTerminateCode(CodeGenerationOption option);
	abstract protected void generateDiscreteUpdateCode(CodeGenerationOption option) throws MatDimException;


}

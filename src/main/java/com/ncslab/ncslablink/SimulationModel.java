package com.ncslab.ncslablink;

import Jama.Matrix;
import com.google.common.io.LittleEndianDataInputStream;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.block.io.terminal.ScopeStruct;
import com.ncslab.block.io.terminal.Terminal;
import com.ncslab.block.sink.Scope;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.utils.Property;
import lombok.Getter;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.*;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.ibatis.jdbc.Null;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.websocket.Session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;



public class SimulationModel extends NCSLabModel{

    // 系统状态和参数
    private double[] states;
    private double[] parameters;
    private double[] inputs;
    private double[] outputs;
    private JSONObject result = new JSONObject();
	SimulationModel(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn,mode);

        // 绑定模型的输出端口
        int statesSize = 0;
        for(Block block: blockList) {
            for (State state : block.getStateList()) {
                if (state.getDataType() == DataType.REAL) {
                    statesSize++;
                } else {
                    Matrix matrix = state.getData().getMatrix();
                    statesSize += matrix.getRowDimension() * matrix.getColumnDimension();
                }
            }
        }
        states = new double[statesSize];
	}

	public static SimulationModel createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
        return new SimulationModel(jsonIn,mode);
	}

	private void sendSimulatingMessage(Session session, double time) throws IOException{
		JSONObject jb = new JSONObject();
		jb.put("msg", "simulating");
		jb.put("time", time);
		jb.put("timeLength", this.getConfig().getStopTime());
        if(session != null)
		    session.getBasicRemote().sendText(jb.toString());
	}

    private void sendTimeSeriesMessage(Session session, JSONObject timeseries) throws IOException{
		JSONObject jb=new JSONObject();
		jb.put("msg", "timeseries");
        jb.put("version", 1.0);
        jb.put("timeseries", timeseries);
        if(session!=null)
            session.getBasicRemote().sendText(jb.toString());
        else
            System.out.println(jb);
	}

	public void simulate(Session session) throws ModelException {
		System.out.println("Executing simulation codes...");
        // 初始化解算器
        double absTol = getConfig().getAbsTol();
        double relTol = getConfig().getRelTol();
        double minStep = getConfig().getMinStep();
        double maxStep = getConfig().getFixedStep();
        double step = getConfig().getFixedStep();
        try {

            // 定义控制系统的微分方程
            FirstOrderDifferentialEquations systemODE = new FirstOrderDifferentialEquations() {
                @Override
                public int getDimension() {
                    return states.length; // 状态变量数量
                }

                @Override
                public void computeDerivatives(double t, double[] x, double[] xDot) {

                    // 1. 计算误差（设定值与实际值的差）

                    // 2. 计算积分项导数（对应mdlDerivatives）
                    calculateDerivatives(t,x,xDot);
                    // 检查是否到达采样时间点
//                    if (Math.abs(t % dt) < 1e-10 || t == 0) {
//                        updateDiscreteStates(t, y);
//                    }

                    // 打印当前状态（实际应用中可通过WebSocket发送）
//                     System.out.printf("t=%.3f, y=%.6f, error=%.6f, output=%.6f%n",
//                                     t, y[0], error, output);

                }

            };

            FirstOrderIntegrator integrator = null;
            switch (getConfig().getSolver()) {
            	case "ode45":
                    integrator = new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); break;
                case "ode1":
                    // 对应MATLAB中的ode1，是欧拉方法
                    integrator = new EulerIntegrator(step); break;
                case "ode2":
                    // 对应MATLAB中的ode2，是改进的欧拉方法（Heun方法）
                    integrator = new MidpointIntegrator(step); break;
                case "ode3":
                    // 对应MATLAB中的ode3，是三阶Runge-Kutta方法
                    integrator = new ThreeEighthesIntegrator(step); break;
                case "ode4":
                    // 对应MATLAB中的ode4，是经典四阶Runge-Kutta方法
                    integrator = new ClassicalRungeKuttaIntegrator(step); break;
                case "ode5":
                    // 对应MATLAB中的ode5，是Dormand-Prince方法，不过这里使用定步长
                    integrator = new DormandPrince54Integrator(minStep, maxStep, absTol, relTol); break;
                default:
                    throw new IllegalArgumentException("不支持的积分器名称: " + getConfig().getSolver());
            }

            // 添加固定步长处理器
            FixedStepHandler fixedStepHandler = new FixedStepHandler() {
                @Override
                public void init(double t0, double[] y0, double t) {
//                    calculateInits(t0, states);
                }

                @Override
                public void handleStep(double t, double[] y, double[] yDot, boolean isLast) {
                    // 复制当前状态
                    double[] state = Arrays.copyOf(y, y.length);
                    // 计算代数输出（可以直接调用系统ODE的计算部分）
//                    systemODE.computeDerivatives(t, y, yDot);
                    // 3. 处理离散状态更新
                    calculateDiscreteUpdates(t);

                    double iteration = Math.floor(t/step)/1000;
                    // 4. 发送时间序列消息（无论是否有状态）
                    if (iteration - Math.floor((iteration))  < minStep) {
                        try {
                            sendSimulatingMessage(session, t);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(isLast){
                        calculateTerminates(t);
                    }
                }
            };
//

            StepHandler stepHandler = new StepHandler() {
                @Override
                public void init(double t0, double[] y0, double t) {
//                    calculateInits(t0, states);
                }

                @Override
                public void handleStep(StepInterpolator interpolator, boolean isLast) {
                    double currentTime = interpolator.getCurrentTime();
                    double[] state = interpolator.getInterpolatedState();

                    // 处理离散状态更新
                    calculateDiscreteUpdates(currentTime);

                    // 处理主步长的输出

                    // 发送时间序列消息
                    if(currentTime - Math.floor(currentTime) < minStep) {
                        try {
                            sendSimulatingMessage(session, currentTime);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(isLast){
                        calculateTerminates(currentTime);
                    }
                }
            };


            // send the simulation data to the client
            double tStart = getConfig().getStartTime();
            double tEnd = getConfig().getStopTime();
            calculateInits(tStart, states);
            boolean hasState = systemODE.getDimension() > 0;
            if(hasState) {
                if(getConfig().getSolver().length()>4) {
                    // Continuous
                    integrator.addStepHandler(stepHandler);
                    double tEndActual = integrator.integrate(systemODE, tStart, states, tEnd, states);
                    System.out.printf("Simulation over in time=%f(%f)%n", tEndActual, tEnd);
                }
                // Discrete
                else {
                    performFixedStepIntegration(session, integrator, systemODE, tStart, tEnd, step, minStep);
                }
            }else {
                double t = tStart;

                while(t <= tEnd){
                    calculateOutputs(t);
                    calculateDiscreteUpdates(t);
                    // 发送时间序列消息
                    if(t - Math.floor(t) < minStep) {
                        try {
                            sendSimulatingMessage(session, t);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    t+=step;
                    if(t > tEnd){
                        t = tEnd;
                    }
                }

            }
            calculateTerminates(tEnd);
            writeResultFiles();
		}
		catch(Exception e) {
            e.printStackTrace();
			throw new ModelException("Can not execute the exe file!");
		}

		System.out.println("Simulation executed successfully!");

	}

    /**
     * Performs fixed-step integration with precise output control
     */
    private void performFixedStepIntegration(Session session, FirstOrderIntegrator integrator,
                                             FirstOrderDifferentialEquations systemODE,
                                             double tStart, double tEnd, double step, double minStep)
        throws IOException {

        double currentTime = tStart;
        double[] currentStates = Arrays.copyOf(states, states.length);

        // Calculate the exact number of steps needed
        int totalSteps = (int) Math.round((tEnd - tStart) / step);
        double actualStep = (tEnd - tStart) / totalSteps;

        System.out.printf("Fixed-step integration: start=%.6f, end=%.6f, steps=%d, actualStep=%.6f%n",
            tStart, tEnd, totalSteps, actualStep);

        // Initialize and send initial condition
        calculateOutputs(currentTime);
        calculateDiscreteUpdates(currentTime);
        sendSimulatingMessage(session, currentTime);
//        System.out.printf("Step 0: t=%.6f (initial)%n", currentTime);

        // Perform step-by-step integration
        for (int stepIndex = 1; stepIndex <= totalSteps; stepIndex++) {
            double targetTime = tStart + stepIndex * actualStep;

            // Clamp to exact end time to avoid floating-point overshoot
            if (stepIndex == totalSteps) {
                targetTime = tEnd;
            }

            try {
                // Integrate from current time to target time
                double actualEndTime = integrator.integrate(systemODE, currentTime, currentStates, targetTime, currentStates);

                // Update current time
                currentTime = actualEndTime;

                // Calculate outputs and discrete updates at this precise time point
                calculateOutputs(currentTime);
                calculateDiscreteUpdates(currentTime);

                // Send simulation message
                if(stepIndex % 1000 == 0) {
                    sendSimulatingMessage(session, currentTime);
                }
//                System.out.printf("Step %d: t=%.6f (target=%.6f)%n", stepIndex, currentTime, targetTime);

                // Verify we're making progress and haven't stalled
                if (Math.abs(currentTime - targetTime) > minStep) {
                    System.out.printf("Warning: Integration stopped at t=%.6f instead of target t=%.6f%n",
                        currentTime, targetTime);
                }

            } catch (Exception e) {
                System.err.printf("Integration failed at step %d, time=%.6f, target=%.6f%n",
                    stepIndex, currentTime, targetTime);
                throw new RuntimeException("Integration step failed", e);
            }
        }

        // Final verification and cleanup
        if (Math.abs(currentTime - tEnd) > minStep) {
            System.out.printf("Warning: Final time %.6f differs from target %.6f%n", currentTime, tEnd);

            // Force final step if needed
            try {
                currentTime = integrator.integrate(systemODE, currentTime, currentStates, tEnd, currentStates);
                calculateOutputs(currentTime);
                calculateDiscreteUpdates(currentTime);
                sendSimulatingMessage(session, currentTime);
                System.out.printf("Final correction: t=%.6f%n", currentTime);
            } catch (Exception e) {
                System.err.printf("Final correction step failed at t=%.6f%n", currentTime);
            }
        }

        System.out.printf("Fixed-step integration completed at t=%.6f%n", currentTime);
    }

    private void calculateOutputs(double t) {
        // 计算各个模块的输出
        // 类似Simulink的mdlOutputs
        for(Block block: outputChain) {
            block.calculateOutput(t);
        }

        JSONArray series = new JSONArray();
        for (Block block : blockList) {
            JSONObject blockData = new JSONObject();
            JSONArray outputDataArray = new JSONArray();
            JSONArray inputDataArray = new JSONArray();
            blockData.put("blockUUID", block.getBlockUUID());
            try {
                for (OutputPort outputPort : block.getOutputPortList()) {
                    JSONObject outputData = new JSONObject();
                    outputData.put("name", outputPort.getOutputSignalC().getName());
                    outputData.put("type", outputPort.getOutputSignalC().getDataType());
                    outputData.put("real", outputPort.getOutputSignalC().getData().getInitValue());
                    outputData.put("matrix", outputPort.getOutputSignalC().getData().getMatrix());
                    outputDataArray.put(outputData);
                }
                for (InputPort inputPort : block.getInputPortList()) {
                    JSONObject inputData = new JSONObject();
                    OutputPort outputPort = inputPort.getLinkedLine().getLinkedOutputPort();
                    inputData.put("name", outputPort.getOutputSignalC().getName());
                    inputData.put("type", outputPort.getOutputSignalC().getDataType());
                    inputData.put("real", outputPort.getOutputSignalC().getData().getInitValue());
                    inputData.put("matrix", outputPort.getOutputSignalC().getData().getMatrix());
                    inputDataArray.put(inputData);
                }
            }
            catch (JSONException e) {
                System.err.println("JSON encode error " + block.getBlockType() + " at time " + t);
                throw e;
            }
            catch (NullPointerException e) {
                System.err.println("No output or input data for block " + block.getBlockType() + " at time " + t);
                throw e;
            }
            blockData.put("outputs", outputDataArray);
            blockData.put("inputs", inputDataArray);
            series.put(blockData);
        }

        result.put("time", t);
        result.put("series", series);
    }

    private void calculateDerivatives(double t, double[] x, double[] xDot) {
        // 计算连续状态的导数
        // 类似Simulink的mdlDerivatives

        int index = 0;
        for(Block block: blockList) {
            for (State state : block.getStateList()) {
                if (state.getDataType() == DataType.REAL) {
                    state.getData().setInitValue(x[index++]);
                } else {
                    Matrix matrix = state.getData().getMatrix();
                    for (int i = 0; i < matrix.getRowDimension(); i++) {
                        for (int j = 0; j < matrix.getColumnDimension(); j++) {
                            matrix.set(i, j, x[index++]);
                        }
                    }
                }
            }
        }

        calculateOutputs(t);

        for (Block block: blockList) {
            block.calculateDerivative(t);
        }

        index = 0;
        for (Block block: blockList) {
            for(State state: block.getStateList()){
                if(state.getDataType() == DataType.REAL){
                    xDot[index++] = state.getDerivateData().getInitValue();
                }else{
                    Matrix matrix = state.getDerivateData().getMatrix();
                    for (int i = 0; i < matrix.getRowDimension(); i++) {
                        for (int j = 0; j < matrix.getColumnDimension(); j++) {
                            xDot[index++] = matrix.get(i, j);
                        }
                    }
                }
            }
        }

    }

    private void calculateDiscreteUpdates(double t) {
        // 更新离散状态
        // 类似Simulink的mdlUpdate
        for(Block block: blockList){
            block.calculateDiscreteUpdate(t);
        }
    }

    private void calculateInits(double t, double[] x){
        // 计算各个模块的初始值
        // 类似Simulink的mdlInitialize
        for(Block block: blockList){
            block.calculateInit();
        }

        int index = 0;
        for(Block block: blockList) {
            for (State state : block.getStateList()) {
                if (state.getDataType() == DataType.REAL) {
                    x[index++] = state.getData().getInitValue();
                } else {
                    Matrix matrix = state.getData().getMatrix();
                    for (int i = 0; i < matrix.getRowDimension(); i++) {
                        for (int j = 0; j < matrix.getColumnDimension(); j++) {
                            x[index++] = matrix.get(i, j);
                        }
                    }
                }
            }
        }
    }

    private void calculateTerminates(double t){
        for(Block block: blockList){
            block.calculateTerminate(t);
        }
    }

    // 对应 C++ 的 writeScope 函数
    private void writeScope(int cursor, ScopeStruct scope, JSONArray jsonScopes) {
        Scope scopeBlock = (Scope) scope.getBlock();
        JSONObject jsonScope = new JSONObject();

        jsonScope.put("width", scope.getWidth());
        jsonScope.put("height", scope.getHeight());
        jsonScope.put("name", scopeBlock.getBlockName());
        jsonScope.put("path", scopeBlock.getBlockPath());
        jsonScope.put("uuid", scopeBlock.getBlockUUID());

        int size = scope.getTimeList().size();
        jsonScope.put("length", size);

        JSONArray time = new JSONArray();
        JSONArray data = new JSONArray();

        // 处理时间和数据列表
        while (!scope.getTimeList().isEmpty() && !scope.getDataList().isEmpty()) {
            time.put(scope.getTimeList().remove(0));

            for (int h = 0; h < scope.getHeight(); h++) {
                for (int w = 0; w < scope.getWidth(); w++) {
                    data.put(scope.getDataList().remove(0));
                }
            }
        }

        jsonScope.put("time", time);
        jsonScope.put("data", data);

        jsonScopes.put(cursor, jsonScope);
    }

    @Getter
    protected String m2plabRoot = Optional.ofNullable(System.getenv("M2PLAB_ROOT")).orElse("/data/M2PLab");

    // Get the code path base using properties, with environment variable substitution
    protected String codePathBase=("deploy".equals(Property.instance.getProperty("mode").trim())?
        Property.instance.getProperty("CCodePath")
        :
        Property.instance.getProperty("CCodePathWin"))
        .replace("${M2PLAB_ROOT}", m2plabRoot)
        .replace("${user.home}", System.getProperty("user.home"))
        .replace("${user.dir}", System.getProperty("user.dir"));


    @Deprecated // 兼容旧版本
    private void writeResultFiles(){

        String userPath=codePathBase+getUserId();

        File dir=new File(userPath);
        if(!dir.exists()) {
            dir.mkdir();
        }


        String modelPath=userPath+"/"+getModelId();
        dir=new File(modelPath);
        if(!dir.exists()) {
            dir.mkdir();
        }

        JSONObject result = new JSONObject();
        JSONArray jsonScopes = new JSONArray();

        int scopeCursor = 0;

        for (Terminal terminal : getTerminalList()) {
            if(!(terminal instanceof ScopeStruct)){
                continue;
            }
            writeScope(scopeCursor, (ScopeStruct) terminal, jsonScopes);
            scopeCursor++;
        }

        result.put("version", "0.1");
        result.put("scopes", jsonScopes);

        // 将 JSON 写入文件
        try (FileWriter file = new FileWriter(modelPath + "/results.json")) {
            file.write(result.toString());
            System.out.println("JSON 写入成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



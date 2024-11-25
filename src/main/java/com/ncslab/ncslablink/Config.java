package com.ncslab.ncslablink;

import org.json.JSONObject;

public class Config {

	private double fixedStep;
	private String solver;
	private double startTime;
	private double stopTime;

	private int MaxDataPoints=2000;

	Config(JSONObject configIn,ModelMode mode){

        if (configIn.has("FixedStep")) {
            Object fixedStepObj = configIn.get("FixedStep");
            if (fixedStepObj instanceof String) {
                String fixedStepStr = (String) fixedStepObj;
                if (fixedStepStr.equals("auto")) {
                    this.fixedStep = 0.01;
                } else {
                    try {
                        this.fixedStep = Double.parseDouble(fixedStepStr);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format for FixedStep: " + fixedStepStr);
                        this.fixedStep = 0.01; // 或者设置为默认值，或者根据你的需求处理错误
                    }
                }
            } else if (fixedStepObj instanceof Double || fixedStepObj instanceof Integer) {
                // 如果FixedStep是一个数字，直接赋值
                this.fixedStep = ((Number) fixedStepObj).doubleValue();
            } else {
                System.err.println("Invalid type for FixedStep: " + fixedStepObj.getClass().getName());
                this.fixedStep = 0.01; // 或者设置为默认值，或者根据你的需求处理错误
            }
        } else {
            // 如果FixedStep键不存在，设置一个默认值
            this.fixedStep = 0.01;
        }

		this.solver="auto";
		if(!configIn.isNull("Solver")) {
			this.solver=configIn.getString("Solver");
		}


//		if(configIn.getString("FixedStep").equals("auto")) {
//			this.fixedStep=0.01;
//		}
//		else {
//			this.fixedStep=configIn.getDouble("FixedStep");
//		}
//
//		this.solver="auto";
//		if(configIn.isNull("Solver")==false) {
//			this.solver=configIn.getString("Solver");
//		}

		if(mode==ModelMode.Simulation) {
			this.startTime=configIn.getDouble("StartTime");
			this.stopTime=configIn.getDouble("StopTime");
			this.MaxDataPoints=configIn.getInt("MaxDataPoints");
		}
		else {
			this.startTime=0;
			this.stopTime=10;
		}
	}

	public static Config createFromJSON(JSONObject configIn,ModelMode mode) {
		Config config=new Config(configIn,mode);
		return config;
	}

	public double getFixedStep() {
		return fixedStep;
	}

	public int getMaxDataPoints() {
		return this.MaxDataPoints;
	}

	public String getSolver() {
		return solver;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getStopTime() {
		return stopTime;
	}
}

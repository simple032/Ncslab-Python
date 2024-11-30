package com.ncslab.ncslablink;

import org.json.JSONObject;

public class Config {

	private double fixedStep;
	private String solver;
	private double startTime;
	private double stopTime;

	private int MaxDataPoints=2000;

	Config(JSONObject configIn,ModelMode mode){


        this.fixedStep = configIn.optDouble("FixedStep", 0.01);

		this.solver=configIn.optString("Solver", "auto");

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

        this.startTime=configIn.optDouble("StartTime", 0);
        this.stopTime=configIn.optDouble("StopTime", 10);
        this.MaxDataPoints=configIn.optInt("MaxDataPoints", 3000);

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

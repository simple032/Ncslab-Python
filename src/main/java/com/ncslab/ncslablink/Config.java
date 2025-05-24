package com.ncslab.ncslablink;

import lombok.Getter;
import org.json.JSONObject;

@Getter
public class Config {

	private double fixedStep;
	private String solver;
	private double startTime;
	private double stopTime;
    private double minStep;
    private double absTol;
    private double relTol;
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
        this.minStep=configIn.optDouble("MinStep", 1e-6);
        this.absTol=configIn.optDouble("AbsTol", 1e-6);
        this.relTol=configIn.optDouble("RelTol", 1e-6);

	}

	public static Config createFromJSON(JSONObject configIn,ModelMode mode) {
        return new Config(configIn,mode);
	}

}

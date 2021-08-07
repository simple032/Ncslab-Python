package ncslablink;

import org.json.JSONObject;

public class Config {
	
	private double fixedStep;
	private String solver;
	private double startTime;
	private double stopTime;
	
	Config(JSONObject configIn){
		this.fixedStep=configIn.getDouble("FixedStep");
		this.solver=configIn.getString("Solver");
		this.startTime=configIn.getDouble("StartTime");
		this.stopTime=configIn.getDouble("StopTime");
	}
	
	public static Config createFromJSON(JSONObject configIn) {
		Config config=new Config(configIn);
		return config;
	}
	
	double getFixedStep() {
		return fixedStep;
	}
	
	String getSolver() {
		return solver;
	}
	
	double getStartTime() {
		return startTime;
	}
	
	double getStopTime() {
		return stopTime;
	}
}

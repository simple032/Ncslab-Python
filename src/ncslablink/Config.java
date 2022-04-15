package ncslablink;

import org.json.JSONObject;

public class Config {
	
	private double fixedStep;
	private String solver;
	private double startTime;
	private double stopTime;
	
	private int MaxDataPoints=2000;
	
	Config(JSONObject configIn,ModelMode mode){
		this.fixedStep=configIn.getDouble("FixedStep");
		this.solver=configIn.getString("Solver");
		
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

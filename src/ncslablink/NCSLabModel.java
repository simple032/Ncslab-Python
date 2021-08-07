package ncslablink;

import org.json.JSONObject;

public class NCSLabModel {
	
	private JSONObject jsonIn;
	
	private String modelName;
	private String modelRealName;
	
	private Config config;
	
	NCSLabModel(JSONObject jsonIn){
		this.jsonIn=jsonIn;
		
		parseModel();
		
		System.out.println(config.getFixedStep());
	}
	
	public static NCSLabModel createFromJSON(JSONObject jsonIn) {
		NCSLabModel model=new NCSLabModel(jsonIn);
		
		return model;
	}
	
	private void parseModel() {
		modelName=jsonIn.getString("modelName");
		modelRealName=jsonIn.getString("modelRealName");
		
		config=Config.createFromJSON(jsonIn.getJSONObject("config"));
	}

}

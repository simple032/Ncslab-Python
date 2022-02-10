package ncslablink;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;

import block.Block;
import block.BlockType;

import line.Line;

abstract public class NCSLabModel {
	
	private static int modelSeqCount=0;
	
	protected int modelSeq;
	
	//从Web传递过来的JSON文件
	private JSONObject jsonIn;
	
	//model的名称，S开头后面跟数字
	private String modelName;
	//model的真正名字
	private String modelRealName;
	
	//Model的配置文件
	private Config config;
	
	private int userId;
	private int modelId;
	private int testRig;
	
	private long uuid;
	
	private JSONObject saveInfo;
	
	private ModelMode mode=ModelMode.Simulation;
	
	//所有的模块
	protected Vector<Block> blockList=new Vector<Block>();
	
	//所有的连线
	protected Vector<Line> lineList=new Vector<Line>();
	
	//所有的错误信息
	protected Vector<ErrorMessage> errorList=new Vector<ErrorMessage>();
	
	//解析model，变成数据结构
	protected NCSLabModel(JSONObject jsonIn,ModelMode mode) throws ModelException{
		this.mode=mode;
		
		this.jsonIn=jsonIn;
		
		//解析model，变成数据结构
		parseModel();
		
		modelSeq=modelSeqCount;
		
		modelSeqCount++;
		
		//System.out.println(config.getFixedStep());
	}
	
	public int getModelSeq() {
		return modelSeq;
	}
	
	public String getModelRealName() {
		return this.modelRealName;
	}
	
	public Config getConfig() {
		return config;
	}
	
	public int getUserId() {
		return this.userId;
	}
	
	public int getModelId() {
		return this.modelId;
	}
	
	public long getUuid() {
		return uuid;
	}
	
	public int getTestRig() {
		return testRig;
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public Vector<ErrorMessage> getErrorList(){
		return this.errorList;
	}
	
	protected void addErrorMessage(ErrorMessage message) {
		errorList.add(message);
	}
	
	public Vector<Block> getBlockList(){
		return this.blockList;
	}
	
	private void parseModel() throws ModelException{

		//解析各个JSON项目
		modelName=jsonIn.getString("modelName");
		modelRealName=jsonIn.getString("modelRealName");
		
		userId=jsonIn.getInt("userId");
		modelId=jsonIn.getInt("modelId");
		uuid=jsonIn.getLong("uuid");
		testRig=jsonIn.getInt("testRig");
		
		//解析JSON的Config
		config=Config.createFromJSON(jsonIn.getJSONObject("config"),mode);
		
		saveInfo=jsonIn.getJSONObject("saveInfo");
		
		//解析各个Block
		parseBlocks();
		//解析各条连线
		parseLines();
		
		updateDimensions();
		
		//showBlocks();
	}
	
	public JSONObject getSaveInfo() {
		return saveInfo;
	}
	
	private void showBlocks() {
		for(Block block:blockList) {
			System.out.println("+++++++++++++++++++++++++++");
			System.out.println("Name: "+block.getBlockName());
			System.out.println("Type: "+block.getBlockType());
			System.out.println("In: "+block.getInputPortList().size()+" Out:"+block.getOutputPortList().size());
		}
	}
	
	public void showErrorMessages() {
		if(errorList.isEmpty()) {
			System.out.println("No error! Succeed");
		}
		else {
			int i=0;
			for(ErrorMessage error:errorList) {
				System.out.println("Error "+(i+1)+": "+error.getMessage());
				i++;
			}
		}
	}
	
	/*解析各个Block*/
	private void parseBlocks() throws ModelException{
		
		JSONArray blockJSONList=jsonIn.getJSONArray("blocks");
		for(int i=0;i<blockJSONList.length();i++) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);

			Block block=BlockType.createBlock(i+1,blockJSON,this);
			
			System.out.println("Parsing block ("+block.getBlockId()+"): '"+block.getBlockName()+"'...");
			
			if(block!=null) {
				blockList.add(block);
			}
			
		}
	}
	
	private void parseLines() {
		JSONArray lineJSONList=jsonIn.getJSONArray("lines");
		for(int i=0;i<lineJSONList.length();i++) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			
			//解析各条连线
			Line line=Line.createLine(lineJSON, this);
			line.setLineId(i+1);
			
			System.out.println("Parsing line ("+line.getLineId()+"): '"+line.getLinkedOutputPort().getBLock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")-->"+line.getLinkedInputPort().getBLock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")"); 
			
			if(line!=null) {
				lineList.add(line);
			}
		}
	}
	
	
	private void updateDimensions() throws MatDimException{
		for(Block block:blockList) {
			block.updateDimension();
		}
		
		for(Block block:blockList) {
			block.checkDimension();
		}
	}
}

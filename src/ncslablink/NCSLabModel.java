package ncslablink;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;

import block.Block;
import block.BlockType;

import line.Line;

public class NCSLabModel {
	
	//从Web端传过来的描述框图的json文件
	private JSONObject jsonIn;
	
	//模型的名字（s开头的带数字的名字）
	private String modelName;
	//模型的名字（用户指定的名字）
	private String modelRealName;
	
	//模型的配置参数
	private Config config;
	
	private int userId;
	private int modelId;
	
	//组件块的列表
	protected Vector<Block> blockList=new Vector<Block>();
	
	//线的列表
	protected Vector<Line> lineList=new Vector<Line>();
	
	//错误信息的列表
	protected Vector<ErrorMessage> errorList=new Vector<ErrorMessage>();
	
	//构造函数，从web传入json，建立模型的数据结构
	protected NCSLabModel(JSONObject jsonIn){
		this.jsonIn=jsonIn;
		
		//解析json中的内容，建立block,line,input,output相互连接的数据结构
		parseModel();
		
		//System.out.println(config.getFixedStep());
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
	
	public Vector<ErrorMessage> getErrorList(){
		return this.errorList;
	}
	
	public static NCSLabModel createFromJSON(JSONObject jsonIn) {
		NCSLabModel model=new NCSLabModel(jsonIn);
		
		return model;
	}
	
	protected void addErrorMessage(ErrorMessage message) {
		errorList.add(message);
	}
	
	public Vector<Block> getBlockList(){
		return this.blockList;
	}
	
	private void parseModel() {

		//读入json中的信息
		modelName=jsonIn.getString("modelName");
		modelRealName=jsonIn.getString("modelRealName");
		
		userId=jsonIn.getInt("userId");
		modelId=jsonIn.getInt("modelId");
		
		//读取json中的config配置
		config=Config.createFromJSON(jsonIn.getJSONObject("config"));
		
		//解析json文件中的block模块，建立block的数据结构
		parseBlocks();
		//解析json文件中的line模块，在数据结构中，用line连接各个block
		parseLines();
		
		//showBlocks();
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
	
	/*读取所有的block，建立block的数据结构 */
	private void parseBlocks() {
		
		JSONArray blockJSONList=jsonIn.getJSONArray("blocks");
		for(int i=0;i<blockJSONList.length();i++) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);

			//根据Block的type，建立不同的block的数据结构
			Block block=BlockType.createBlock(blockJSON,this);
			block.setBlockId(i+1);
			
			System.out.println("Parsing block ("+block.getBlockId()+"): '"+block.getBlockName()+"'...");
			
			//将block加入到blockList的Vector中
			if(block!=null) {
				blockList.add(block);
			}
			
		}
	}
	
	private void parseLines() {
		JSONArray lineJSONList=jsonIn.getJSONArray("lines");
		for(int i=0;i<lineJSONList.length();i++) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			
			//建立Line的数据结构，连接两端的Block
			Line line=Line.createLine(lineJSON, this);
			line.setLineId(i+1);
			
			System.out.println("Parsing line ("+line.getLineId()+"): '"+line.getLinkedOutputPort().getBLock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")-->"+line.getLinkedInputPort().getBLock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")"); 
			
			//将Line加入到lineList中
			if(line!=null) {
				lineList.add(line);
			}
		}
	}

}

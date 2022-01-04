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
	
	//��Web�˴�������������ͼ��json�ļ�
	private JSONObject jsonIn;
	
	//ģ�͵����֣�s��ͷ�Ĵ����ֵ����֣�
	private String modelName;
	//ģ�͵����֣��û�ָ�������֣�
	private String modelRealName;
	
	//ģ�͵����ò���
	private Config config;
	
	private int userId;
	private int modelId;
	private int testRig;
	
	private long uuid;
	
	private JSONObject saveInfo;
	
	private ModelMode mode=ModelMode.Simulation;
	
	//�������б�
	protected Vector<Block> blockList=new Vector<Block>();
	
	//�ߵ��б�
	protected Vector<Line> lineList=new Vector<Line>();
	
	//������Ϣ���б�
	protected Vector<ErrorMessage> errorList=new Vector<ErrorMessage>();
	
	//���캯������web����json������ģ�͵����ݽṹ
	protected NCSLabModel(JSONObject jsonIn,ModelMode mode) throws ModelException{
		this.mode=mode;
		
		this.jsonIn=jsonIn;
		
		//����json�е����ݣ�����block,line,input,output�໥���ӵ����ݽṹ
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

		//����json�е���Ϣ
		modelName=jsonIn.getString("modelName");
		modelRealName=jsonIn.getString("modelRealName");
		
		userId=jsonIn.getInt("userId");
		modelId=jsonIn.getInt("modelId");
		uuid=jsonIn.getLong("uuid");
		testRig=jsonIn.getInt("testRig");
		
		//��ȡjson�е�config����
		config=Config.createFromJSON(jsonIn.getJSONObject("config"),mode);
		
		saveInfo=jsonIn.getJSONObject("saveInfo");
		
		//����json�ļ��е�blockģ�飬����block�����ݽṹ
		parseBlocks();
		//����json�ļ��е�lineģ�飬�����ݽṹ�У���line���Ӹ���block
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
	
	/*��ȡ���е�block������block�����ݽṹ */
	private void parseBlocks() throws ModelException{
		
		JSONArray blockJSONList=jsonIn.getJSONArray("blocks");
		for(int i=0;i<blockJSONList.length();i++) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);

			//����Block��type��������ͬ��block�����ݽṹ
			Block block=BlockType.createBlock(i+1,blockJSON,this);
			
			System.out.println("Parsing block ("+block.getBlockId()+"): '"+block.getBlockName()+"'...");
			
			//��block���뵽blockList��Vector��
			if(block!=null) {
				blockList.add(block);
			}
			
		}
	}
	
	private void parseLines() {
		JSONArray lineJSONList=jsonIn.getJSONArray("lines");
		for(int i=0;i<lineJSONList.length();i++) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			
			//����Line�����ݽṹ���������˵�Block
			Line line=Line.createLine(lineJSON, this);
			line.setLineId(i+1);
			
			System.out.println("Parsing line ("+line.getLineId()+"): '"+line.getLinkedOutputPort().getBLock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")-->"+line.getLinkedInputPort().getBLock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")"); 
			
			//��Line���뵽lineList��
			if(line!=null) {
				lineList.add(line);
			}
		}
	}
	
	
	private void updateDimensions() {
		for(Block block:blockList) {
			block.updateDimension();
		}
	}
}

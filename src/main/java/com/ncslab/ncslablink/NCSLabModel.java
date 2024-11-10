package com.ncslab.ncslablink;

import lombok.Getter;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;

import com.ncslab.block.Block;
import com.ncslab.block.BlockType;

import com.ncslab.line.Line;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.terminal.Terminal;

import com.ncslab.circuit.CircuitParser;
import com.ncslab.circuit.loop.CircuitLoopException;

import com.ncslab.ncslablink.Config;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.MatDimException;

abstract public class NCSLabModel {

	private static int modelSeqCount=0;

	@Getter
    protected int modelSeq;

    //xiazhiqiang:获取JsonIn
    @Getter
    private JSONObject jsonIn; // json from the web
	@Getter
    private String modelName; // format: S + number
	@Getter
    private String modelRealName;
	@Getter
    private Config config; // config file for the model

	@Getter
    private int userId;
	@Getter
    private int modelId;
	@Getter
    private int testRig;

	@Getter
    private long uuid;

	@Getter
    private JSONObject saveInfo;

	private ModelMode mode = ModelMode.Simulation;

	// all blocks in the model
	@Getter
    protected Vector<Block> blockList=new Vector<Block>();

	private Vector<Block> dimensionList=new Vector<Block>();
	private Vector<Block> scanDimList=new Vector<Block>();
	private Vector<Block> dimTerminalBlockList=new Vector<Block>();
	private Vector<OutputPort> dimOutputPortPathList=new Vector<OutputPort>();

	// all lines
	protected Vector<Line> lineList=new Vector<Line>();

	// all error info
	@Getter
    protected Vector<ErrorMessage> errorList=new Vector<ErrorMessage>();

	@Getter
    protected Vector<Terminal> terminalList=new Vector<Terminal>();

	private int blockSeq=0;
	private int lineSeq=0;

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

	public ModelMode getModelMode() {
		return this.mode;
	}

    public JSONArray getBlocksJSON() {
		return jsonIn.getJSONArray("blocks");
	}

	public JSONArray getLinesJSON() {
		return jsonIn.getJSONArray("lines");
	}

    protected void addErrorMessage(ErrorMessage message) {
		errorList.add(message);
	}

    public void addTerminal(Terminal terminal){
		terminalList.add(terminal);
	}

    private void addCircuitBlocks(CircuitParser circuitPaser) {
		Vector<Block> circuitBlockList=circuitPaser.getCircuitModel().getModelBlocks();

		for(Block block:circuitBlockList) {
			block.setBlockId(blockSeq+1);
			blockSeq++;
			blockList.add(block);
		}
	}

	private void addCircuitLines(CircuitParser circuitPaser) {
		Vector<Line> circuitLineList=circuitPaser.getCircuitModel().getModelLines();

		for(Line line:circuitLineList) {
			line.setLineId(lineSeq+1);
			lineSeq++;
			lineList.add(line);
		}
	}

	private void parseModel() throws ModelException{
		//System.out.println(this.getBlocksJSON());
		//System.out.println(this.getLinesJSON());
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
		JSONArray blockJSONList=jsonIn.getJSONArray("blocks");
		//在解析电路模块时增加判断，防止出现在单独进行控制类实验时出现无法解析电路模块的问题
		int i = 0;
		int j = 0;
		while (i < blockJSONList.length()) {
			JSONObject blockJSON = blockJSONList.getJSONObject(i);
			// 如果含电路模块
			if (blockJSON.getString("srcBlock").startsWith("fl_lib")) {
				j = j + 1;
			}
			i++;
		}
		CircuitParser circuitParser=null;
		if(j!=0) {
			// 解析电路模块,把电路图转换成框图
			circuitParser = new CircuitParser(this);
			// circuitPaser.showBlocks();

			// 把电路图中转换生成的模块都加入到BlockList中
			addCircuitBlocks(circuitParser);
			// 把电路图中转换生成的都加入LineList
			addCircuitLines(circuitParser);
		}

		//解析各个Block
		parseBlocks();
		//xiazhiqiang:检查模块命名是否唯一
		checkBlocksName();
		//解析各条连线
		parseLines();
		//TODO:检查是否有空端口
		checkUnlinkedPorts();


		if(j!=0) {
			// 解开代数环的代码
			try {
				circuitParser.getCircuitModel().loopProcess();
			}
			catch(CircuitLoopException e) {
				//throw(new ModelException("Loop error"));
				circuitParser.getCircuitModel().clearElectBlockLoop();
			}
		}
		setupDimensionList();

		updateDimensions();

		showBlocks();
	}

    private void showBlocks() {
		for(Block block:blockList) {
			System.out.println("+++++++++++++++++++++++++++");
			System.out.println("ID: "+block.getBlockId());
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

			Block block=BlockType.createBlock(blockSeq+1,blockJSON,this);
			blockSeq++;

			System.out.println("Parsing block ("+block.getBlockId()+"): '"+block.getBlockName()+"'...");

            blockList.add(block);

        }
	}

	private void parseLines() {
		JSONArray lineJSONList=jsonIn.getJSONArray("lines");
		for(int i=0;i<lineJSONList.length();i++) {
			JSONObject lineJSON=lineJSONList.getJSONObject(i);
			//xiazhiqiang:隐去子系统连线，并将输入连线链接到子系统的In，输出连线链接到子系统的Out
			replaceInLine(lineJSON);
			replaceOutLine(lineJSON);
			//解析各条连线
			Line line=Line.createLine(lineJSON, this);
			line.setLineId(lineSeq+1);
			lineSeq++;

			System.out.println("Parsing line ("+line.getLineId()+"): '"+line.getLinkedOutputPort().getBLock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")-->"+line.getLinkedInputPort().getBLock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")");

            lineList.add(line);
        }
	}


	private void updateDimensions() throws MatDimException{
		for(Block block:dimensionList) {
			block.updateDimension();
		}

		for(Block block:dimensionList) {
			block.checkDimension();
		}
	}

	private void findDimTerminalBlocks() {
		System.out.println("Looking for terminal blocks");
		for(Block block:blockList) {
			if(block.isTerminalBlock()) {
				System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
				dimTerminalBlockList.add(block);
			}
		}
	}

	private void checkUnlinkedPorts() throws ModelException{
		//空的input连接到constant
        Vector<Block> fullBlockList = (Vector<Block>) blockList.clone();
		for(Block block:blockList) {
			for(int i=0; i<block.getInputPortList().size();i++) {
                InputPort input = block.getInputPortList().get(i);
				if(input.getLinkedLine()==null) {
					//如果输入端口没有连接，则连接到constant
					// {"blockType": "Constant", "blockName": "Constant1", "position": [100, 400, 160, 460], "paramValues": {"Value": "10"}}
					JSONObject blockJSON = new JSONObject();
					blockJSON.put("blockType", "Constant");
					blockJSON.put("blockName", "Auto_Constant"+(blockSeq));
					JSONObject paramValues = new JSONObject();
					paramValues.put("Value", "0");
					blockJSON.put("paramValues", paramValues);
                    blockJSON.put("blockPath", block.getBlockPath());

                    Block newBlock= null;

                    newBlock = BlockType.createBlock(blockSeq+1,blockJSON,this);

                    blockSeq++;

					System.out.println("Parsing block ("+newBlock.getBlockId()+"): '"+newBlock.getBlockName()+"'...");

                    fullBlockList.add(newBlock);

                    JSONObject lineJSON = new JSONObject();
                    lineJSON.put("fromBlockName", newBlock.getBlockName());
                    lineJSON.put("fromPortNo", 1);

                    lineJSON.put("toBlockName", block.getBlockName());
                    lineJSON.put("toPortNo", i+1);
                    lineJSON.put("linePath", block.getBlockPath());

                    Line line=Line.createLine(lineJSON, fullBlockList);
                    line.setLineId(lineSeq+1);
                    lineSeq++;

                    System.out.println("Parsing line ("+line.getLineId()+"): '"+line.getLinkedOutputPort().getBLock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")-->"+line.getLinkedInputPort().getBLock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")");

                    lineList.add(line);
				}
			}
		}
		//空的output连接terminal
        for(Block block:blockList){
            for(int i=0; i<block.getOutputPortList().size();i++) {
                OutputPort output = block.getOutputPortList().get(i);
                if(output.getLinkedLineList().isEmpty()) {
                    //如果输入端口没有连接，则连接到constant
                    // {"blockType": "Terminator", "blockName": "Terminator1", "position": [100, 400, 160, 460], "paramValues": {}}
                    JSONObject blockJSON = new JSONObject();
                    blockJSON.put("blockType", "Terminator");
                    blockJSON.put("blockName", "Auto_Terminator" + (blockSeq));
                    JSONObject paramValues = new JSONObject();
                    blockJSON.put("paramValues", paramValues);
                    blockJSON.put("blockPath", block.getBlockPath());


                    Block newBlock = null;

                    newBlock = BlockType.createBlock(blockSeq, blockJSON, this);

                    blockSeq++;

                    System.out.println("Parsing block (" + newBlock.getBlockId() + "): '" + newBlock.getBlockName() + "'...");

                    fullBlockList.add(newBlock);

                    JSONObject lineJSON = new JSONObject();
                    lineJSON.put("fromBlockName", block.getBlockName());
                    lineJSON.put("fromPortNo", i + 1);

                    lineJSON.put("toBlockName", newBlock.getBlockName());
                    lineJSON.put("toPortNo", 1);
                    lineJSON.put("linePath", block.getBlockPath());

                    Line line = Line.createLine(lineJSON, fullBlockList);
                    line.setLineId(lineSeq + 1);
                    lineSeq++;

                    System.out.println("Parsing line (" + line.getLineId() + "): '" + line.getLinkedOutputPort().getBLock().getBlockName() + "(" + line.getLinkedOutputPort().getNumber() + ")-->" + line.getLinkedInputPort().getBLock().getBlockName() + "(" + line.getLinkedInputPort().getNumber() + ")");

                    lineList.add(line);
                }

            }
        }
        if(blockList.size() != fullBlockList.size())
            blockList = fullBlockList;
	}

	private void scanDimInputPort(InputPort inputPort) {
		Line line=inputPort.getLinkedLine();
		OutputPort outputPort=line.getLinkedOutputPort();

		//如果输出端口已经生成完毕，则不用再生成，结束这一个分支的遍历
		if(outputPort.getIsDimScaned()==true) {
			return;
		}

		for(OutputPort output:dimOutputPortPathList) {
			if(output==outputPort) {
				return;
			}
		}

		//记录这个OutputPort已经在现有路径回路中，作为记忆
		dimOutputPortPathList.add(outputPort);

		//如果没有生成，那就遍历block，生成这个block的代码
		Block block = outputPort.getBLock();

		boolean isDimThroughBlock = false;
		Vector<OutputPort> outputPortList = block.getOutputPortList();
		for (OutputPort output : outputPortList) {
			if (output.getDimThrough()== true) {
				isDimThroughBlock = true;
			}
		}

		//如果有Feedthrough的模块，则要遍历整个模块的InputPort
		if(isDimThroughBlock) {
			block.setIsDimScaned(true);
			Vector<InputPort> InputPortList=block.getInputPortList();
			for(InputPort input:InputPortList) {
				//递归调用，实现遍历
				scanDimInputPort(input);
			}
			//遍历完成，也要生成模块的输出代码
			//generateBlockOutputCode(block);
			dimensionList.add(block);
			//block.setIsDimScaned(true);
		}
		else {
			block.setIsDimScaned(true);
			Vector<InputPort> InputPortList=block.getInputPortList();

			//如果dimThrough是真的话，说明这是类似Sum和Add的模块，输入的Dimension必须相互配合
			//扫描第一个输入
			if(InputPortList.size()!=0) {
				scanDimInputPort(InputPortList.get(0));
			}

			if(InputPortList.size()>1) {
				for(int i=1;i<InputPortList.size();i++) {
					InputPort input=InputPortList.get(i);
					Block linkedBlock=input.getLinkedLine().getLinkedOutputPort().getBLock();
					if(linkedBlock.getIsDimScaned()==false) {
						scanDimList.add(linkedBlock);
					}
				}
			}

			dimensionList.add(block);
			//block.setIsDimScaned(true);

			//尽管这个模块的输出计算不取决于当前的输入，但是它的Update还是需要输入量的计算。因此将这个模块加入scanBlockList，进入二次遍历
			//scanDimList.add(block);
		}
		//清除记忆的路径回路中的这个模块
		dimOutputPortPathList.remove(dimOutputPortPathList.size()-1);
	}

	/*进行遍历的方法*/
	private void scanDimChain() {
		System.out.println("scaning outputChain");

		//遍历所有的终端模块
		for(Block block:dimTerminalBlockList) {
			block.setIsDimScaned(true);
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanDimInputPort(inputPort);
			}
			dimensionList.add(block);
			//block.setIsDimScaned(true);
		}


		//进行二次遍历，因为二次遍历过程中，scanDimList中的元素动态变化，所有要用while循环
		while(scanDimList.isEmpty()==false) {
			//取出第一个元素进行遍历
			Block block=scanDimList.remove(0);
			if(block.getIsDimScaned()) {
				continue;
			}
			block.setIsDimScaned(true);
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanDimInputPort(inputPort);
			}
			dimensionList.add(block);
			//block.setIsDimScaned(true);
		}
	}

	private void showDimBlocks() {
		int i=1;
		for(Block block:dimensionList) {
			System.out.println("("+i+")"+block.getBlockName()+"("+block.getBlockId()+")");
			i++;
		}
	}

	private void setupDimensionList() {
		findDimTerminalBlocks();
		scanDimChain();

		showDimBlocks();
	}

    //xiazhiqiang:检查前端模块是否存在命名相同的情况
	private void checkBlocksName() throws MatDimException{
		for(Block block:blockList) {
			int i=0;
			String name=block.getBlockName();
			for(Block block1:blockList) {
				if(name.equals(block1.getBlockName())) {
					i=i+1;
				}
			}
			if(i>1) {
				MatDimException e=new MatDimException(block.getBlockName()+" Name is not unique!\n \n");
				throw(e);
			}
		}
	}

    //xiazhiqiang:隐去Subsystem的输入连线，同时将该连线的输出连接到子系统中的In
    private void replaceInLineBack(JSONObject lineJSON) {
        String toBlockName=lineJSON.getString("toBlockName");
        String blockPath=null;
        for(Block block:blockList) {
            if(block.getBlockName().equals(toBlockName)&&block.getBlockType().equals("Subsystem")) {
                blockPath=block.getBlockPath()+"/"+toBlockName;
                break;
            }
        }
        for(Block block1:blockList) {
            System.out.println("block1's getBlockPath: "+block1.getBlockPath()+
                "  getBlockType: "+block1.getBlockType());
            if(block1.getBlockPath().equals(blockPath)&&block1.getBlockType().equals("In")){
                lineJSON.put("toBlockName", block1.getBlockName());
                break;
            }
        }
    }
    private void replaceInLine(JSONObject lineJSON) {
        String toBlockName=lineJSON.getString("toBlockName");
        String blockPath=null;
        String toPortNo=lineJSON.getString("toPortNo");
        for(Block block:blockList) {
            if(block.getBlockName().equals(toBlockName)&&block.getBlockType().equals("Subsystem")) {
                blockPath=block.getBlockPath()+"/"+toBlockName;
                break;
            }
        }
        for(Block block1:blockList) {
            System.out.println("block1's getBlockPath: "+block1.getBlockPath()+
                "  getBlockType: "+block1.getBlockType());

            if(block1.getBlockPath().equals(blockPath)&&block1.getBlockType().equals("In")&&block1.getParamValues().getString("No").equals(toPortNo)){
                lineJSON.put("toBlockName", block1.getBlockName());
                break;
            }
        }
    }
    //xiazhiqiang:隐去Subsystem的输出连线，同时将该连线的输入连接到子系统中的out
    private void replaceOutLineBack(JSONObject lineJSON) {
        String fromBlockName=lineJSON.getString("fromBlockName");
        String blockPath=null;
        for(Block block:blockList) {
            if(block.getBlockName().equals(fromBlockName)&&block.getBlockType().equals("Subsystem")) {
                blockPath=block.getBlockPath()+"/"+fromBlockName;
                break;
            }
        }
        for(Block block1:blockList) {
            if(block1.getBlockPath().equals(blockPath)&&block1.getBlockType().equals("Out")){
                lineJSON.put("fromBlockName", block1.getBlockName());
                break;
            }
        }
    }

    private void replaceOutLine(JSONObject lineJSON) {
        String fromBlockName=lineJSON.getString("fromBlockName");
        String blockPath=null;
        String fromPortNo=lineJSON.getString("fromPortNo");
        for(Block block:blockList) {
            if(block.getBlockName().equals(fromBlockName)&&block.getBlockType().equals("Subsystem")) {
                blockPath=block.getBlockPath()+"/"+fromBlockName;
                break;
            }
        }
        for(Block block1:blockList) {
            if(block1.getBlockPath().equals(blockPath)&&block1.getBlockType().equals("Out")&&block1.getParamValues().getString("No").equals(fromPortNo)){
                lineJSON.put("fromBlockName", block1.getBlockName());
                break;
            }
        }
    }
}

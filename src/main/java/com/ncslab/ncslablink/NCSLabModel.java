package com.ncslab.ncslablink;

import com.ncslab.block.io.State;
import com.ncslab.block.route.From;
import com.ncslab.block.route.To;
import com.ncslab.circuit.block.electblock.ElectBlock;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.checkerframework.checker.units.qual.s;
import org.json.JSONArray;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.model.LineDto;
import com.ncslab.dto.model.SaveInfoDto;
import com.ncslab.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Generated;

import com.greenpineyu.fel.function.operator.Sub;
import com.ncslab.block.Block;
import com.ncslab.block.BlockType;

import com.ncslab.line.Line;
import com.ncslab.system.NCSLabSystem;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.terminal.Terminal;

import com.ncslab.circuit.CircuitParser;
import com.ncslab.circuit.loop.CircuitLoopException;

import com.ncslab.block.subsystem.*;

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
    private String templateName;
    
    @Getter
    private int copyNum;
    
    @Getter
    private JSONObject option;

	@Getter
    private JSONObject saveInfo;

	private ModelMode mode;

    private boolean cidModeEnable=false;

	// all blocks in the model	
    protected List<From> fromBlockList = new ArrayList<>();
    protected List<To> gotoBlockList = new ArrayList<>();
    protected List<Subsystem> subsystemBlockList = new ArrayList<>();
    protected List<In> inBlockList = new ArrayList<>();
    protected List<Out> outBlockList = new ArrayList<>();

	private List<Block> dimensionList = new ArrayList<>();
	private List<Block> scanDimList = new ArrayList<>();
	private List<Block> dimTerminalBlockList = new ArrayList<>();
	private List<OutputPort> dimOutputPortPathList = new ArrayList<>();

    // output chain
    private boolean isAlgebraicLoop=false;
    private List<Block> terminalBlockList = new ArrayList<>();

    private List<Block> scanBlockList = new ArrayList<>();
    private List<OutputPort> outputPortPathList = new ArrayList<>();

    //输出链，应该先输出哪个，然后再输出哪个
    protected List<Block> outputChain = new ArrayList<>();


    // all lines
	// protected List<Line> lineList = new ArrayList<>();
    protected List<Line> fromLineList = new ArrayList<>();
    protected List<Line> gotoLineList = new ArrayList<>();

	// all error info
	@Getter
    protected List<ErrorMessage> errorList = new CopyOnWriteArrayList<>();

	@Getter
    protected List<Terminal> terminalList = new ArrayList<>();

	private int blockSeq=0;
	private int lineSeq=0;

    //context
    @Getter
    protected int stateNum=0;
    @Getter
    protected int singleStateNum=0;
    @Getter
    protected int matrixStateNum=0;
    @Getter
    @Setter
    protected int signalNum=0;
    @Getter
    @Setter
    protected int parameterNum=0;
    @Getter
    protected int inputNum=0;
    @Getter
    protected int outputNum=0;

	@Getter
	private NCSLabSystem rootSystem = new NCSLabSystem();

	//解析model，变成数据结构 - 原有JSONObject构造函数
	protected NCSLabModel(JSONObject jsonIn,ModelMode mode) throws ModelException{
		initFromJsonObject(jsonIn, mode);
	}	

	// 新增：直接从DTO构造函数
	protected NCSLabModel(ModelDto modelDto, ModelMode mode) throws ModelException {
		if (modelDto == null) {
			throw new ModelException("ModelDto cannot be null");
		}
		
		if (!modelDto.isValid()) {
			throw new ModelException("Invalid ModelDto: " + modelDto.getValidationError());
		}
		
		initFromDto(modelDto, mode);
	}
	
	// 从JSONObject初始化的通用方法
	private void initFromJsonObject(JSONObject jsonIn, ModelMode mode) throws ModelException {
		this.mode = mode;
		this.jsonIn = jsonIn;
		
		//解析model，变成数据结构
		parseModel();
		
		//解析模型state,signal,parameter,input,output数量
		parseContext();
		
		//构造模块输出链
		setupOuputChain();
		
		modelSeq = modelSeqCount;
		modelSeqCount++;
	}
	
	// 从ModelDto DTO初始化的新方法
	private void initFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
		this.mode = mode;
		
		// 从DTO设置基本属性
		this.modelName = modelDto.getModelName();
		this.modelRealName = modelDto.getModelRealName();
		this.userId = modelDto.getUserId();
		this.modelId = modelDto.getModelId();
		this.uuid = modelDto.getUuid();
		this.testRig = modelDto.getTestRig();
		
		// 处理可选字段
		this.templateName = modelDto.getTemplateName();
		this.copyNum = modelDto.getCopyNum();
		
		// 为了向后兼容，创建等价的JSONObject
		// Removed fallback conversion - using native DTO approach
		
		// 解析配置 - 直接使用DTO方式
		try {
			if (modelDto.getConfig() != null) {
				// Using direct DTO config - create Config from DTO
				config = Config.createFromConfigDto(modelDto.getConfig(), mode);
			}
		} catch (Exception e) {
			System.err.println("Failed to parse config from DTO, skipping: " + e.getMessage());
		}
		
		// 解析保存信息 - 直接使用DTO方式
		try {
			if (modelDto.getSaveInfo() != null) {
				// Using direct DTO saveInfo - extract relevant fields
				saveInfo = createSaveInfoFromDto(modelDto.getSaveInfo());
			}
		} catch (Exception e) {
			System.err.println("Failed to parse saveInfo from DTO, skipping: " + e.getMessage());
		}
		
		// 处理options字段
		if (modelDto.getOption() != null && !modelDto.getOption().isEmpty()) {
			// Convert options map for compatibility
			this.option = new JSONObject(modelDto.getOption());
		}
		
		// 使用增强的DTO解析blocks和lines
		try {
			parseBlocksFromDto(modelDto.getBlocks());
			handleSubsystemRelationships();
			parseLinesFromDto(modelDto.getLines());
			moveSubsystemBlockLine();
		} catch (Exception e) {
			throw new ModelException("Failed to parse blocks/lines from DTO: " + e.getMessage());
		}
		
		// 继续现有工作流程
		rootSystem.calculateSystemState();
		
		// IMPORTANT: Add missing connection auto-generation for DTO path
		// This ensures DTO parsing has the same auto-generation behavior as JSON parsing
		System.out.println("Checking for unlinked ports and auto-generating missing connections...");
		int blockCountBefore = getBlockList().size();
		checkUnlinkedPorts();
		int blockCountAfter = getBlockList().size();
		if (blockCountAfter > blockCountBefore) {
			System.out.printf("Auto-generated %d blocks for unconnected ports%n", blockCountAfter - blockCountBefore);
		} else {
			System.out.println("No auto-generation needed - all ports are connected");
		}
		
		// CRITICAL FIX: Add missing dimension processing for DTO path
		// This was causing RT simulation to have empty scope results
		System.out.println("DTO Fix: Setting up dimension processing...");
		rootSystem.setupDimensionList();
		try {
			rootSystem.updateDimensions();
		} catch (Exception e) {
			System.err.println("DTO Fix: Error during dimension processing: " + e.getMessage());
			e.printStackTrace();
		}
		
		rootSystem.setupOutputChain();
		
		modelSeq = modelSeqCount;
		modelSeqCount++;
		
		System.out.println("Successfully initialized model from DTO: " + modelName + 
		                   " with " + getBlockList().size() + " blocks and " + getLineList().size() + " lines");
	}

	private void handleSubsystemRelationships() {
		for(Subsystem subSystem : subsystemBlockList) {
			String subSystemPath = subSystem.getFullPath();

			// Handle subsystem relationships
			for(In in : inBlockList) {
				if(Objects.equals(in.getBlockPath(), subSystemPath)) {
					System.out.println("Adding " + in.getBlockName() + " to subsystem: " + subSystem.getBlockName());
					subSystem.addIn(in);
				}
			}

			for(Out out : outBlockList) {
				if(Objects.equals(out.getBlockPath(), subSystemPath)) {
					System.out.println("Adding " + out.getBlockName() + " to subsystem: " + subSystem.getBlockName());
					subSystem.addOut(out);
				}
			}
			//Manual update due to the dynamic allocation of new ports
			subSystem.updateBlock();
		}
	}

	public ModelMode getModelMode() {
		return this.mode;
	}

    public JSONArray getBlocksJSON() {
		if (jsonIn != null) {
			return jsonIn.getJSONArray("blocks");
		}
		return new JSONArray(); // Return empty array for DTO-based parsing
	}

	public JSONArray getLinesJSON() {
		if (jsonIn != null) {
			return jsonIn.optJSONArray("lines");
		}
		return new JSONArray(); // Return empty array for DTO-based parsing
	}

    protected void addErrorMessage(ErrorMessage message) {
		errorList.add(message);
	}

    public void addTerminal(Terminal terminal){
		System.out.printf("RT Debug: Adding terminal %s to terminalList (size before: %d)%n", 
			terminal.getName(), terminalList.size());
		terminalList.add(terminal);
		System.out.printf("RT Debug: Terminal added, terminalList size now: %d%n", terminalList.size());
	}

    private void addCircuitBlocks(CircuitParser circuitPaser) {
		List<Block> circuitBlockList = circuitPaser.getCircuitModel().getModelBlocks();

		for(Block block:circuitBlockList) {
			block.setBlockId(blockSeq+1);
			blockSeq++;
			rootSystem.addBlock(block);
		}
	}

	private void addCircuitLines(CircuitParser circuitPaser) {
		List<Line> circuitLineList = circuitPaser.getCircuitModel().getModelLines();

		for(Line line:circuitLineList) {
			line.setLineId(lineSeq+1);
			lineSeq++;
			getLineList().add(line);
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
		JSONArray blockJSONList=jsonIn.optJSONArray("blocks");
		//在解析电路模块时增加判断，防止出现在单独进行控制类实验时出现无法解析电路模块的问题
		int i = 0;
		int j = 0;
		while (i < blockJSONList.length()) {
			JSONObject blockJSON = blockJSONList.getJSONObject(i);
			// 如果含电路模块
			if (blockJSON.has("srcBlock") && blockJSON.getString("srcBlock").startsWith("fl_lib")) {
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
        refactorSubsystemBlocks();
        showBlocks();
		//xiazhiqiang:检查模块命名是否唯一,
        // 20250313 ysw:由于存在子系统,导致模块命名不在具有唯一性，但是前端的path有问题，因此要改成检查模块cid是否唯一，
        checkBlocksCId();
        if(!cidModeEnable)
            checkBlocksName();

		//解析各条连线
		parseLines();

        //20241226:判断模块是否为From或Goto，在同样标签的模块间添加虚拟连线
//        replaceLogicLines();
        addLogicLines();

		//检查是否有空端口
		checkUnlinkedPorts();



		if(j!=0 && circuitParser!=null) {
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



	}

    private void parseContext(){
        int singleStateNum=0;
        int matrixStateNum=0;
        int signalNum=0;
        int parameterNum=0;
        for(Block block:getBlockList()) {
            int blockSignalNum = 0;
            for(State state:block.getStateList()) {
                switch(state.getDataType()) {
                    case REAL:
                        singleStateNum++;
                        break;
                    case MATRIX:
                        matrixStateNum++;
                        break;
                }
            }
            blockSignalNum = block.getOutputPortList().size() + block.getInputPortList().size();
            signalNum += blockSignalNum;
            parameterNum += block.getParameterList().size();
            block.setSignalNum(blockSignalNum);
        }
        setStateNum(singleStateNum,matrixStateNum);
        setSignalNum(signalNum);
        setParameterNum(parameterNum);
    }

    private void setStateNum(int singleStateNum,int matrixStateNum){
        this.stateNum=singleStateNum+matrixStateNum;
        this.singleStateNum=singleStateNum;
        this.matrixStateNum=matrixStateNum;
    }

    private void showBlocks() {
		for(Block block:getBlockList()) {
			System.out.println("+++++++++++++++++++++++++++");
			System.out.println("ID: "+block.getBlockId());
			System.out.println("Name: "+block.getBlockName());
            System.out.println("CId: "+block.getBlockUUID());
			System.out.println("Path: "+block.getBlockPath());
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

    /*建立输出链，决定应该先计算是你哪个模块，再计算哪个模块*/
    private void setupOuputChain() {

        //将电路系统有代数环模块设置好
        setupElectBlocks();

        //找到终端的Block
        findTerminalBlocks();
        //沿着终端模块，建立输出链
        scanOutputChain();
    }

    //将电路系统有代数环模块设置好
    private void setupElectBlocks() {
        System.out.println("Looking for elect blocks");
        for(Block block:getBlockList()) {
            if(block instanceof ElectBlock) {
                ElectBlock electBlock=(ElectBlock)block;
                //如果是loopPoint
                if(electBlock.isLoopPoint()) {
                    System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
                    //将它设置成FeedThrough
                    block.setFeedThrough(false);

                    List<Block> relatedBlockList = electBlock.getRelatedBlockList();

                    for(Block relatedBlock:relatedBlockList) {
                        if(!relatedBlock.getIsOutputCodeGenerated()) {
                            //将与loop计算相关的模块加入OutputChain
                            outputChain.add(relatedBlock);
                            //terminalBlockList.add(relatedBlock);
                            relatedBlock.setIsOuputCodeGenerated(true);
                            //同时加入二次搜索的列表,二次搜索的时候搜索输入的路径
                            scanBlockList.add(relatedBlock);
                        }
                    }

                    //将模块加入OutputChain,进行二次搜索
                    outputChain.add(block);
                    block.setIsOuputCodeGenerated(true);
                    scanBlockList.add(block);
                }
            }
        }
    }

    /*寻找终端Block的函数，将所有的终端block加入terminalBlockList，为遍历做准备 */
    // TODO: this method is duplicated with the one in NCSLabModel
    private void findTerminalBlocks() {
        System.out.println("Looking for terminal blocks");
        for(Block block:getBlockList()) {
            if(block.isTerminalBlock()) {
                System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
                terminalBlockList.add(block);
            }
        }
    }

    /*进行遍历的方法*/
    private void scanOutputChain() {
        System.out.println("scaning outputChain");

        //遍历所有的终端模块
        for(Block block:terminalBlockList) {
            List<InputPort> inputPortList = block.getInputPortList();
            for(InputPort inputPort:inputPortList) {
                scanInputPort(inputPort);
            }

            //code+=block.generateBlockOutputCodeM();
            //generateBlockOutputCode(block);
            outputChain.add(block);
            block.setIsOuputCodeGenerated(true);
        }

        //进行二次遍历，因为二次遍历过程中，scanBlockList中的元素动态变化，所有要用while循环
        while(!scanBlockList.isEmpty()) {
            //取出第一个元素进行遍历
            Block block=scanBlockList.remove(0);
            List<InputPort> inputPortList = block.getInputPortList();
            for(InputPort inputPort:inputPortList) {
                scanInputPort(inputPort);
            }
        }
    }

    private void scanInputPort(InputPort inputPort) {
        Line line=inputPort.getLinkedLine();
        OutputPort outputPort=line.getLinkedOutputPort();

        //如果输出端口已经生成完毕，则不用再生成，结束这一个分支的遍历
        if(outputPort.getIsCodeGenerated()==true) {
            return;
        }

        for(OutputPort output:outputPortPathList) {
            if(output==outputPort) {
                isAlgebraicLoop=true;
            }
        }

        //如果发现代数环
        if(isAlgebraicLoop) {
            String errorString;
            errorString="Found algorbet loop!!!";
            boolean isDisp=false;
            for(OutputPort output:outputPortPathList) {

                if(output==outputPort) {
                    isDisp=true;
                }

                if(isDisp) {
                    errorString+=output.getBlock().getBlockName()+"->";
                }
            }

            errorString+=outputPort.getBlock().getBlockName();

            System.err.println(errorString);
            ErrorMessage errorMessage=new ErrorMessage(ErrorMessage.AlgebraicLoop,errorString+"\n");
            addErrorMessage(errorMessage);
            isAlgebraicLoop=false;
            return;
        }


        //记录这个OutputPort已经在现有路径回路中，作为记忆
        outputPortPathList.add(outputPort);

        //如果没有生成，那就遍历block，生成这个block的代码
        Block block=outputPort.getBlock();

        boolean isFeedThroughBlock=false;
        List<OutputPort> outputPortList = block.getOutputPortList();
        for(OutputPort output:outputPortList) {
            if(output.getFeedThrough()==true) {
                isFeedThroughBlock=true;
            }
        }

        //如果有Feedthrough的模块，则要遍历整个模块的InputPort
        if(isFeedThroughBlock) {
            List<InputPort> InputPortList = block.getInputPortList();
            for(InputPort input:InputPortList) {
                //递归调用，实现遍历
                scanInputPort(input);
            }
            //遍历完成，也要生成模块的输出代码
            //generateBlockOutputCode(block);
            outputChain.add(block);
            block.setIsOuputCodeGenerated(true);
        }
        //如果没有，就直接生成模块的输出代码
        else {
            //生成模块的输出代码
            //generateBlockOutputCode(block);
            outputChain.add(block);
            block.setIsOuputCodeGenerated(true);

            //尽管这个模块的输出计算不取决于当前的输入，但是它的Update还是需要输入量的计算。因此将这个模块加入scanBlockList，进入二次遍历
            scanBlockList.add(block);
        }

        //清除记忆的路径回路中的这个模块
        outputPortPathList.remove(outputPortPathList.size()-1);
    }

    /*解析各个Block - 原有JSONObject方法*/
	private void parseBlocks() throws ModelException{

		JSONArray blockJSONList=jsonIn.getJSONArray("blocks");
		for(int i=0;i<blockJSONList.length();i++) {
			JSONObject blockJSON=blockJSONList.getJSONObject(i);

			Block block=BlockType.createBlock(blockSeq+1,blockJSON,this);
			blockSeq++;

//			System.out.println("Parsing block ("+block.getBlockId()+"): '"+block.getBlockName()+"'...");

            rootSystem.addBlock(block);
            categorizeBlock(block);
        }
	}
	
	/*解析各个Block - REAL DTO-NATIVE方法 (NO MORE CONVERSION!)*/
	private void parseBlocksFromDto(java.util.List<BlockDto> blockDtos) throws ModelException {
		if (blockDtos == null || blockDtos.isEmpty()) {
			System.out.println("No blocks to parse from DTO");
			return;
		}
		
		System.out.println("DTO-NATIVE: Parsing " + blockDtos.size() + " blocks directly from DTOs (no conversion)");
		
		for (BlockDto blockDto : blockDtos) {
			try {
				
				// **REAL DTO-NATIVE**: 直接从DTO创建Block实例，无需转换！
				Block block = BlockType.createBlockFromDto(blockSeq + 1, blockDto, this);
				if (block == null) {
					System.err.println("Failed to create block: " + blockDto.getBlockType() + "/" + blockDto.getBlockName());
					continue;
				}
				
				blockSeq++;
				rootSystem.addBlock(block);
				categorizeBlock(block);
				
				System.out.println("Successfully created block: " + block.getBlockType() + "/" + block.getBlockName());
				
			} catch (Exception e) {
				System.err.println("Error parsing block DTO: " + blockDto.getBlockName() + " - " + e.getMessage());
				// 继续处理其他块，不中断整个解析过程
			}
		}
		
		System.out.println("Successfully parsed " + getBlockList().size() + " blocks from DTO");
	}
	
	/**
	 * 将Block按类型分类到相应的列表中
	 * 从parseBlocks()方法中提取出来的通用逻辑
	 */
	private void categorizeBlock(Block block) {
		if(block instanceof From){
			fromBlockList.add((From) block);
		}else if(block instanceof To){
			gotoBlockList.add((To) block);
		}else if(block instanceof Subsystem){
			subsystemBlockList.add((Subsystem) block);
		}else if(block instanceof In){
			inBlockList.add((In) block);
		}else if(block instanceof Out){
			outBlockList.add((Out) block);
		}
	}

    private void refactorSubsystemBlocks() {
        for(Subsystem subsystem:subsystemBlockList){
            String subsystemPath = subsystem.getFullPath();
            
            // 将子系统和它的端口联系起来
            for(In in:inBlockList){
                if(in.getBlockPath().equals(subsystemPath)){
                    subsystem.addIn(in);
                    in.setSubsystem(subsystem);
                }
            }
            for(Out out:outBlockList){
                if(out.getBlockPath().equals(subsystemPath)){
                    subsystem.addOut(out);
                    out.setSubsystem(subsystem);
                }
            }
            
            // 将所有属于该子系统的块添加到containedBlocks中
            for(Block block:getBlockList()){
                if(block.getBlockPath().equals(subsystemPath)){
                    subsystem.addBlock(block);
                }
            }
            
            // 将所有属于该子系统的连线添加到containedLines中
            for(Line line:getLineList()){
                if(isLineWithinSubsystem(line, subsystemPath)){
                    subsystem.addLine(line);
                }
            }
        }
    }

	private void moveSubsystemBlockLine() {
        for(Subsystem subsystem:subsystemBlockList){
            String subsystemPath = subsystem.getFullPath();

			List<Block> blocksToRemove = new ArrayList<>();
			List<Line> linesToRemove = new ArrayList<>();

            // 将子系统和它的端口联系起来
            // 将所有属于该子系统的块添加到containedBlocks中
            for(Block block:getBlockList()){
                if(block.getBlockPath().equals(subsystemPath)){
                    blocksToRemove.add(block);
                }
            }
            
            // 将所有属于该子系统的连线添加到containedLines中
            for(Line line:getLineList()){
                if(isLineWithinSubsystem(line, subsystemPath)){
                    linesToRemove.add(line);
                }
            }

			for(Block block : blocksToRemove) {
				rootSystem.removeBlock(block);
				subsystem.addBlock(block);
			}
			for(Line line : linesToRemove) {
				rootSystem.removeLine(line);
				subsystem.addLine(line);
			}
        }
    }
    
    /**
     * Check if a line belongs to a specific subsystem based on the blocks it connects
     */
    private boolean isLineWithinSubsystem(Line line, String subsystemPath) {
        if (line.getLinkedOutputPort() == null || line.getLinkedInputPort() == null) {
            return false;
        }
        
        Block fromBlock = line.getLinkedOutputPort().getBlock();
        Block toBlock = line.getLinkedInputPort().getBlock();
        
        // Line belongs to subsystem if both connected blocks are within the subsystem path
        return fromBlock.getBlockPath().equals(subsystemPath) && 
               toBlock.getBlockPath().equals(subsystemPath);
    }

	/*解析各条连线 - 原有JSONObject方法*/
	private void parseLines() {
		JSONArray lineJSONList=jsonIn.optJSONArray("lines");
        if(lineJSONList != null) {
            for (int i = 0; i < lineJSONList.length(); i++) {
                JSONObject lineJSON = lineJSONList.getJSONObject(i);
                processLineDto(lineJSON);
            }
        }
	}
	
	/*解析各条连线 - 增强的DTO方法*/
	private void parseLinesFromDto(java.util.List<LineDto> lineDtos) {
		if (lineDtos == null || lineDtos.isEmpty()) {
			System.out.println("No lines to parse from DTO");
			return;
		}
		
		System.out.println("Parsing " + lineDtos.size() + " lines from DTO");
		
		for (LineDto lineDto : lineDtos) {
			try {
				// 验证DTO有效性
				if (!lineDto.isValid()) {
					System.err.println("Invalid line DTO: " + lineDto.getValidationError() + ", skipping");
					continue;
				}
				
				// 直接处理DTO连线 - 不再需要JSON转换
				// Process line directly from DTO
				processLineDto(lineDto);
				
				System.out.println("Successfully processed line: " + 
				                   lineDto.getFromBlockName() + " -> " + lineDto.getToBlockName());
				
			} catch (Exception e) {
				System.err.println("Error parsing line DTO: " + 
				                   lineDto.getFromBlockName() + " -> " + lineDto.getToBlockName() + 
				                   " - " + e.getMessage());
				// 继续处理其他连线，不中断整个解析过程
			}
		}
		
		System.out.println("Successfully parsed " + getLineList().size() + " lines from DTO");
	}
	
	/**
	 * 处理连线 JSON对象的通用逻辑
	 * 从parseLines()方法中提取出来的通用逻辑
	 */
	private void processLineDto(JSONObject lineJSON) {
		//xiazhiqiang:隐去子系统连线，并将输入连线链接到子系统的In，输出连线链接到子系统的Out
		replaceInLine(lineJSON);
		replaceOutLine(lineJSON);
		
		//解析各条连线
		Line line = Line.createLine(lineJSON, this.getBlockList());
		line.setLineId(lineSeq + 1);
		lineSeq++;
		
//		System.out.println("Parsing line ("+line.getLineId()+"): '"+line.getLinkedOutputPort().getBlock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")-->"+line.getLinkedInputPort().getBlock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")");
		
		getLineList().add(line);
	}

	/**
	 * Process line directly from DTO without JSON conversion
	 */
	private void processLineDto(LineDto lineDto) {
		try {

			//解析各条连线
			Line line = Line.createLine(lineDto, this.getBlockList());
			line.setLineId(lineSeq + 1);
			lineSeq++;
			rootSystem.addLine(line);
		} catch (Exception e) {
			System.err.println("Failed to process line DTO: " + e.getMessage());
		}
	}

	/**
	 * Create JSONObject saveInfo from DTO for compatibility
	 */
	private JSONObject createSaveInfoFromDto(SaveInfoDto saveInfoDto) {
		try {
			JSONObject json = new JSONObject();
			if (saveInfoDto.getModelRealName() != null) {
				json.put("modelRealName", saveInfoDto.getModelRealName());
			}
			json.put("copyNum", saveInfoDto.getCopyNum());
			json.put("modelId", saveInfoDto.getModelId());
			json.put("publicFlag", saveInfoDto.getPublicFlag());
			json.put("testRig", saveInfoDto.getTestRig());
			if (saveInfoDto.getDescription() != null) {
				json.put("description", saveInfoDto.getDescription());
			}
			json.put("packetSize", saveInfoDto.getPacketSize());
			json.put("stepTime", saveInfoDto.getStepTime());
			json.put("targetPlatform", saveInfoDto.getTargetPlatform());
			json.put("uuid", saveInfoDto.getUuid());
			json.put("userId", saveInfoDto.getUserId());
			return json;
		} catch (Exception e) {
			System.err.println("Failed to create saveInfo from DTO: " + e.getMessage());
			return new JSONObject();
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
		System.out.printf("RT Debug: Looking for terminal blocks in %d total blocks%n", getBlockList().size());
		int terminalCount = 0;
		int scopeCount = 0;
		for(Block block:getBlockList()) {
			if(block.getBlockType().equals("Scope")) {
				scopeCount++;
				System.out.printf("RT Debug: Found Scope block '%s' with %d outputs (isTerminal=%s)%n", 
					block.getBlockName(), block.getOutputPortList().size(), block.isTerminalBlock());
			}
			if(block.isTerminalBlock()) {
				terminalCount++;
				System.out.printf("RT Debug: Found terminal block '%s' (type=%s)%n", 
					block.getBlockName(), block.getBlockType());
				dimTerminalBlockList.add(block);
			}
		}
		System.out.printf("RT Debug: Total scope blocks found: %d, terminal blocks found: %d%n", scopeCount, terminalCount);
	}

	private void checkUnlinkedPorts() throws ModelException{

        // 向blockList中添加Block对象
        List<Block> list = new ArrayList<>(getBlockList());
        List<Block> fullBlockList = new ArrayList<>(list);

		// Iterate over a copy to avoid ConcurrentModificationException
		List<Block> originalBlocks = new ArrayList<>(getBlockList());
		for(Block block:originalBlocks) {
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
                    blockJSON.put("blockUUID", UUID.randomUUID().toString());

                    Block newBlock = BlockType.createBlock(blockSeq+1,blockJSON,this);

                    blockSeq++;

					System.out.println("Parsing block ("+newBlock.getBlockId()+"): '"+newBlock.getBlockName()+"'...");

                    fullBlockList.add(newBlock);
                    rootSystem.addBlock(newBlock); // Also add to model's getBlockList() for Line.createLine()

                    JSONObject lineJSON = new JSONObject();
                    lineJSON.put("fromBlockName", newBlock.getBlockName());
                    lineJSON.put("fromBlockUUID", newBlock.getBlockUUID());
                    lineJSON.put("fromPortNo", 1);

                    lineJSON.put("toBlockName", block.getBlockName());
                    lineJSON.put("toBlockUUID", block.getBlockUUID());
                    lineJSON.put("toPortNo", i+1);
                    lineJSON.put("linePath", block.getBlockPath());					

                    Line line=Line.createLine(lineJSON, getBlockList());
                    line.setLineId(lineSeq+1);
                    lineSeq++;

					OutputPort outputPort = line.getLinkedOutputPort();					

                    System.out.println("Parsing line ("+line.getLineId()+"): " + block.getBlockPath() + " '"
                        +outputPort.getBlock().getBlockPath()+"/"+ outputPort.getBlock().getBlockName()+"("+outputPort.getNumber()+")" +
                        "-->"+input.getBlock().getBlockPath() +"/"+ input.getBlock().getBlockName()+"("+input.getNumber()+")");

                    getLineList().add(line);
				}
			}
		}
		//空的output连接terminal
        for(Block block:originalBlocks){
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
                    blockJSON.put("blockUUID", UUID.randomUUID().toString());

                    Block newBlock = BlockType.createBlock(blockSeq+1, blockJSON, this);

                    blockSeq++;

                    System.out.println("Parsing block (" + newBlock.getBlockId() + "): '" + newBlock.getBlockName() + "'...");

                    fullBlockList.add(newBlock);
                    rootSystem.addBlock(newBlock); // Also add to model's getBlockList() for Line.createLine()

                    JSONObject lineJSON = new JSONObject();
                    lineJSON.put("fromBlockName", block.getBlockName());
                    lineJSON.put("fromBlockUUID", block.getBlockUUID());
                    lineJSON.put("fromPortNo", i + 1);

                    lineJSON.put("toBlockName", newBlock.getBlockName());
                    lineJSON.put("toBlockUUID", newBlock.getBlockUUID());
                    lineJSON.put("toPortNo", 1);
                    lineJSON.put("linePath", block.getBlockPath());

                    Line line = Line.createLine(lineJSON, getBlockList());
                    line.setLineId(lineSeq + 1);
                    lineSeq++;

                    System.out.println("Parsing line ("+line.getLineId()+"): '"
                        +line.getLinkedOutputPort().getBlock().getBlockPath()+"/"+ line.getLinkedOutputPort().getBlock().getBlockName()+"("+line.getLinkedOutputPort().getNumber()+")+" +
                        "-->"+line.getLinkedInputPort().getBlock().getBlockPath() +"/"+ line.getLinkedInputPort().getBlock().getBlockName()+"("+line.getLinkedInputPort().getNumber()+")");

                    rootSystem.addLine(line);
                }

            }
        }
        // FIXME:
		// if(getBlockList().size() != fullBlockList.size())
        //     getBlockList() = fullBlockList;
		
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
		Block block = outputPort.getBlock();

		boolean isDimThroughBlock = false;
		List<OutputPort> outputPortList = block.getOutputPortList();
		for (OutputPort output : outputPortList) {
            if (output.getDimThrough()) {
                isDimThroughBlock = true;
                break;
            }
		}

		//如果有Feedthrough的模块，则要遍历整个模块的InputPort
		if(isDimThroughBlock) {
			block.setIsDimScaned(true);
			List<InputPort> InputPortList = block.getInputPortList();
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
			List<InputPort> InputPortList = block.getInputPortList();

			//如果dimThrough是真的话，说明这是类似Sum和Add的模块，输入的Dimension必须相互配合
			//扫描第一个输入
			if(InputPortList.size()!=0) {
				scanDimInputPort(InputPortList.get(0));
			}

			if(InputPortList.size()>1) {
				for(int i=1;i<InputPortList.size();i++) {
					InputPort input=InputPortList.get(i);
					Block linkedBlock=input.getLinkedLine().getLinkedOutputPort().getBlock();
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
//		System.out.println("scaning outputChain");

		//遍历所有的终端模块
		for(Block block:dimTerminalBlockList) {
			block.setIsDimScaned(true);
			List<InputPort> inputPortList = block.getInputPortList();
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
			List<InputPort> inputPortList = block.getInputPortList();
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
//			System.out.println("("+i+")"+block.getBlockName()+"("+block.getBlockId()+")");
			i++;
		}
	}

	private void setupDimensionList() {
		System.out.println("RT Debug: Setting up dimension list...");
		findDimTerminalBlocks();
		scanDimChain();
		System.out.printf("RT Debug: Dimension list setup complete - %d blocks in dimensionList%n", dimensionList.size());
		showDimBlocks();
	}

    //xiazhiqiang:检查前端模块是否存在命名相同的情况
	private void checkBlocksName() throws MatDimException{
		for(Block block:getBlockList()) {
			int i=0;
			String name=block.getBlockName();
			for(Block block1:getBlockList()) {
				if(name.equals(block1.getBlockName())) {
					i=i+1;
				}
			}
			if(i>1) {
                throw(new MatDimException(block.getBlockName()+" Name is not unique!\n \n"));
			}
		}
	}

    private void checkBlocksPath() throws MatDimException{
        for(Block block:getBlockList()) {
            int i=0;
            String path=block.getBlockPath();
            for(Block block1:getBlockList()) {
                if(path.equals(block1.getBlockPath())) {
                    i=i+1;
                }
            }
            if(i>1) {
                throw(new MatDimException(block.getBlockPath()+" Path is not unique!\n \n"));
            }
        }
    }

    private void checkBlocksCId() throws MatDimException{
        if(!getBlockList().isEmpty()){
            cidModeEnable = !"null".equals(getBlockList().get(0).getBlockUUID());
        }
    }

    //xiazhiqiang:隐去Subsystem的输入连线，同时将该连线的输出连接到子系统中的In
    private void replaceInLineBack(JSONObject lineJSON) {
        String toBlockName=lineJSON.getString("toBlockName");
        String blockPath=null;
        for(Block block:getBlockList()) {
            if(block.getBlockName().equals(toBlockName)&&block.getBlockType().equals("Subsystem")) {
                blockPath=block.getBlockPath()+"/"+toBlockName;
                break;
            }
        }
        for(Block block1:getBlockList()) {
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
        String toBlockUUID = lineJSON.optString("toBlockUUID","null");
        String blockPath=null;
        String toPortNo=lineJSON.getString("toPortNo");
        for(Block block:getBlockList()) {
            if( (!cidModeEnable && block.getBlockName().equals(toBlockName))
                || (cidModeEnable && block.getBlockUUID().equals(toBlockUUID)) )
                if(block instanceof Subsystem) {
                blockPath=block.getBlockPath()+"/"+toBlockName;
                break;
            }
        }
        if(blockPath == null){
            return;
        }
        for(In block1:inBlockList) {

//            System.out.println("block1's getBlockPath: "+block1.getBlockPath()+
//                "  getBlockType: "+block1.getBlockType());
            if(block1.getBlockPath().equals(blockPath)
			&& block1.getPort().getInitString().equals(toPortNo)
			){
                lineJSON.put("toBlockName", block1.getBlockName());
                lineJSON.put("toBlockUUID", block1.getBlockUUID());
                lineJSON.put("toPortNo", 1);
                break;
            }
        }
    }
    //xiazhiqiang:隐去Subsystem的输出连线，同时将该连线的输入连接到子系统中的out
    private void replaceOutLineBack(JSONObject lineJSON) {
        String fromBlockName=lineJSON.getString("fromBlockName");
        String blockPath=null;
        for(Block block:getBlockList()) {
            if(block.getBlockName().equals(fromBlockName)&&block.getBlockType().equals("Subsystem")) {
                blockPath=block.getBlockPath()+"/"+fromBlockName;
                break;
            }
        }
        for(Block block1:getBlockList()) {
            if(block1.getBlockPath().equals(blockPath)&&block1.getBlockType().equals("Out")){
                lineJSON.put("fromBlockName", block1.getBlockName());
                break;
            }
        }
    }

    private void replaceOutLine(JSONObject lineJSON) {
        String fromBlockName=lineJSON.getString("fromBlockName");
        String fromBlockUUID=lineJSON.optString("fromBlockUUID","null");
        String blockPath=null;
        String fromPortNo=lineJSON.getString("fromPortNo");
        for(Block block:getBlockList()) {
            if( (cidModeEnable && block.getBlockUUID().equals(fromBlockUUID))
             || (!cidModeEnable && block.getBlockName().equals(fromBlockName)) ){
                if(block instanceof Subsystem) {
                    blockPath = block.getBlockPath() + "/" + fromBlockName;
                    break;
                }
            }
        }
        if(blockPath == null){
            return;
        }
        for(Out block1:outBlockList) {
//            System.out.println("block1's getBlockPath: "+block1.getBlockPath()+
//                "  getBlockType: "+block1.getBlockType());
            if(block1.getBlockPath().equals(blockPath)
			&& block1.getPort().getInitString().equals(fromPortNo)
			){
                lineJSON.put("fromBlockName", block1.getBlockName());
                lineJSON.put("fromBlockUUID", block1.getBlockUUID());
                lineJSON.put("fromPortNo", 1);
                break;
            }
        }
    }

    private void replaceLogicLines() throws ModelException {

        // 1. 替代法
        List<Line> lines = new ArrayList<>();

        for (Line line1 : getLineList()) {
            if (line1.getLinkedOutputPort().getBlock() instanceof From) {
                From from = (From) line1.getLinkedOutputPort().getBlock();
                boolean not_found = true;
                for (To to : gotoBlockList) {
                    if (Objects.equals(to.getTagName(), from.getTagName())) {
                        for (Line line2 : getLineList()) {
                            if (line2.getLinkedInputPort().getBlock() == to) {
                                JSONObject lineJSON = new JSONObject();
                                lineJSON.put("toBlockName", line1.getLinkedInputPort().getBlock().getBlockName());
                                lineJSON.put("toBlockUUID", line1.getLinkedInputPort().getBlock().getBlockUUID());
                                lineJSON.put("toPortNo", line1.getLinkedInputPort().getNumber());
                                lineJSON.put("fromBlockName", line2.getLinkedOutputPort().getBlock().getBlockName());
                                lineJSON.put("fromBlockUUID", line2.getLinkedOutputPort().getBlock().getBlockUUID());
                                lineJSON.put("fromPortNo", line2.getLinkedOutputPort().getNumber());
                                lineJSON.put("linePath", to.getBlockPath());
                                Line line = Line.createLine(lineJSON, getBlockList());
                                lines.add(line);
                                not_found = false;
                                break;
                            }
                        }
                    }
                    if (!not_found) {
                        break;
                    }
                }
                if (not_found) {
                    throw new ModelException("Goto for " + from.getTagName() + " not found");
                }
            } else {
                lines.add(line1);
            }
        }
        //getLineList() = lines;
    }

    private void addLogicLines() throws ModelException {
        // 2.增加虚拟连线法
        for(From from: fromBlockList) {
            boolean not_found = true;
            for(To to: gotoBlockList) {
                if(Objects.equals(to.getTagName(), from.getTagName())){
                    JSONObject lineJSON = new JSONObject();
                    lineJSON.put("toBlockName", from.getBlockName());
                    lineJSON.put("toPortNo", 1);
                    lineJSON.put("toBlockUUID", from.getBlockUUID());
                    lineJSON.put("fromBlockName", to.getBlockName());
                    lineJSON.put("fromPortNo", 1);
                    lineJSON.put("fromBlockUUID", to.getBlockUUID());

                    lineJSON.put("linePath", to.getBlockPath());
                    Line line = Line.createLine(lineJSON, getBlockList());
                    getLineList().add(line);
                    not_found = false;
                    break;
                }
            }
            if(not_found){
                throw new ModelException("Goto for " + from.getTagName() + " not found");
            }
        }
    }


	public List<Block> getBlockList() {
		List<Block> blockList = new ArrayList<>();
		blockList.addAll(rootSystem.getBlocks());
		for(Subsystem subsystem:subsystemBlockList){
			blockList.addAll(subsystem.getContainedBlocks());
		}
		return blockList;
	}

    public List<Line> getLineList() {
		List<Line> lineList = new ArrayList<>();
		lineList.addAll(rootSystem.getLines());
		for(Subsystem subsystem:subsystemBlockList){
			lineList.addAll(subsystem.getContainedLines());
		}
        return lineList;
    }

	public Integer assignNextBlockSequence() {
		blockSeq++;
		return blockSeq;
	}

    public Integer assignNextLineSequence() {
        lineSeq++;
        return lineSeq;
    }
    
}

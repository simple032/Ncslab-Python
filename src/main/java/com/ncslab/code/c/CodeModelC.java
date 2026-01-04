package com.ncslab.code.c;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import jakarta.websocket.Session;

import com.ncslab.block.data.Data;
import com.ncslab.code.m.MfcalcClient;
import com.ncslab.code.m.MfcalcClientManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import com.ncslab.dto.core.ModelDto;

import com.utils.AlgorithmsMapper;
import com.utils.Mybatis1Utils;

import com.ncslab.block.Block;
import com.ncslab.code.CodeGenerationOption;
import com.ncslab.code.CodeModel;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.database.Algorithms;
import com.utils.Property;

@Slf4j
abstract public class CodeModelC extends CodeModel {

	private static final String REAL = "real_t";

	protected String user = Optional.ofNullable(Property.instance.getProperty("user")).orElse("m2plab");
	protected String group = Optional.ofNullable(Property.instance.getProperty("group")).orElse("m2plab");

	abstract protected CodeStructC getCodeStructC();

	// 原有JSONObject构造函数
	protected CodeModelC(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	// 新增ModelDto DTO构造函数
	protected CodeModelC(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}
	
	protected void generatorCircuitOutputCode(CodeGenerationOption option) {
		getCodeStructC().generatorCircuitOutputCode();
	}
	
	protected void generatorCircuitGloablCode(CodeGenerationOption option) {
		getCodeStructC().generatorCircuitInitCode();
	}
	
	protected void generatorCircuitUpdateCode(CodeGenerationOption option) {
		getCodeStructC().generatorCircuitUpdateCode();
	}

	public String getIpAddress() {
		// 安卓客户端发过来的为空
		JSONObject saveInfo = getSaveInfo();
		if(saveInfo != null && saveInfo.has("ipAddress"))
			return saveInfo.getString("ipAddress");
		else return "192.168.46.34";
	}
	public String getNetmask() {
		JSONObject saveInfo = getSaveInfo();
		if(saveInfo != null && saveInfo.has("netmask"))
			return saveInfo.getString("netmask");
		else return "255.255.255.0";
	}
	public String getGateway() {
//		return getSaveInfo().getString("gateway");
		JSONObject saveInfo = getSaveInfo();
		if(saveInfo != null && saveInfo.has("gateway"))
			return saveInfo.getString("gateway");
		else return "192.168.46.1";
	}
	public int getMonitorPort() {
//		return getSaveInfo().getInt("monitorPort");
		JSONObject saveInfo = getSaveInfo();
		if(saveInfo != null && saveInfo.has("monitorPort"))
			return saveInfo.getInt("monitorPort");
		else return 27015;
	}

	@Override
	public void generate()  {
		super.generate();
		writeCCodeFiles();
	}

	// genetate C++ code files
//	public void generate(String Platform)  {
//		super.generate();
//		writeCCodeFiles(Platform);
//	}

	/*将代码变成C语言的一系列文件 */
	private void writeCCodeFiles() {
		getCodeStructC().writeCCodeFiles();
	}

    public void removeAllFiles() { getCodeStructC().removeAllFiles(); }

	@Override
//	/*将代码变成C语言的一系列文件 */
//	private void writeCCodeFiles(String Platform) {
//		getCodeStructC().writeCCodeFiles(Platform);
//	}

	protected void generateInitCode(CodeGenerationOption option) {
//		System.out.println("Generating init codes......");
		for(Block block:getBlockList()) {
			// log.debug("Generating init codes for ("+block.getBlockId()+")"+block.getBlockName());

			block.generateBlockInitCodeC(getCodeStructC());
		}

		getCodeStructC().generateIncludeCode();
		//code.writeCCodeFiles();
		if(this.getModelMode()==ModelMode.Compilation) {
			getCodeStructC().generateHardwareDefineCode();
		}

		getCodeStructC().generateParameterDefineCode();
		getCodeStructC().generateStateDefineCode();
		getCodeStructC().generateOutputSignalDefineCode();
		getCodeStructC().gnenrateDataStructureCode();
		getCodeStructC().generateGlobalVariableDefineCode();
		
		if(this.getCircuitModel()!=null) {
			getCodeStructC().generateCircuitDefineCode();
		}
	}

	@Override
	protected void generateBlockOutputCode(Block block,CodeGenerationOption option) {
		block.generateBlockOutputCodeC(getCodeStructC());
	}

	@Override
	protected void generateBlockSinkOutputCode(Block block,CodeGenerationOption option) {
		block.generateBlockSinkOutputCodeC(getCodeStructC());
	}

	//generate arrays code for discrete blocks
	//author:xiazhiqiang
	protected void generateBlockArraysCode(Block block) {
		block.generateBlockArraysCodeC(getCodeStructC());
	}

	@Override
	protected void generateArraysCode(CodeGenerationOption option) {
//		System.out.println("Generating arrays codes......");

		for(Block block:getBlockList()) {
//			System.out.println("Generating arrays codes for ("+block.getBlockId()+")"+block.getBlockName());

			generateBlockArraysCode(block);
		}
	}
//end

	protected void generateBlockUpdateCode(Block block) throws MatDimException {
		block.generateBlockUpdateCodeC(getCodeStructC());
	}

	@Override
	protected void generateUpdateCode(CodeGenerationOption option) throws MatDimException {
//		System.out.println("Generating update codes......");

		for(Block block:getBlockList()) {
//			System.out.println("Generating update codes for ("+block.getBlockId()+")"+block.getBlockName());

			generateBlockUpdateCode(block);
		}
	}

	protected void generateDiscreteUpdateCode(CodeGenerationOption option) throws MatDimException {
//		System.out.println("Generating discrete update codes......");

		for(Block block:getBlockList()) {
//			System.out.println("Generating discrete update codes for ("+block.getBlockId()+")"+block.getBlockName());

			generateDiscreteBlockUpdateCode(block);
		}
	}

	protected void generateDiscreteBlockUpdateCode(Block block) throws MatDimException {
		block.generateDiscreteBlockUpdateCodeC(getCodeStructC());
	}

	protected void generateBlockTerminateCode(Block block) {
		block.generateBlockTerminateCodeC(getCodeStructC());
	}

	@Override
	protected void generateTerminateCode(CodeGenerationOption option) {
//		System.out.println("Generating terminate codes......");

		for(Block block:getBlockList()) {
			generateBlockTerminateCode(block);
		}
	}

	protected void generateBlockStatementCode(Block block) {
		block.generateBlockStatementCodeC(getCodeStructC());
	}

	@Override
	protected void generateStatementCode(CodeGenerationOption option) {
//		System.out.println("Generating statement codes......");

		for(Block block:getBlockList()) {

//			System.out.println("Generating statement codes for ("+block.getBlockId()+")"+block.getBlockName());

			generateBlockStatementCode(block);

		}
	}

	/**
	 * use make to generate executable file
	 *
	 * @param
	 * @return whether the executable file is generated successfully
	 */
	public boolean makeExeFile() {
		System.out.println("Making exe file ncslab...");
		boolean flag = getCodeStructC().makeExeFile();
		if(flag){
			System.out.println("Exe file ncslab created!");
		}
		else {
			System.out.println("Cannot create exe file ncslab!");
		}
		return flag;
	}

	@Override
	protected void generateDerivativeCode(CodeGenerationOption option) {
//		System.out.println("Generating derivative codes......");

		for(Block block:getBlockList()) {
//			System.out.println("Generating derivative codes for ("+block.getBlockId()+")"+block.getBlockName());

			block.generateBlockDerivativeCodeC(getCodeStructC());
		}
	}

	public void saveToDatabase() {
		//final EntityManagerFactory emf = Persistence.createEntityManagerFactory("piscesPU");
        //final EntityManager em = emf.createEntityManager();
        SqlSession  sqlSession = Mybatis1Utils.getSqlSession();
        AlgorithmsMapper mapper = sqlSession.getMapper( AlgorithmsMapper.class);
        Algorithms algorithm = new Algorithms();

		JSONObject saveInfo = getSaveInfo();
		if(saveInfo == null){
			saveInfo = new JSONObject();
			saveInfo.put("userId", getUserId());
			saveInfo.put("modelId", getModelId());
			saveInfo.put("modelRealName", getModelName());
			saveInfo.put("publicFlag", 0);
			saveInfo.put("stepTime", 0.04);
			saveInfo.put("packetSize", 10);
			saveInfo.put("testRig", 2);
			saveInfo.put("uuid", System.currentTimeMillis());
			saveInfo.put("targetPlatform", 1);
		}

        algorithm.setAuthor(saveInfo.getInt("userId"));
        algorithm.setName(saveInfo.getString("modelRealName"));
        
        algorithm.setTestRig(saveInfo.getInt("testRig"));
        algorithm.setModelId(saveInfo.getInt("modelId"));
        
        algorithm.setStepTime((float) saveInfo.getDouble("stepTime"));
        algorithm.setPacketSize(saveInfo.getInt("packetSize"));

        algorithm.setUuid(saveInfo.getLong("uuid"));
        algorithm.setPublicFlag(saveInfo.getInt("publicFlag"));
        algorithm.setTargetPlatform(saveInfo.getInt("targetPlatform"));
		
		algorithm.setBin(getCodeStructC().readExeFile());
		algorithm.setLastUpdate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));

        if(saveInfo.has("ipAddress")&&saveInfo.has("monitorPort")) {
        	algorithm.setDescription(saveInfo.getString("ipAddress"),saveInfo.getString("monitorPort"));
        }

		mapper.insert(algorithm);
        sqlSession.commit();
        sqlSession.close();
	}


    public void simulate(Session session) throws ModelException{

    }

	public void preBuild(){        
        //1.清除工作
        cleanup();
    }

    public void postBuild(){
        //TODO:
        //1.发送最终结果，将仿真结果发送给M2PCode

        //2.清除工作
        // cleanup();
    }

    private void cleanup(){
        //1.批量删除临时变量
        MfcalcClient client = MfcalcClientManager.getClientForUser("18");
        StringBuilder command = new StringBuilder("clear");
        if(client!=null && !Data.getTemp_variable_names().isEmpty()) {
            for(String temp_variable_name:Data.getTemp_variable_names()) {
                command.append(" ").append(temp_variable_name);
            }
            client.runCommand(command+"\n");
            Data.getTemp_variable_names().clear();
        }

        //2.删除临时文件
        if(!"true".equals(Optional.ofNullable(Property.instance.getProperty("debug")).orElse("false"))){
            System.out.println("Cleaning up temporary files...");
			removeAllFiles();
        }
    }

	/**
	 * Get the parameter list from CodeStructC.
	 * TODO: This is a stub method added for test compatibility.
	 * Returns an empty list as parameters are managed internally.
	 * @return An empty list
	 */
	public java.util.List<com.ncslab.block.io.Parameter> getParameterList() {
		return new java.util.ArrayList<>();
	}
}

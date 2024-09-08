package com.ncslab.code.c;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;

import com.ncslab.utils.AlgorithmsMapper;
import com.ncslab.utils.Mybatis1Utils;

import com.ncslab.block.Block;
import com.ncslab.code.CodeGenerationOption;
import com.ncslab.code.CodeModel;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.database.Algorithms;

abstract public class CodeModelC extends CodeModel {

	private static final String REAL = "real_t";

	//生成代码的时候统计signal和parameter的个数
	@Setter
	@Getter
    private int signalNum=0;

	@Setter
	@Getter
    private int parameterNum=0;

	@Getter
	private int stateNum=0;

	@Getter
	private int singleStateNum=0;

	@Getter
	private int matrixStateNum=0;

	abstract protected CodeStructC getCodeStructC();

	protected CodeModelC(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	public void setStateNum(int singleStateNum,int matrixStateNum) {
		this.singleStateNum=singleStateNum;
		this.matrixStateNum=matrixStateNum;
		this.stateNum=singleStateNum+matrixStateNum;
	}

	public String getIpAddress() {
		if(getSaveInfo().has("ipAddress"))
			return getSaveInfo().getString("ipAddress");
		else return "192.168.46.34";
	}
	public String getNetmask() {
		if(getSaveInfo().has("netmask"))
			return getSaveInfo().getString("netmask");
		else return "255.255.255.0";
	}
	public String getGateway() {
//		return getSaveInfo().getString("gateway");
		if(getSaveInfo().has("gateway"))
			return getSaveInfo().getString("gateway");
		else return "192.168.46.1";
	}
	public int getMonitorPort() {
//		return getSaveInfo().getInt("monitorPort");
		if(getSaveInfo().has("monitorPort"))
			return getSaveInfo().getInt("monitorPort");
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

	@Override
//	/*将代码变成C语言的一系列文件 */
//	private void writeCCodeFiles(String Platform) {
//		getCodeStructC().writeCCodeFiles(Platform);
//	}

	protected void generateInitCode(CodeGenerationOption option) {
		System.out.println("Generating init codes......");
		for(Block block:blockList) {
			System.out.println("Generating init codes for ("+block.getBlockId()+")"+block.getBlockName());

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
		System.out.println("Generating arrays codes......");

		for(Block block:blockList) {
			System.out.println("Generating arrays codes for ("+block.getBlockId()+")"+block.getBlockName());

			generateBlockArraysCode(block);
		}
	}
//end

	protected void generateBlockUpdateCode(Block block) throws MatDimException {
		block.generateBlockUpdateCodeC(getCodeStructC());
	}

	@Override
	protected void generateUpdateCode(CodeGenerationOption option) throws MatDimException {
		System.out.println("Generating update codes......");

		for(Block block:blockList) {
			System.out.println("Generating update codes for ("+block.getBlockId()+")"+block.getBlockName());

			generateBlockUpdateCode(block);
		}
	}

	protected void generateDiscreteUpdateCode(CodeGenerationOption option) throws MatDimException {
		System.out.println("Generating discrete update codes......");

		for(Block block:blockList) {
			System.out.println("Generating discrete update codes for ("+block.getBlockId()+")"+block.getBlockName());

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
		System.out.println("Generating terminate codes......");

		for(Block block:blockList) {
			generateBlockTerminateCode(block);
		}
	}

	protected void generateBlockStatementCode(Block block) {
		block.generateBlockStatementCodeC(getCodeStructC());
	}

	@Override
	protected void generateStatementCode(CodeGenerationOption option) {
		System.out.println("Generating statement codes......");

		for(Block block:blockList) {

			System.out.println("Generating statement codes for ("+block.getBlockId()+")"+block.getBlockName());

			generateBlockStatementCode(block);

		}
	}

	/**
	 * use make to generate executable file
	 *
	 * @param void
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
		System.out.println("Generating derivative codes......");

		for(Block block:blockList) {
			System.out.println("Generating derivative codes for ("+block.getBlockId()+")"+block.getBlockName());

			block.generateBlockDerivativeCodeC(getCodeStructC());
		}
	}

	public void saveToDatabase() {
		//final EntityManagerFactory emf = Persistence.createEntityManagerFactory("piscesPU");
        //final EntityManager em = emf.createEntityManager();
        SqlSession  sqlSession = Mybatis1Utils.getSqlSession();
        AlgorithmsMapper mapper = sqlSession.getMapper( AlgorithmsMapper.class);
        Algorithms algorithm = new Algorithms();

        algorithm.setAuthor(getSaveInfo().getInt("userId"));
        algorithm.setName(getSaveInfo().getString("modelRealName"));
        algorithm.setBin(getCodeStructC().readExeFile());

        algorithm.setTestRig(getSaveInfo().getInt("testRig"));
        algorithm.setModelId(getSaveInfo().getInt("modelId"));
        algorithm.setLastUpdate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));

        algorithm.setStepTime(new Float(getSaveInfo().getDouble("stepTime")));
        algorithm.setPacketSize(getSaveInfo().getInt("packetSize"));

        algorithm.setUuid(getSaveInfo().getLong("uuid"));
        algorithm.setPublicFlag(getSaveInfo().getInt("publicFlag"));
        algorithm.setTargetPlatform(getSaveInfo().getInt("targetPlatform"));
        mapper.insert(algorithm);
        sqlSession.commit();
        sqlSession.close();

        if(getSaveInfo().has("ipAddress")&&getSaveInfo().has("monitorPort")) {
        	algorithm.setDescription(getSaveInfo().getString("ipAddress"),getSaveInfo().getString("monitorPort"));
        }
	}
}

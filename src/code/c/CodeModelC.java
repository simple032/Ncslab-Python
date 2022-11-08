package code.c;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;

import com.mybatis1.utils.AlgorithmsMapper;
import com.mybatis1.utils.Mybatis1Utils;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.CodeGenerationOption;
import code.CodeModel;
import line.Line;
import ncslablink.ErrorMessage;
import ncslablink.MatDimException;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import main.database.Algorithms;

abstract public class CodeModelC extends CodeModel {
	
	private static final String REAL="real_t";
	
		
	//生成代码的时候统计singal和parameter的个数
	private int signalNum=0;
	private int parameterNum=0;
	private int stateNum=0;
	private int singleStateNum=0;
	private int matrixStateNum=0;
	
	abstract protected CodeStructC getCodeStructC();
	
	protected CodeModelC(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	public void setSignalNum(int signalNum) {
		this.signalNum=signalNum;
	}
	
	public int getSignalNum() {
		return this.signalNum;
	}
	
	public void setParameterNum(int parameterNum) {
		this.parameterNum=parameterNum;
	}
	
	public int getParameterNum() {
		return this.parameterNum;
	}
	
	public void setStateNum(int singleStateNum,int matrixStateNum) {
		this.singleStateNum=singleStateNum;
		this.matrixStateNum=matrixStateNum;
		this.stateNum=singleStateNum+matrixStateNum;
	}
	
	public int getSingleStateNum() {
		return this.singleStateNum;
	}
	
	public int getMatrixStateNum() {
		return this.matrixStateNum;
	}
	
	public int getStateNum() {
		return this.stateNum;
	}
	
	public void generate()  {
		super.generate();
		writeCCodeFiles(); 
	}
	
	/*将代码变成C语言的一系列文件 */
	private void writeCCodeFiles() {
		getCodeStructC().writeCCodeFiles();
	}
	
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
	}
	
	protected void generateBlockOutputCode(Block block,CodeGenerationOption option) {
		block.generateBlockOutputCodeC(getCodeStructC());
	}
	
	//generate arrays code for discrete blocks
	//author:xiazhiqiang
	protected void generateBlockArraysCode(Block block) {
		block.generateBlockArraysCodeC(getCodeStructC());
	}
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
	
	protected void generateUpdateCode(CodeGenerationOption option) throws MatDimException {
		System.out.println("Generating update codes......");
		
		for(Block block:blockList) {
			System.out.println("Generating update codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			generateBlockUpdateCode(block);
		}
	}
	
	protected void generateDiscreteBlockUpdateCode(Block block) throws MatDimException {
		block.generateDiscreteBlockUpdateCodeC(getCodeStructC());
	}
	
	protected void generateDiscreteUpdateCode(CodeGenerationOption option) throws MatDimException {
		System.out.println("Generating discrete update codes......");
		
		for(Block block:blockList) {
			System.out.println("Generating discrete update codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			generateDiscreteBlockUpdateCode(block);
		}
	}
	
	protected void generateBlockTerminateCode(Block block) {
		block.generateBlockTerminateCodeC(getCodeStructC());
	}
	
	protected void generateTerminateCode(CodeGenerationOption option) {
		System.out.println("Generating terminate codes......");
		
		for(Block block:blockList) {
			generateBlockTerminateCode(block);
		}
	}
	
	protected void generateBlockStatementCode(Block block) {
		block.generateBlockStatementCodeC(getCodeStructC());
	}
	
	protected void generateStatementCode(CodeGenerationOption option) {
		System.out.println("Generating statement codes......");
		
		for(Block block:blockList) {
			
			System.out.println("Generating statement codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			generateBlockStatementCode(block);
			
		}
	}
	
	/*调用make命令，生成可执行代码 */
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
       // try {
         //   em.getTransaction().begin();
           // em.persist(algorithm);
         //   em.getTransaction().commit();
       // } finally {
         //   em.close();
        //}
	}
}

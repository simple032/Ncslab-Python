package code.c;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.CodeGenerationOption;
import code.CodeModel;
import line.Line;
import ncslablink.ErrorMessage;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import main.database.Algorithms;

public class CodeModelC extends CodeModel {
	
	private static final String REAL="real_t";
	
	private CodeStructC code=new CodeStructC(this);
	
	//生成代码的时候统计singal和parameter的个数
	private int signalNum=0;
	private int parameterNum=0;
	private int stateNum=0;
	
	CodeModelC(JSONObject jsonIn,ModelMode mode) throws ModelException{
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
	
	public void setStateNum(int stateNum) {
		this.stateNum=stateNum;
	}
	
	public int getStateNum() {
		return this.stateNum;
	}
	
	public static CodeModelC createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelC model=new CodeModelC(jsonIn,mode);
		
		return model;
	}
	
	public void generate() {
		super.generate();
		writeCCodeFiles(); 
	}
	
	/*将代码变成C语言的一系列文件 */
	private void writeCCodeFiles() {
		code.writeCCodeFiles();
	}
	
	protected void generateInitCode(CodeGenerationOption option) {
		System.out.println("Generating init codes......");
		for(Block block:blockList) {
			System.out.println("Generating init codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			block.generateBlockInitCodeC(code);
		}
		
		code.generateIncludeCode();
		//code.writeCCodeFiles();
		code.generateParameterDefineCode(); 
		code.generateStateDefineCode();
		code.generateOutputSignalDefineCode(); 
		code.gnenrateDataStructureCode();
	}
	
	protected void generateBlockOutputCode(Block block,CodeGenerationOption option) {
		block.generateBlockOutputCodeC(code);
	}
	
	protected void generateBlockUpdateCode(Block block) {
		block.generateBlockUpdateCodeC(code);
	}
	
	protected void generateUpdateCode(CodeGenerationOption option) {
		System.out.println("Generating update codes......");
		
		for(Block block:blockList) {
			System.out.println("Generating update codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			generateBlockUpdateCode(block);
		}
	}
	
	/*调用make命令，生成可执行代码 */
	public void makeExeFile() {
		System.out.println("Making exe file ncslabccode.exe...");
		if(code.makeExeFile()) {
			System.out.println("Exe file ncslabccode.exe created!");
		}
		else {
			System.out.println("Cannot create exe file ncslabccode.exe!");
		}
	}
	
	protected void generateDerivativeCode(CodeGenerationOption option) {
		System.out.println("Generating derivative codes......");
		
		for(Block block:blockList) {
			System.out.println("Generating derivative codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			block.generateBlockDerivativeCodeC(code);
		}
	}
	
	public void saveToDatabase() {
		final EntityManagerFactory emf = Persistence.createEntityManagerFactory("piscesPU");
        final EntityManager em = emf.createEntityManager();
 
        Algorithms algorithm = new Algorithms();
        
        algorithm.setAuthor(getSaveInfo().getInt("userId"));
        algorithm.setName(getSaveInfo().getString("modelRealName"));
        algorithm.setBin(code.readExeFile());
        
        algorithm.setTestRig(getSaveInfo().getInt("testRig"));
        algorithm.setModelId(getSaveInfo().getInt("modelId"));
        algorithm.setLastUpdate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        
        algorithm.setStepTime(new Float(getSaveInfo().getDouble("stepTime")));
        algorithm.setPacketSize(getSaveInfo().getInt("packetSize"));
        
        algorithm.setUuid(getSaveInfo().getLong("uuid"));
        algorithm.setPublicFlag(getSaveInfo().getInt("publicFlag"));
        algorithm.setTargetPlatform(getSaveInfo().getInt("targetPlatform"));
 
        try {
            em.getTransaction().begin();
            em.persist(algorithm);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
	}
}

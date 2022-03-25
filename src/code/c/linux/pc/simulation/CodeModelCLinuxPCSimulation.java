package code.c.linux.pc.simulation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.io.File;
import java.io.IOException;

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
import code.c.CodeModelC;
import code.c.CodeStructC;

public class CodeModelCLinuxPCSimulation extends CodeModelC{
	
	private CodeStructCLinuxPCSimulation codeRaspberry=new CodeStructCLinuxPCSimulation(this);
	
	CodeModelCLinuxPCSimulation(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCLinuxPCSimulation createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxPCSimulation model=new CodeModelCLinuxPCSimulation(jsonIn,mode);
		
		return model;
	}
	
	public void simulate() throws ModelException{
		Process process=null;
		System.out.println("Executing simulation codes...");
		try {
			process=Runtime.getRuntime().exec("./ncslab "+this.getConfig().getStopTime(),null, new File(codeRaspberry.getCodePath()));
			try {
				process.waitFor();
			}
			catch(InterruptedException e) {
				throw new ModelException("Can not execute the exe file!");
			}
		}
		catch(IOException e) {
			throw new ModelException("Can not execute the exe file!");
		}
		finally {
			if(process!=null) {
				process.destroy();
			}
		}
		
		System.out.println("Simulation codes executed successfully!");
		
	}
}

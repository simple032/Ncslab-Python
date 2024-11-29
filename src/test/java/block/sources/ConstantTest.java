package block.sources;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.code.c.windows.simulation.CodeModelCWindowsSimulation;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;

import utils.ResourceReader;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Objects;


public class ConstantTest {

	@Test
	public void testConstantBlock(){
		String filePath = "constantTest.json"; // 替换为实际文件路径
		JSONObject msg = ResourceReader.readJsonResource(filePath);
		
		String result = "";
		try {
			JSONObject  mdlData=msg.getJSONObject("mdlData");
            String target = mdlData.getString("target");
            String jsonDataString=mdlData.getString("jsonData");
			JSONObject jsonData=new JSONObject(jsonDataString);
			String errorMsgs="";

            CodeModelC modelC = null;
            if(Objects.equals(target, "linux")){
                modelC = CodeModelCLinuxPCSimulation.createFromJSON(jsonData, ModelMode.Simulation);
            }else if(Objects.equals(target, "linux-rpi")){
                modelC= CodeModelCLinuxPCSimulation.createFromJSON(jsonData,ModelMode.Simulation);
            }else{
				modelC= CodeModelCWindowsSimulation.createFromJSON(jsonData,ModelMode.Simulation);
			}
            
        	modelC.generate();

        	if(!modelC.getErrorList().isEmpty()) {
        		for(ErrorMessage em: modelC.getErrorList()) {
        			errorMsgs += em.getMessage();
        		}
        		throw new ModelException(errorMsgs);
        	}

        	if(!modelC.makeExeFile()) {
        		throw new ModelException("Can not make exe file!");
        	}
			modelC.removeAllFiles();
        	//modelC.simulate(session);

        }
        catch(ModelException e) {
        	System.err.println(e.getMessage());
        	System.err.println("Code generatrion terminated unsuccessfully");
        }
		catch(Exception e) {
			e.printStackTrace();
		}
	}

    
}



package block.function;


import com.ncslab.code.c.SFcnCompileModelC;
import com.ncslab.ncslablink.SFcnException;
import com.ncslab.websocket.SFcnCompileWebSocket;
import utils.ResourceReader;

//
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
//
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


public class SFunctionTest {

	@Test
	public void testCompileWithJSONWindows(){
		String filePath = "sfcnCompile.json"; // 替换为实际文件路径
		JSONObject msg = ResourceReader.readJsonResource(filePath);
		
		String result = "";
		try {
			
			JSONObject jsonData=msg.getJSONObject("jsonData");
			SFcnCompileModelC modelSFcn=new SFcnCompileModelC(jsonData);
			modelSFcn.writeSFcnFile();
			if(modelSFcn.makeExeFile()==false) {
				throw new SFcnException("Can not make exe file!");
			};
			modelSFcn.compile();
			result = modelSFcn.checkDimentions();
			if(result.startsWith("result")) {
				result="success";
			}
		}
		catch (SFcnException e) {
			System.err.println(e.getMessage());
			System.err.println("Code generatrion terminated unsuccessfully������");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals("success", result);
	}

    
}



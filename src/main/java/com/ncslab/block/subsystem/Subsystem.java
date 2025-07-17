package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import org.json.JSONObject;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Subsystem extends Block{

    Vector<In> inBlockList;
    Vector<Out> outBlockList;
    public Subsystem(JSONObject blockJSON,NCSLabModel model) {

        super(blockJSON,model);
		inBlockList=new Vector<In>();
		outBlockList=new Vector<Out>();
	}
    public void generateOutputCodeC(CodeStructC code) {

	}
	  public void updateDimension() throws MatDimException{
		}
	public void checkDimension() throws MatDimException{
	}

    public void addIn(In in){
//        inBlockList.add(in);
//        int no = Integer.parseInt(in.getNo().getDataString());
//        if(inputPortList.size() <= no){
//            for(int i=inputPortList.size();i<no;i++){
//                if(i<(no-1)){
//                    inputPortList.add(null);
//                }else{
//                    inputPortList.add(new InputPort(this, i+1));
//                }
//            }
//        }else{
//            inputPortList.set(no-1, new InputPort(this, no));
//        }
    }
    public void addOut(Out out){
//        outBlockList.add(out);
//        int no = Integer.parseInt(out.getNo().getDataString());
//        if(outputPortList.size() <= no){
//            for(int i=outputPortList.size();i<no;i++){
//                if(i<(no-1)){
//                    outputPortList.add(null);
//                }else{
//                    outputPortList.add(new OutputPort(this, i+1));
//                }
//            }
//        }else{
//            outputPortList.set(no-1, new OutputPort(this, no));
//        }
    }
}

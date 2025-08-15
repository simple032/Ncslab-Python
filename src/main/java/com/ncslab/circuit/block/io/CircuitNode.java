package com.ncslab.circuit.block.io;

import java.util.ArrayList;
import java.util.List;

import com.ncslab.circuit.block.BlockMode;
import com.ncslab.circuit.block.io.CircuitPort;

public class CircuitNode {
	
	private List<CircuitPort> circuitPortList = new ArrayList<>();
	
	private int nodeId;
	
	private boolean isNodeCurrentDecided=false;
	
	public CircuitNode(int nodeId) {
		this.nodeId=nodeId;
	}
	
	public int getNodeId() {
		return this.nodeId;
	}
	
	public void addCircuitPort(CircuitPort circuitPort) {
		circuitPortList.add(circuitPort);
		circuitPort.setCircuitNode(this);
	}
	
	public List<CircuitPort> getCircuitPortList(){
		return this.circuitPortList;
	}
	
	public boolean isCircuitPortIncluded(CircuitPort port) {
		for(CircuitPort circuitPort:circuitPortList) {
			if(port==circuitPort) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getIsNodeCurrentDecided() {
		return this.isNodeCurrentDecided;
	}
	
	public void showNode() {
		System.out.println("Node "+nodeId+"...");
		int i=1;
		for(CircuitPort port:circuitPortList) {
			System.out.print(""+i+":"+port.getBlock().getBlockName()+"/"+port.getName()+"\t"+port.getIsCurrentDecided()+"\t");
			i++;
			
			List<PortCurrent> currentList=port.getCurrentList();
			for(PortCurrent current:currentList) {
				System.out.print(current.getCircuitBlock().getBlockName()+"/"+(current.getSign()?"+":"-"));
			}
			System.out.println();
		}
		
	}
	
	public List<CircuitPort> getOtherCircuitPortList(CircuitPort basePort){
		List<CircuitPort> otherPortList = new ArrayList<>();
		for(CircuitPort port:circuitPortList) {
			if(basePort!=port) {
				otherPortList.add(port);
			}
		}
		return otherPortList;
	}
	
	private void setupLastPortCurrent() {
		CircuitPort lastPort=null;
		List<CircuitPort> otherPortList = new ArrayList<>();
		
		for(CircuitPort port:circuitPortList) {
			if(port.getIsCurrentDecided()==false) {
				lastPort=port;
			}
			else {
				otherPortList.add(port);
			}
		}
		
		//
		lastPort.setupDirectCurrent(otherPortList);
	}
	
	//搜索一个Node
	public void searchNode(CircuitPort basePort) {
		
		//如果节点已经被确定了,那就不用遍历了,返回.直接引用即可
		if(isNodeCurrentDecided) {
			return;
		}
		
		//System.out.println("Setup node "+nodeId+"...");
		
		//记录被确定电流表达的Port的个数,记录到n
		List<CircuitPort> undecidedPortList = new ArrayList<>();
		int n=0;
		for(CircuitPort port:circuitPortList) {
			//如果Port被确定电流表达,计数加一
			if(port.getIsCurrentDecided()) {
				n++;
			}
			else
			//如果Port时连枝,说明电流就是输出量,可以被直接决定
			if(port.getBlock().getBlockMode()==BlockMode.Link) {
				port.setupDirectCurrent();
				//port.getBlock().getAnotherCircuitPort(port).setupDirectCurrent();
				n++;
			}
			else {
				//把除baseport之外的未定port,放在undecidedPortList中
				if(port!=basePort) {
					undecidedPortList.add(port);
				}
			}
		}
		
		//System.out.println(circuitPortList.size()+":"+n);
		
		//如果没有被决定电流的Port个数只有一个,根据节点总电流为0,那就可以决定剩余的那一个节点的电流
		if(circuitPortList.size()-n==1) {
			//根据其他节点决定剩余一个节点
			setupLastPortCurrent();
		}
		//否则就要递归遍历
		else {
			//一般用BasePort的电流作为未定值,由其他节点计算
			//如果没有BasePort,就指定第一个未定Port为BasePort
			if(basePort==null) {
				basePort=undecidedPortList.remove(0);
			}
			
			//寻找其他Port相连的Block的另一个节点,进行遍历
			for(CircuitPort port:undecidedPortList) {
				//寻找Block另一边的anotherPort,anotherPort与Port电流相同,方向相反
				CircuitPort anotherPort=port.getBlock().getAnotherCircuitPort(port);
				//遍历AnotherPort的节点,获得所有的节点电流
				anotherPort.getCircuitNode().searchNode(anotherPort);
				//将AnotherPort相反的电流,赋值给Port
				port.setCurrentList(anotherPort.getReverseCurrentList());
			}
			//确定剩下的那个Port的电流表达式
			setupLastPortCurrent();
		}
		
		//遍历完毕,这个节点所有port的电流都已经确定,设置标志
		isNodeCurrentDecided=true;
		
	}
}

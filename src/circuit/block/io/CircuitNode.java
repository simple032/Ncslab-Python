package circuit.block.io;

import java.util.Vector;

import circuit.block.io.CircuitPort;

public class CircuitNode {
	
	private Vector<CircuitPort> circuitPortList=new Vector<CircuitPort>();
	
	private int nodeId;
	
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
	
	public Vector<CircuitPort> getCircuitPortList(){
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
}

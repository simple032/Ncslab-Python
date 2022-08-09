package circuit.loop;

public class CircuitLoopException extends Exception {
	private String msg;
	public CircuitLoopException(String msg){
		super(msg);
	}
	
	protected void setMessage(String msg) {
		this.msg=msg;
	}
}

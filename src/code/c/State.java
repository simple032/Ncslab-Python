package code.c;

public class State {
	private int id;
	private String name;
	private String localName;
	private int width=1;
	private DataTypeC type=DataTypeC.REAL;
	
	public State(int id,String name,String localName){
		this.type=DataTypeC.REAL;
		this.id=id;
		this.name=name;
		this.localName=localName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDefineString() {
		String defineString="";
		
		switch(type) {
		case REAL:
			defineString="REAL"; 
		}
		
		return defineString;
	}
}

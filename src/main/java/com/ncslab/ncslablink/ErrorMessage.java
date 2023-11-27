package com.ncslab.ncslablink;

public class ErrorMessage {
	public static final int AlgebraicLoop=1;
	
	private int code=0;
	private String message;
	
	public ErrorMessage(int code,String message){
		this.code=code;
		this.message=message;
		
	}
	
	public String getMessage() {
		return message;
	}
}

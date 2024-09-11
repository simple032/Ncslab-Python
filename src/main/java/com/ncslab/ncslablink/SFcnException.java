package com.ncslab.ncslablink;

public class SFcnException extends Exception {

	private static final long serialVersionUID = 3184088244132071193L;
	private String msg;
	public SFcnException(String msg){
		super(msg);
	}

	protected void setMessage(String msg) {
		this.msg=msg;
	}
}

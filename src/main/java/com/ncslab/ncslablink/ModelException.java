package com.ncslab.ncslablink;

public class ModelException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7709463740975795805L;
	private String msg;
	public ModelException(String msg){
		super(msg);
	}
	
	protected void setMessage(String msg) {
		this.msg=msg;
	}
}

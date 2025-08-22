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
	
	public ModelException(String string, Exception e) {
		super(string, e);
    }

    protected void setMessage(String msg) {
		this.msg=msg;
	}
}

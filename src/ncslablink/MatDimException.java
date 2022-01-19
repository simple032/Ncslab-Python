package ncslablink;

import block.Block;

public class MatDimException extends ModelException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2177281651018598767L;
	private int code;
	public MatDimException(String msg){
		super(msg);
	}
	
	public MatDimException(int code,Block block) {
		super("");
		
		String msg;
		
		switch(code) {
		case 1:
			msg="The input of Matrix is not allowed for the block "+block.getBlockName()+".";
			this.setMessage(msg);
			break;
		}
	}
}

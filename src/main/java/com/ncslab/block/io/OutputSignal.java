package com.ncslab.block.io;

import Jama.Matrix;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.Block;
import lombok.Getter;
import lombok.Setter;

public class OutputSignal {
	private int id;
	private String name;
	private String localName;
    @Getter
    private int width=1;
    @Getter
    private int height=1;
	@Setter
    @Getter
    private DataType dataType = DataType.REAL; // Dimension: REAL (scalar) or MATRIX (vector/matrix)
    @Setter
    @Getter
    private CDataType cDataType = CDataType.DOUBLE; // Actual C data type: int8, int16, double, etc.
    @Getter
    private Block block;
	private int outputPortId;

    @Getter
    private Data data = new Data(0);

	public OutputSignal(Block block,int id,int outputPortId,String localName){
		this.block=block;
		this.id=id;
		this.name = (block != null) ? "Block" + block.getBlockId() + "_Output" + outputPortId : "Signal_" + id;
		this.localName=localName;
		this.outputPortId=outputPortId;
	}

	public OutputSignal(Block block, int id, int outputPortId, String localName, int width,int height) {
		this.block=block;
		this.id=id;
		this.width=width;
		this.height=height;
		this.name = (block != null) ? "Block" + block.getBlockId() + "_Output" + outputPortId : "Signal_" + id;
		this.localName=localName;
		this.outputPortId=outputPortId;

		if(width>1||height>1) {
			this.dataType =DataType.MATRIX;
            this.data = new Data(height, width);
		}
	}

	public String getName() {
		this.name = (block != null) ? "Block" + block.getBlockId() + "_Output" + outputPortId : "Signal_" + id;
		return this.name;
	}

	public String getDefineString() {
		String defineString="";

		switch(dataType) {
		case REAL:
		case MATRIX:
			defineString="REAL";
		}

		return defineString;
	}

	public String getDefineCodeC() {
		String code="";
		System.out.println("OutputSignal.getDefineCodeC: name=" + getName() +
		                 ", dataType=" + dataType + ", cDataType=" + cDataType +
		                 ", height=" + height + ", width=" + width);

		// CRITICAL FIX: Always check actual dimensions to determine scalar vs matrix
		// Even if dataType is set to MATRIX, if dimensions are 1x1, use scalar type
		boolean isActuallyScalar = (height == 1 && width == 1);

		switch(dataType) {
		case REAL:
			// Use C data type if specified, otherwise default to REAL (double)
			if (cDataType != null && cDataType != CDataType.DOUBLE) {
				code += cDataType.getCppType() + " " + getName() + ";\n";
			} else {
				code += "REAL " + getName() + ";\n";
			}
			break;
		case MATRIX:
			// CRITICAL: Check if this is actually a scalar (1x1)
			if (isActuallyScalar) {
				// Output as scalar, not matrix
				if (cDataType != null && cDataType != CDataType.DOUBLE) {
					code += cDataType.getCppType() + " " + getName() + ";\n";
				} else {
					code += "REAL " + getName() + ";\n";
				}
			} else {
				// True matrix/vector - declare matrix variable with dimensions
				// CRITICAL FIX: Initialize Eigen matrices with dimensions to avoid 0x0 default construction
				// This prevents "row >= 0 && row < rows()" assertion failures when accessing elements
				// before explicit resize()
				if (cDataType != null) {
					// Typed matrix - initialize with dimensions
					code += cDataType.getMatrixTypeName() + " " + getName() + "(" + height + ", " + width + ");\n";
				} else {
					// Default dynamic matrix (Matrix = Eigen::MatrixXd) - initialize with dimensions
					code += "Matrix " + getName() + "(" + height + ", " + width + ");\n";
				}
			}
			break;
		case STRING:
			// String type - use std::string
			code += "std::string " + getName() + ";\n";
			break;
		}
		return code;

	}

	/**
	 * Generate initialization code to resize matrix variables
	 */
	public String getInitCodeC() {
		String code = "";

		// Only generate resize code for matrices (not scalars)
		boolean isActuallyScalar = (height == 1 && width == 1);

		if (dataType == DataType.MATRIX && !isActuallyScalar) {
			code += "    " + getName() + ".resize(" + height + ", " + width + ");\n";
		}

		return code;
	}

    public void setWidth(int width) {
		// Update width dimension
		this.width = width;

		// CRITICAL FIX: Always check BOTH dimensions to determine dataType
		// A signal is REAL only if BOTH width=1 AND height=1
		if(width > 1 || height > 1) {
			this.dataType = DataType.MATRIX;
		} else {
			this.dataType = DataType.REAL;
		}

		// CRITICAL FIX: Always recreate Data when dimensions change
		// This ensures Data object matches the dimension metadata
		if(width != this.data.getWidth() || height != this.data.getHeight()) {
			if(this.dataType == DataType.MATRIX) {
				this.data = new Data(height, width);
			} else {
				this.data = new Data(0.0);  // Scalar with explicit double
			}
		}
	}

    public void setHeight(int height) {
		// Update height dimension
		this.height = height;

		// CRITICAL FIX: Always check BOTH dimensions to determine dataType
		// A signal is REAL only if BOTH width=1 AND height=1
		if(width > 1 || height > 1) {
			this.dataType = DataType.MATRIX;
		} else {
			this.dataType = DataType.REAL;
		}

		// CRITICAL FIX: Always recreate Data when dimensions change
		// This ensures Data object matches the dimension metadata
		if(width != this.data.getWidth() || height != this.data.getHeight()) {
			if(this.dataType == DataType.MATRIX) {
				this.data = new Data(height, width);
			} else {
				this.data = new Data(0.0);  // Scalar with explicit double
			}
		}
	}

    public void setValue(double value){
        this.data.setInitValue(value);
    }

    public void setValue(Matrix matrix){
        this.data.setMatrix(matrix);
    }

    public void setData(Data data){
    	if(data.getDataType()==DataType.MATRIX) {
            this.data.setMatrix(data.getMatrix());
        }else {
            this.data.setInitValue(data.getInitValue());
        }
    }
}

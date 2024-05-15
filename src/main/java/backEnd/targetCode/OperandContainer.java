package backEnd.targetCode;

import frontEnd.lexic.dictionary.tokenEnums.DataType;

public class OperandContainer {
	private Operand destination;
	private Operand operand1;
	private Operand operand2;
	private DataType operandsType;	// If there is no type, it's an operation between registers (addresses).

	public void setDestination(Operand destination) {
		this.destination = destination;
		if (destination != null) {
			setOperandsType(destination.getType());
		}
	}

	public void setOperand1(Operand operand1) {
		this.operand1 = operand1;
		if (operand1 != null) {
			setOperandsType(operand1.getType());
		}
	}

	public void setOperand2(Operand operand2) {
		this.operand2 = operand2;
		if (operand2 != null) {
			setOperandsType(operand2.getType());
		}
	}

	public void setOperandsType(DataType operandsType) {
		if ((this.operandsType != operandsType || this.operandsType == null) && operandsType != null) {
			this.operandsType = operandsType;
		}
	}

	public DataType getOperandsType() {
		return operandsType;
	}

	public Operand getDestination() {
		return destination;
	}

	public Operand getOperand1() {
		return operand1;
	}

	public Operand getOperand2() {
		return operand2;
	}
}

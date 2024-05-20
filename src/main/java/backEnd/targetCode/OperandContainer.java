package backEnd.targetCode;

import frontEnd.lexic.dictionary.tokenEnums.DataType;

public class OperandContainer {
	private Operand destination;
	private Operand operand1;
	private Operand operand2;
	private String operator;

	public void setDestination(Operand destination) {
		this.destination = destination;
	}

	public void setOperand1(Operand operand1) {
		this.operand1 = operand1;
	}

	public void setOperand2(Operand operand2) {
		this.operand2 = operand2;
	}

	public void setOperator(String operator) {
		this.operator = operator;
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

	public String getOperator() {
		return operator;
	}
}

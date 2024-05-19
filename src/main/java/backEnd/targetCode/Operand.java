package backEnd.targetCode;

import frontEnd.lexic.dictionary.tokenEnums.DataType;

public class Operand {
	private String value;	// Register address (relative to fp or temporal register) or literal.
	private DataType type;
	private boolean isRegister;	// It's a temporary register or a variable.

	public Operand(boolean isRegister, DataType type, String value) {
		this.isRegister = isRegister;
		this.type = type;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public boolean isRegister() {
		return isRegister;
	}

	public DataType getType() {
		return type;
	}

	public void setRegister(boolean register) {
		isRegister = register;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

package backEnd.targetCode.operations;

public class Register {
    private final OptionRegisterEnum registerEnum;
    private final String registerName;
    private final String variableName;

    public Register(OptionRegisterEnum registerEnum, String registerName, String variableName) {
        this.registerEnum = registerEnum;
        this.registerName = registerName;
        this.variableName = variableName;
    }

    public OptionRegisterEnum getRegisterEnum() {
        return registerEnum;
    }

    public boolean sameEnum(OptionRegisterEnum registerEnum) {
        return this.registerEnum == registerEnum;
    }

    public String getRegisterName() {
        return registerName;
    }

    public String getVariableName() {
        return variableName;
    }
}

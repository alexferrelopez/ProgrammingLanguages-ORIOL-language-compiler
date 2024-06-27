package backEnd.targetCode;

import java.util.ArrayList;
import java.util.List;

public class FunctionContext {
    private final String functionName;
    private final List<Pair<String,String>> registerAddressPairList;

    public FunctionContext(String functionName) {
        this.functionName = functionName;
        this.registerAddressPairList = new ArrayList<>();
    }

    public String getFunctionName() {
        return functionName;
    }

    public void addRegisterAddressPair(String register, String address) {
        registerAddressPairList.add(new Pair<>(register, address));
    }

    public List<Pair<String, String>> getRegisterAddressPairList() {
        return registerAddressPairList;
    }

    public void clearRegisterAddressPairList() {
        registerAddressPairList.clear();
    }
}

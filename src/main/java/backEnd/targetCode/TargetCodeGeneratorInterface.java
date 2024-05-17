package backEnd.targetCode;

import backEnd.exceptions.TargetCodeException;
import frontEnd.intermediateCode.TACInstruction;

import java.util.List;

public interface TargetCodeGeneratorInterface {
	void generateMIPS(List<TACInstruction> instructions) throws TargetCodeException;
}
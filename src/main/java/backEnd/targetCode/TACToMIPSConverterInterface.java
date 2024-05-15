package backEnd.targetCode;

import frontEnd.exceptions.InvalidFileException;
import frontEnd.intermediateCode.TACInstruction;

import java.util.List;

public interface TACToMIPSConverterInterface {
	void generateMIPS(List<TACInstruction> instructions) throws InvalidFileException;
}
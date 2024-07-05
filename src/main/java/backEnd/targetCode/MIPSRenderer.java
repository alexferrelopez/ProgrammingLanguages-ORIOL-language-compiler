package backEnd.targetCode;

import java.util.Map;

public interface MIPSRenderer {
    String render(String templatePath, Map<String, String> placeholders);
}

package frontEnd.lexic.dictionary;

import java.util.List;

public interface TokenType {

    // Getter method for the pattern
    String getPattern();

    List<String> getTranslation();
}
package frontEnd.sintaxis.grammar.derivationRules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.grammar.AbstractSymbol;

import java.io.File;
import java.io.IOException;
import java.util.*;

final class ParsingTableWrapper {
    public int grammarHashCode; // This is a hash code of the grammar that was used to generate the parsing table, to check if the parsing table is still valid
    public Map<NonTerminalSymbol, List<AbstractSymbol>>[][] parsingTable;

    @JsonCreator
    public ParsingTableWrapper(@JsonProperty("grammarHashCode") int grammarHashCode, @JsonProperty("parsingTable") Map<NonTerminalSymbol, List<AbstractSymbol>>[][] parsingTable) {
        this.grammarHashCode = grammarHashCode;
        this.parsingTable = parsingTable;
    }
}

public class ParsingTable {
    private final Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar;
    private final List<TerminalSymbol> uniqueTerminals;
    private final List<NonTerminalSymbol> uniqueNoTerminals;
    private static final String BASE_PATH = "src/main/resources/";
    private static final String PARSE_TABLE_FILE = BASE_PATH + "parsingTable.json";

    private final ParsingTableWrapper parsingTableWrapper;

    /**
     * Constructor class Parsing table
     *
     * @param grammar our grammar
     */
    public ParsingTable(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar) {
        ParsingTableWrapper parsingTableWrapper;
        this.grammar = grammar;
        this.uniqueNoTerminals = findUniqueNoTerminals(grammar);
        this.uniqueTerminals = findUniqueTerminals(grammar);

        if ((new File(PARSE_TABLE_FILE).exists())) {
            parsingTableWrapper = getParsingTableFromJson();
            if (parsingTableWrapper.grammarHashCode != grammar.hashCode()) {
                parsingTableWrapper = getParsingTableFromGrammar();
                cacheParsingTable(parsingTableWrapper);
            }
        } else {
            parsingTableWrapper = getParsingTableFromGrammar();
            cacheParsingTable(parsingTableWrapper);
        }
        this.parsingTableWrapper = parsingTableWrapper;
    }

    private void cacheParsingTable(ParsingTableWrapper parsingTableWrapper) {
        //write json to file
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(ParsingTable.PARSE_TABLE_FILE), parsingTableWrapper);

        } catch (IOException ignored) {
        }
    }

    public boolean compareObjects(Object reference, Object value) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json1 = mapper.writeValueAsString(reference);
            String json2 = mapper.writeValueAsString(value);

            return json1.equals(json2);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compare objects", e);
        }
    }

    private ParsingTableWrapper getParsingTableFromJson() {
        ParsingTableWrapper parsingTableWrapper;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //this.parsingTableWrapper = objectMapper.readValue(new File(PARSE_TABLE_FILE), new TypeReference<>() {
            parsingTableWrapper = objectMapper.readValue(new File(PARSE_TABLE_FILE), new TypeReference<>() {
            });
            return parsingTableWrapper;
        } catch (IOException e) {
            parsingTableWrapper = getParsingTableFromGrammar();
        }
        return parsingTableWrapper;
    }

    /**
     * Method to fill in the parsing table
     */
    private ParsingTableWrapper getParsingTableFromGrammar() {
        ParsingTableWrapper parsingTableWrapper = new ParsingTableWrapper(grammar.hashCode(), new Map[uniqueNoTerminals.size()][uniqueTerminals.size()]);
        for (int i = 0; i < uniqueNoTerminals.size(); i++) {
            for (int j = 0; j < uniqueTerminals.size(); j++) {
                NonTerminalSymbol nt = uniqueNoTerminals.get(i);
                TerminalSymbol t = uniqueTerminals.get(j);
                Map<NonTerminalSymbol, List<AbstractSymbol>> production = First.getProduction(grammar, nt, t);

                if (production != null) {
                    parsingTableWrapper.parsingTable[i][j] = production;
                    continue;
                }
                if (Follow.haveFollow(grammar, nt, t)) {
                    production = Follow.getProduction(grammar, nt);
                    parsingTableWrapper.parsingTable[i][j] = production;
                }

            }
        }
        return parsingTableWrapper;
    }

    /**
     * Method to get the unique terminals
     *
     * @param grammar Our gramamr
     * @return List of unique terminals
     */
    private List<TerminalSymbol> findUniqueTerminals(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar) {
        List<TerminalSymbol> uniqueTerminals = new LinkedList<>();
        for (Map.Entry<NonTerminalSymbol, List<List<AbstractSymbol>>> entry : grammar.entrySet()) {
            List<List<AbstractSymbol>> symbolMatrix = entry.getValue();
            for (List<AbstractSymbol> symbolList : symbolMatrix) {
                for (AbstractSymbol productionSymbols : symbolList) {
                    if (productionSymbols.isTerminal()) {
                        if (productionSymbols.getName().equals(TerminalSymbol.EPSILON)) continue;
                        boolean found = false;
                        for (TerminalSymbol terminal : uniqueTerminals) {
                            if (terminal.getName().equals((productionSymbols.getName()))) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            uniqueTerminals.add((TerminalSymbol) productionSymbols);
                        }
                    }

                }
            }
        }
        uniqueTerminals.add(new TerminalSymbol("EOF"));
        return uniqueTerminals;
    }

    /**
     * Method to get the unique non-terminals
     *
     * @param grammar Our grammar
     * @return List of unique no terminals
     */
    private List<NonTerminalSymbol> findUniqueNoTerminals(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar) {
        Set<NonTerminalSymbol> keys = grammar.keySet();
        return new LinkedList<>(keys);
    }


    public List<AbstractSymbol> getProductionList(NonTerminalSymbol nonTerminal, Token terminal) {
        int positionTerminal = -1;
        int positionNonTerminal = -1;
        for (int i = 0; i < uniqueTerminals.size(); i++) {
            if (uniqueTerminals.get(i).getName().equals(String.valueOf(terminal.getType()))) {
                positionTerminal = i;
                break;
            }
        }
        if (positionTerminal == -1) {
            return null;
        }
        for (int i = 0; i < uniqueNoTerminals.size(); i++) {
            String str = uniqueNoTerminals.get(i).getName();
            String str2 = nonTerminal.getName();
            if (str.equals(str2)) {
                positionNonTerminal = i;
                break;
            }
        }
        if (positionNonTerminal == -1) {
            return null;
        }
        Map productionMap = parsingTableWrapper.parsingTable[positionNonTerminal][positionTerminal];
        if (Objects.isNull(productionMap)) {
            return null;
        }
        List<AbstractSymbol> abstractSymbols = new LinkedList<AbstractSymbol>(productionMap.values());


        return (List<AbstractSymbol>) abstractSymbols.get(0);
    }

    public Map getProduction(NonTerminalSymbol nonTerminal, Token terminal) {
        int positionTerminal = -1;
        int positionNonTerminal = -1;
        for (int i = 0; i < uniqueTerminals.size(); i++) {
            if (uniqueTerminals.get(i).getName().equals(String.valueOf(terminal.getType()))) {
                positionTerminal = i;
                break;
            }
        }
        if (positionTerminal == -1) {
            return null;
        }
        for (int i = 0; i < uniqueNoTerminals.size(); i++) {
            String str = uniqueNoTerminals.get(i).getName();
            String str2 = nonTerminal.getName();
            if (str.equals(str2)) {
                positionNonTerminal = i;
                break;
            }
        }
        if (positionNonTerminal == -1) {
            return null;
        }
        Map productionMap = parsingTableWrapper.parsingTable[positionNonTerminal][positionTerminal];
        return productionMap;
    }


}
package FrontEnd;
import java.util.*;

public class ParsingTable {
    private Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar;
    private List<TerminalSymbol> uniqueTerminals;
    private List<NonTerminalSymbol> uniqueNoTerminals;
    private Map[][] parsingTable;

    /**
     * Constructor class Parsing table
     * @param grammar our grammar
     */
    public ParsingTable(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar) {
        this.grammar = grammar;
        this.uniqueNoTerminals = findUniqueNoTerminals(grammar);
        this.uniqueTerminals = findUniqueTerminals(grammar);
        this.parsingTable = new Map[uniqueNoTerminals.size()][uniqueTerminals.size()];
        fillParsingTable();
    }

    /**
     * Method to fill in the parsing table
     */
    private void fillParsingTable() {
        for (int i = 0; i < uniqueNoTerminals.size(); i++) {
            for (int j = 0; j < uniqueTerminals.size(); j++) {
                NonTerminalSymbol nt = uniqueNoTerminals.get(i);
                TerminalSymbol t = uniqueTerminals.get(j);
                Map<NonTerminalSymbol, List<AbstractSymbol>> production = First.getProduction(grammar, nt, t);
                if (production != null) {
                    parsingTable[i][j] = production;
                    continue;
                }
                if (Follow.haveFollow(grammar, nt, t)) {
                    production = Follow.getProduction(grammar, nt);
                    parsingTable[i][j] = production;
                }

            }
        }
    }
}
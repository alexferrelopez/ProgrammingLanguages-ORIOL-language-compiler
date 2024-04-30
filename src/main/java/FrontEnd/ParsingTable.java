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

    /**
     * Method to get the unique terminals
     * @param grammar Our gramamr
     * @return List of unique terminals
     */
    private List<TerminalSymbol> findUniqueTerminals(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar) {
        List<TerminalSymbol> uniqueTerminals = new LinkedList<>();
        for (Map.Entry<NonTerminalSymbol, List<List<AbstractSymbol>>> entry : grammar.entrySet()) {
            List<List<AbstractSymbol>> symbolMatrix = entry.getValue();
            for(List<AbstractSymbol> symbolList: symbolMatrix){
                for(AbstractSymbol productionSymbols: symbolList){
                    if(productionSymbols.isTerminal()){
                        if(productionSymbols.getName().equals(TerminalSymbol.EPSILON)) continue;
                        boolean found = false;
                        for(TerminalSymbol terminal: uniqueTerminals){
                            if(terminal.getName().equals((productionSymbols.getName()))){
                                found = true;
                                break;
                            }
                        }
                        if(!found){
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
     * @param grammar Our grammar
     * @return List of unique no terminals
     */
    private List<NonTerminalSymbol> findUniqueNoTerminals(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar) {
        Set<NonTerminalSymbol> keys = grammar.keySet();
        return new LinkedList<>(keys);
    }

}
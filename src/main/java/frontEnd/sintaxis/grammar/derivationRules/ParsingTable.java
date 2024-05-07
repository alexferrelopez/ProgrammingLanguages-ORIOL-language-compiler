package frontEnd.sintaxis.grammar.derivationRules;

import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.grammar.AbstractSymbol;

import java.util.*;

public class ParsingTable {
    private final Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar;
    private final List<TerminalSymbol> uniqueTerminals;
    private final List<NonTerminalSymbol> uniqueNoTerminals;
    private final Map[][] parsingTable;

    /**
     * Constructor class Parsing table
     *
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

    public Map[][] getParsingTable() {
        return parsingTable;
    }


    public List<AbstractSymbol> getProduction(NonTerminalSymbol nonTerminal, Token terminal) {
        int positionTerminal = -1;
        int positionNonTerminal = -1;
        for (int i = 0; i < uniqueTerminals.size(); i++) {
            if (uniqueTerminals.get(i).getName().equals(String.valueOf(terminal.getType()))) {
                positionTerminal = i;
                break;
            }
        }
        if (positionTerminal == -1) {
            System.out.println("Error, no s'ha trobat el Terminal :(");
            //TODO error handler (mirar que no siguin paraules prohibides (aaron, alexia, alex, gemma, oriol...))
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
            System.out.println("Error, no s'ha trobat el noTerminal :(");
            return null;
        }
        Map productionMap = parsingTable[positionNonTerminal][positionTerminal];
        if (Objects.isNull(productionMap)) {
            System.out.println("There is no production associated with " + terminal.getLexeme() + " with " + nonTerminal.getName());//TODO throw exception
            return null;
        }
        List<AbstractSymbol> abstractSymbols = new LinkedList<AbstractSymbol>(productionMap.values());


        return (List<AbstractSymbol>) abstractSymbols.get(0);
    }


}
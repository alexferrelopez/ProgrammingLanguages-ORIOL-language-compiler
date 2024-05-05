package frontEnd.sintaxis.grammar.derivationRules;

import frontEnd.exceptions.InvalidTokenException;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.sintaxis.grammar.AbstractSymbol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class First {

    /**
     * Method for adding a new first to the list of firsts
     *
     * @param firsts   list of firsts
     * @param terminal new no-terminal to be added to the firsts list
     */
    private static void addFirst(List<TerminalSymbol> firsts, TerminalSymbol terminal) {

        for (TerminalSymbol t : firsts) {
            if (t.getName().equals(terminal.getName())) {
                return;
            }
        }
        firsts.add(terminal);
    }

    /**
     * Method for generating the production of the no terminal
     *
     * @param nt                no terminal symbol for which production is being generated.
     * @param productionSymbols the list of terminal or no terminal symbols representing the production
     * @return the production
     */
    private static Map<NonTerminalSymbol, List<AbstractSymbol>> generateProduction(NonTerminalSymbol nt, List<AbstractSymbol> productionSymbols) {
        Map<NonTerminalSymbol, List<AbstractSymbol>> productionParser = new HashMap<>();
        productionParser.put(nt, productionSymbols);
        return productionParser;
    }

    /**
     * Method that obtains a production of the grammar having the specified no terminal and terminal.
     * Each no terminal is associated with a list of production rules.
     *
     * @param grammar our grammar
     * @param nt      no terminal
     * @param t       terminaal
     * @return the production
     */
    public static Map<NonTerminalSymbol, List<AbstractSymbol>> getProduction(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt, TerminalSymbol t) {
        List<List<AbstractSymbol>> production = grammar.get(nt);
        for (List<AbstractSymbol> tint : production) {
            if (tint.get(0) instanceof TerminalSymbol) {
                if (tint.get(0).getName().equals(t.getName())) {
                    return generateProduction(nt, tint);
                }
            } else {
                if (containsFirst(getFirsts(grammar, (NonTerminalSymbol) tint.get(0)), t)) {
                    return generateProduction(nt, tint);
                }
            }
        }
        return null;
    }

    /**
     * Method to check if a production of firsts already contains a terminal
     *
     * @param firsts   list of firsts
     * @param terminal Terminal to know if already exists in the list of firsts
     * @return True if exists, False if not
     */
    private static boolean containsFirst(List<TerminalSymbol> firsts, TerminalSymbol terminal) {
        for (TerminalSymbol t : firsts) {
            if (t.getName().equals(terminal.getName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Method that obtains a production of the grammar having the specified no terminal and terminal.
     * Each no terminal is associated with a list of production rules.
     *
     * @param grammar our grammar
     * @param nt      no terminal
     * @return the production
     */
    public static List<TerminalSymbol> getProductionFirsts(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt) {
        List<TerminalSymbol> terminals = new LinkedList<>(); //firsts
        List<List<AbstractSymbol>> production = grammar.get(nt);
        for (List<AbstractSymbol> differentProductions : production) {
            if (differentProductions.get(0) instanceof TerminalSymbol) {
                addFirst(terminals, (TerminalSymbol) differentProductions.get(0));
            } else {
                int pointer = 0;
                List<TerminalSymbol> firstList = getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(pointer));
                int i = 0;
                while (i < firstList.size()) {
                    if (!firstList.get(i).getName().equals(TerminalSymbol.EPSILON)) {//No es Ɛ
                        addFirst(terminals, firstList.get(i));
                    } else {//Is Ɛ
                        for (TerminalSymbol t : getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(++pointer))) {
                            addFirst(firstList, t);
                        }
                    }
                    i++;
                }
            }
        }
        return terminals;
    }

    /**
     * Method for obtain the firsts given a non-terminal
     *
     * @param grammar our grammar
     * @param nt      non-terminal
     * @return list of terminals
     */
    public static List<TerminalSymbol> getFirsts(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt) {
        List<TerminalSymbol> terminals = new LinkedList<>(); //firsts
        List<List<AbstractSymbol>> production = grammar.get(nt);
        for (List<AbstractSymbol> differentProductions : production) {
            if (differentProductions.get(0).isTerminal()) {
                addFirst(terminals, (TerminalSymbol) differentProductions.get(0));
            } else {
                int pointer = 0;
                List<TerminalSymbol> firsts = getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(pointer));
                int i = 0;
                while (i < firsts.size()) {
                    if (!firsts.get(i).getName().equals(TerminalSymbol.EPSILON)) {//No es Ɛ
                        addFirst(terminals, firsts.get(i));
                    } else {
                        for (TerminalSymbol t : getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(pointer++))) {
                            addFirst(firsts, t);
                        }
                    }
                    i++;
                }
            }
        }
        return terminals;
    }

    public static List<Token> getFirstsToken(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt) {
        List<TerminalSymbol> firstsTerminalSymbol = getFirsts(grammar, nt);
        List<Token> firstTokens = new LinkedList<>();

        for (TerminalSymbol terminalSymbol : firstsTerminalSymbol) {
            try {
                Token token = Tokenizer.convertStringIntoToken(terminalSymbol.getName());
                firstTokens.add(token);
            } catch (InvalidTokenException e) {
                e.printStackTrace();
            }
        }
        return firstTokens;
    }

    /**
     * Method for displaying all the firsts of the grammar
     *
     * @param grammarMap
     */
    public static void displayAllFirsts(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammarMap) {
        for (NonTerminalSymbol nt : grammarMap.keySet()) {
            System.out.print("\nFirsts of " + nt.getName() + " are: ");
            for (Token terminal : First.getFirstsToken(grammarMap, nt)) {
                System.out.print(terminal.getLexeme() + " ");
            }
        }
    }


}

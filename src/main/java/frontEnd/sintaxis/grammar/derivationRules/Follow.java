package frontEnd.sintaxis.grammar.derivationRules;

import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.Tokenizer;
import frontEnd.sintaxis.grammar.AbstractSymbol;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Follow {
    /**
     * Method for adding follows
     *
     * @param follows  follows list
     * @param terminal terminal to be added to the list
     */
    private static void addFollow(List<TerminalSymbol> follows, TerminalSymbol terminal) {
        if (terminal.getName().equals(TerminalSymbol.EPSILON)) return;
        for (TerminalSymbol follow : follows) {
            if (follow.getName().equals(terminal.getName())) {
                return;
            }
        }
        follows.add(terminal);
    }

    /**
     * Method to check if a production of follows already contains a terminal
     *
     * @param grammar Our grammar
     * @param nt      Non-Terminal to get the follows
     * @param t       Terminal to know if already exists in the list of follows
     * @return True if exists, False if not
     */
    public static boolean haveFollow(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt, TerminalSymbol t) {
        List<TerminalSymbol> follows = getFollows(grammar, nt);
        for (TerminalSymbol follow : follows) {
            if (follow.getName().equals(t.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert the producion from a list to a Map
     *
     * @param nt   Non-terminal
     * @param tint The list of a production
     * @return A map of a production
     */
    private static Map<NonTerminalSymbol, List<AbstractSymbol>> generateProduction(NonTerminalSymbol nt, List<AbstractSymbol> tint) {
        Map<NonTerminalSymbol, List<AbstractSymbol>> productionParser = new LinkedHashMap<>();
        productionParser.put(nt, tint);
        return productionParser;
    }

    /**
     * Method for obtaining a terminal list of all the follows of a given non-terminal
     *
     * @param grammar    Our grammar
     * @param noTerminal The non-terminal to be found its follows
     * @return list of all the follows of the given non-terminal
     */
    public static List<TerminalSymbol> getFollows(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol noTerminal) {
        List<TerminalSymbol> follows = new LinkedList<>();
        if (noTerminal.isAxioma()) {
            addFollow(follows, new TerminalSymbol("EOF"));
        }
        for (Map.Entry<NonTerminalSymbol, List<List<AbstractSymbol>>> entry : grammar.entrySet()) {
            for (List<AbstractSymbol> symbolList : entry.getValue()) {
                for (AbstractSymbol symbol : symbolList) {
                    if (symbol.equals(noTerminal)) {
                        int pos = symbolList.indexOf(noTerminal);
                        if (symbolList.size() > pos + 1) {
                            AbstractSymbol next = symbolList.get(pos + 1);
                            if (next.isTerminal()) {
                                addFollow(follows, (TerminalSymbol) next);
                            } else {
                                List<TerminalSymbol> firsts = First.getFirsts(grammar, (NonTerminalSymbol) next);
                                for (TerminalSymbol t : firsts) {
                                    if (!t.getName().equals(TerminalSymbol.EPSILON)) {
                                        addFollow(follows, t);
                                    } else {
                                        NonTerminalSymbol nt = entry.getKey();
                                        if (!nt.equals(noTerminal)) {
                                            List<TerminalSymbol> terminals = getFollows(grammar, nt);
                                            for (TerminalSymbol followTerminals : terminals) {
                                                addFollow(follows, followTerminals);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            NonTerminalSymbol nt = entry.getKey();
                            if (!nt.equals(noTerminal)) {
                                List<TerminalSymbol> terminals = getFollows(grammar, nt);
                                for (TerminalSymbol followTerminals : terminals) {
                                    addFollow(follows, followTerminals);
                                }
                            }
                        }
                    }
                }
            }
        }
        return follows;
    }

    public static List<Token> getFollowsToken(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol noTerminal) {
        List<TerminalSymbol> followsTerminalSymbol = getFollows(grammar, noTerminal);
        List<Token> followsTokens = new LinkedList<>();

        for (TerminalSymbol terminalSymbol : followsTerminalSymbol) {
            try {
                Token token = Tokenizer.convertStringIntoToken(terminalSymbol.getName());
                followsTokens.add(token);
            } catch (InvalidTokenException e) {
                e.printStackTrace();
            }
        }
        return followsTokens;
    }

    /**
     * Method for obtaining the production of follows of a NonTerminal Symbol
     *
     * @param grammar Our grammar
     * @param nt      Non-Terminal Symbol to seek its production
     * @return The production
     */
    public static Map<NonTerminalSymbol, List<AbstractSymbol>> getProduction(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt) {
        List<List<AbstractSymbol>> production = grammar.get(nt);
        for (List<AbstractSymbol> product : production) {
            for (AbstractSymbol tint : product) {
                if (tint instanceof TerminalSymbol) {
                    if (tint.getName().equals("ε")) {
                        return generateProduction(nt, product);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Method for displaying all thefollows
     *
     * @param grammarMap
     */
    public static void displayAllFollows(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammarMap) {
        for (NonTerminalSymbol nt : grammarMap.keySet()) {
            System.out.print("\nFollows of " + nt.getName() + " are: ");
            for (Token terminal : Follow.getFollowsToken(grammarMap, nt)) {
                System.out.print(terminal.getLexeme() + " ");
            }
        }
    }

    /**
     * Method for checking if a token is in the list of follows
     * @param follows list of follows
     * @param stringToken token to be checked
     * @return true if the token is in the list of follows, false if not
     */
    public static boolean containsToken(List<TerminalSymbol> follows, String stringToken) {
        for(TerminalSymbol ts: follows){
            if(ts.getName().equals(stringToken)){
                return true;
            }
        }
        return false;
    }
}
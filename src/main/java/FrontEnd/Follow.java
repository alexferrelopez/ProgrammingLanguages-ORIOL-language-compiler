package FrontEnd;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Follow {
    /**
     * Method for adding follows
     * @param follows follows list
     * @param terminal terminal to be added to the list
     */
    private static void addFollow(List<TerminalSymbol> follows, TerminalSymbol terminal){
        if(terminal.getName().equals(TerminalSymbol.EPSILON)) return;
        for(TerminalSymbol follow: follows){
            if(follow.getName().equals(terminal.getName())){
                return;
            }
        }
        follows.add(terminal);
    }

    /**
     * Method for obtaining a terminal list of all the follows of a given non-terminal
     * @param grammar Our grammar
     * @param noTerminal The non-terminal to be found its follows
     * @return list of all the follows of the given non-terminal
     */
    public static List<TerminalSymbol> getFollows(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol noTerminal) {
        List<TerminalSymbol> follows = new LinkedList<>();
        if(noTerminal.isAxioma()){
            addFollow(follows, new TerminalSymbol("EOF"));
        }
        for(Map.Entry<NonTerminalSymbol, List<List<AbstractSymbol>>> entry : grammar.entrySet()){
            for(List<AbstractSymbol> symbolList  :entry.getValue()){
                for(AbstractSymbol symbol: symbolList){
                    if(symbol.equals(noTerminal)){
                        int pos = symbolList.indexOf(noTerminal);
                        if(symbolList.size()>pos+1){
                            AbstractSymbol next = symbolList.get(pos+1);
                            if(next.isTerminal()){
                                addFollow(follows, (TerminalSymbol) next);
                            }else{
                                List<TerminalSymbol> firsts = First.getFirsts(grammar, (NonTerminalSymbol) next);
                                for (TerminalSymbol t:firsts) {
                                    if(!t.getName().equals(TerminalSymbol.EPSILON)){
                                        addFollow(follows, t);
                                    }else{
                                        NonTerminalSymbol nt = entry.getKey();
                                        if(!nt.equals(noTerminal)){
                                            List<TerminalSymbol> terminals = getFollows(grammar, nt);
                                            for(TerminalSymbol followTerminals: terminals){
                                                addFollow(follows, followTerminals);
                                            }
                                        }
                                    }
                                }
                            }
                        }else{
                            NonTerminalSymbol nt = entry.getKey();
                            if(!nt.equals(noTerminal)){
                                List<TerminalSymbol> terminals = getFollows(grammar, nt);
                                for(TerminalSymbol followTerminals: terminals){
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
}
package FrontEnd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Follow {
    /**
     * Method for adding follows
     * @param follows follows list
     * @param terminal terminal to be added to the list
     */
    private static void addFollow(List<Terminal> follows, Terminal terminal){
        if(terminal.getName().equals(Terminal.EPSILON)) return;
        for(Terminal follow: follows){
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
    public static List<Terminal> getFollows(Map<NoTerminal, List<List<TermiNoTerm>>> grammar, NoTerminal noTerminal) {
        List<Terminal> follows = new LinkedList<>();
        if(noTerminal.isAxioma()){
            addFollow(follows, new Terminal("EOF"));
        }
        for(Map.Entry<NoTerminal, List<List<TermiNoTerm>>> entry : grammar.entrySet()){
            for(List<TermiNoTerm> symbol  :entry.getValue()){
                for(TermiNoTerm tint: symbol){
                    if(tint.equals(noTerminal)){
                        int pos = symbol.indexOf(noTerminal);
                        if(symbol.size()>pos+1){
                            TermiNoTerm next = symbol.get(pos+1);
                            if(next.isTerminal()){
                                addFollow(follows, (Terminal) next);
                            }else{
                                List<Terminal> firsts = First.getFirsts(grammar, (NoTerminal) next);
                                for (Terminal t:firsts) {
                                    if(!t.getName().equals(Terminal.EPSILON)){
                                        addFollow(follows, t);
                                    }else{
                                        NoTerminal nt = entry.getKey();
                                        if(!nt.equals(noTerminal)){
                                            List<Terminal> terminals = getFollows(grammar, nt);
                                            for(Terminal followTerminals: terminals){
                                                addFollow(follows, followTerminals);
                                            }
                                        }
                                    }
                                }
                            }
                        }else{
                            NoTerminal nt = entry.getKey();
                            if(!nt.equals(noTerminal)){
                                List<Terminal> terminals = getFollows(grammar, nt);
                                for(Terminal followTerminals: terminals){
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
package FrontEnd;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;

public class First {

    /**
     *  Method for adding a new first to the list of firsts
     * @param firsts list of firsts
     * @param terminal new no-terminal to be added to the firsts list
     */
    private static void addFirst(List<TerminalSymbol> firsts, TerminalSymbol terminal){

        for(TerminalSymbol t: firsts){
            if(t.getName().equals(terminal.getName())){
                return;
            }
        }
        firsts.add(terminal);
    }

    /**
     * Method for obtain the firsts given a non-terminal
     * @param grammar our grammar
     * @param nt non-terminal
     * @return list of terminals
     */
    public static List<TerminalSymbol> getFirsts(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt) {
        List<TerminalSymbol> terminals = new LinkedList<>(); //firsts
        List<List<AbstractSymbol>> production = grammar.get(nt);
        for(List<AbstractSymbol> differentProductions: production){
            if(differentProductions.get(0).isTerminal()){
                addFirst(terminals, (TerminalSymbol)differentProductions.get(0));
            }else{
                int pointer = 0;
                List<TerminalSymbol> firsts = getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(pointer));
                int i = 0;
                while(i<firsts.size()){
                    if(!firsts.get(i).getName().equals(TerminalSymbol.EPSILON)){//No es Ɛ
                        addFirst(terminals, firsts.get(i));
                    }else{
                        for(TerminalSymbol t: getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(++pointer))){
                            addFirst(firsts, t);
                        }
                    }
                    i++;
                }
            }
        }
        return terminals;
    }


}

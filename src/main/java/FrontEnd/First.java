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
    private static void addFirst(List<Terminal> firsts, Terminal terminal){

        for(Terminal t: firsts){
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
    public static List<Terminal> getFirsts(Map<NoTerminal, List<List<TermiNoTerm>>> grammar, NoTerminal nt) {
        List<Terminal> terminals = new LinkedList<>(); //firsts
        List<List<TermiNoTerm>> production = grammar.get(nt);
        for(List<TermiNoTerm> differentProductions: production){
            if(differentProductions.get(0).isTerminal()){
                addFirst(terminals, (Terminal)differentProductions.get(0));
            }else{
                int pointer = 0;
                List<Terminal> firs = getFirsts(grammar, (NoTerminal) differentProductions.get(pointer));
                int i = 0;
                while(i<firs.size()){
                    if(!firs.get(i).getName().equals(Terminal.EPSILON)){//No es Ɛ
                        addFirst(terminals, firs.get(i));
                    }else{
                        for(Terminal t: getFirsts(grammar, (NoTerminal) differentProductions.get(++pointer))){
                            addFirst(firs, t);
                        }
                    }
                    i++;
                }
            }
        }
        return terminals;
    }


}

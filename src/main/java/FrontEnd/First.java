package FrontEnd;

import java.util.List;
import java.util.HashMap;
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
     * @param grammar our gramamr
     * @param nt non-terminal
     * @return list of terminals
     */
    public static List<Terminal> getFirsts(Map<NoTerminal, List<List<TermiNoTerm>>> grammar, NoTerminal nt) {
        List<Terminal> terminals = new LinkedList<>(); //firsts
        List<List<TermiNoTerm>> production = grammar.get(nt);
        for(List<TermiNoTerm> differentProductions: production){
            if(differentProductions.get(0) instanceof Terminal){
                addFirst(terminals, (Terminal)differentProductions.get(0));
            }else{
                int pointer = 0;
                List<Terminal> firs = getFirsts(grammar, (NoTerminal) differentProductions.get(pointer));
                int i = 0;
                while(i<firs.size()){//for(int i = 0; i<firs.size(); i++ )
                    if(!firs.get(i).getName().equals("Ɛ")){//No es Ɛ
                        //terminals.add(firs.get(i));
                        addFirst(terminals, firs.get(i));
                    }else{//Es Ɛ
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

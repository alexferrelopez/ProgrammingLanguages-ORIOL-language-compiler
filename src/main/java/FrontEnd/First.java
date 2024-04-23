package FrontEnd;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class First {
    private static void addFirst(List<Terminal> firsts, Terminal terminal){

        for(Terminal t: firsts){
            if(t.getName().equals(terminal.getName())){
                return;
            }
        }
        firsts.add(terminal);
    }

    public static List<Terminal> getFirsts(Map<NoTerminal, List<List<TermiNoTerm>>> grammar, NoTerminal nt) {
        List<Terminal> terminals = new LinkedList<>(); //firsts
        return terminals;
    }


}

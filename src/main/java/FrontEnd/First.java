package FrontEnd;

import java.util.HashMap;
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
     * Method for generating the production of the no terminal
     * @param nt no terminal symbol for which production is being generated.
     * @param tint the list of terminal or no terminal symbols representing the production
     * @return the production
     */
    private static Map<NonTerminalSymbol, List<AbstractSymbol>> generateProduction(NonTerminalSymbol nt, List<AbstractSymbol> tint){
        Map<NonTerminalSymbol, List<AbstractSymbol>> productionParser = new HashMap<>();
        productionParser.put(nt, tint);
        return productionParser;
    }

    /**
     *

     Method that obtains a production of the grammar having the specified no terminal and terminal.
     Each no terminal is associated with a list of production rules.
     * @param grammar our grammar
     * @param nt no terminal
     * @param t terminaal
     * @return the production
     */
    public static Map<NonTerminalSymbol, List<AbstractSymbol>> getProduction(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt, TerminalSymbol t){
        List<List<AbstractSymbol>> production = grammar.get(nt);
        for(List<AbstractSymbol> tint: production){
            if(tint.get(0) instanceof TerminalSymbol){
                if(tint.get(0).getName().equals(t.getName())){
                    return generateProduction(nt, tint);
                }
            }else{
                if(containsFirst(getFirsts(grammar, (NonTerminalSymbol) tint.get(0)), t)){
                    return generateProduction(nt, tint);
                }
            }
        }
        return null;
    }

    /**
     * Method to check if a production of firsts already contains a terminal
     * @param firsts list of firsts
     * @param terminal Terminal to know if already exists in the list of firsts
     * @return True if exists, False if not
     */
    private static boolean containsFirst(List<TerminalSymbol> firsts, TerminalSymbol terminal) {
        for(TerminalSymbol t : firsts){
            if(t.getName().equals(terminal.getName())){
                return true;
            }
        }
        return false;
    }


    /**
     *

     Method that obtains a production of the grammar having the specified no terminal and terminal.
     Each no terminal is associated with a list of production rules.
     * @param grammar our grammar
     * @param nt no terminal
     * @return the production
     */
    public static List<TerminalSymbol> getProductionFirsts(Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar, NonTerminalSymbol nt) {
        List<TerminalSymbol> terminals = new LinkedList<>(); //firsts
        List<List<AbstractSymbol>> production = grammar.get(nt);
        for(List<AbstractSymbol> differentProductions: production){
            if(differentProductions.get(0) instanceof TerminalSymbol){
                addFirst(terminals, (TerminalSymbol)differentProductions.get(0));
            }else{
                int pointer = 0;
                List<TerminalSymbol> firs = getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(pointer));
                int i = 0;
                while(i<firs.size()){
                    if(!firs.get(i).getName().equals("Ɛ")){//No es Ɛ
                        //terminals.add(firs.get(i));
                        addFirst(terminals, firs.get(i));
                    }else{//Es Ɛ
                        for(TerminalSymbol t: getFirsts(grammar, (NonTerminalSymbol) differentProductions.get(++pointer))){
                            addFirst(firs, t);
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

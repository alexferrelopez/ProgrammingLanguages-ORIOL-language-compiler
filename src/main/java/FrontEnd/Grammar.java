package frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Grammar {
    private final String GRAMMAR_PATH = "src/main/resources/gramatica.txt";
    /**
     * Our structure to store the whole grammar.
     * It is a HashMap where its Key will be each non-terminal and the Value will be the different productions from that non-terminal.
     * To store the different productions we are using a double LinkedList.
     *      * The first one will store the different productions each non-terminal can lead to.
     *      * And the second list will be each of the Terminals and Non-Terminals that each particular production can result in.
     */
    private final Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammar = new HashMap<>();
    private final List<NonTerminalSymbol> noTerminalList = new LinkedList<>();

    public Map<NonTerminalSymbol, List<List<AbstractSymbol>>> getGrammar(){
        return this.grammar;
    }

    public Grammar() {
        this.readGrammar();
    }

    /**
     * Casts a String like <name> to a NoTerminal Type
     * @param text String to convert to NoTerminal
     * @param isFirst if it's the first means is the axioma
     * @return the NoTerminal
     */
    private NonTerminalSymbol getNoTerminal(String text, boolean isFirst){
        text = text.trim();
        text = text.replaceAll("<", "");
        text = text.replaceAll(">", "");
        return findNoTerminal(text, isFirst);
    }

    /**
     * Returns a NoTerminal given a string
     * @param noTerm NoTerminal to return
     * @param isFirst if it is the axioma
     * @return the NoTerminal
     */
    private NonTerminalSymbol findNoTerminal(String noTerm, boolean isFirst){
        for(NonTerminalSymbol nt: noTerminalList){
            if(nt.getName().equals(noTerm)){
                return nt;
            }
        }
        NonTerminalSymbol nt = new NonTerminalSymbol(noTerm, isFirst);
        noTerminalList.add(nt);
        return nt;
    }

    /**
     * get the production of each NoTerminal from our gramamr
     * @param n NoTerminal to find it's producion
     * @param text Each line of our grammar file
     */
    private void getDreta(NonTerminalSymbol n, String text){
        text = text.trim();
        int punter = 0;
        char[] charArray = text.toCharArray();
        boolean esNoTerm = false;
        boolean esTerm = false;
        StringBuilder sbNOterm = new StringBuilder();
        StringBuilder sbTerm = new StringBuilder();
        grammar.get(n).add(new LinkedList<>());
        for (char c:charArray) { //Loop each character of the line
            if(esNoTerm){
                if(c != '>'){ //If its no terminal and the current character is not '>' means the character is part of the no terminal
                    sbNOterm.append(c);
                }else{ //Once we found a '>' means the NoTerminal has ended, and we have to store it in our grammar
                    String noTerm = sbNOterm.toString();
                    NonTerminalSymbol nt = findNoTerminal(noTerm, false);
                    grammar.get(n).get(punter).add(nt);
                    sbNOterm = new StringBuilder();
                    esNoTerm = false;
                }
            }else{
                if(c == '<'){ //If we found a '<' means a NoTerminal will start
                    esNoTerm = true;
                    if(esTerm){//If we were analyzing a Terminal and we found a '<' means a NoTerminal starts hence the Terminal finishes and we have to store it in our grammar
                        String str = sbTerm.toString();
                        TerminalSymbol t = new TerminalSymbol(str);
                        grammar.get(n).get(punter).add(t);
                        sbTerm = new StringBuilder();
                        esTerm = false;
                    }
                }else if(c == '>'){//If we found a '>' it won't be a NoTerminal no more
                    esNoTerm = false;
                }else if(c == '|'){ //If we found a '>' means we have another production for the same non-terminal
                    grammar.get(n).add(new LinkedList<>());
                    punter++;
                }else if(c == 'ε'){//Is a Epsilon
                    TerminalSymbol epsilon = new TerminalSymbol(String.valueOf(c));
                    grammar.get(n).get(punter).add(epsilon);
                }else if(c == ' '){ //If we found a space and we were analyzing a Terminal it means that the Terminal has ended hence we have to store it
                    if(esTerm){
                        String str = sbTerm.toString();
                        TerminalSymbol t = new TerminalSymbol(str);
                        grammar.get(n).get(punter).add(t);
                        sbTerm = new StringBuilder();
                        esTerm = false;
                    }
                }else{
                    esTerm = true;
                    sbTerm.append(c);
                }
            }
        }
        if(esTerm){//Once we ended looping through each character, we need to know if the last char was part of a Terminal to store it to our grammar
            String str = sbTerm.toString();
            TerminalSymbol t = new TerminalSymbol(str);
            grammar.get(n).get(punter).add(t);
            sbTerm = new StringBuilder();
        }
    }


    public void readGrammar(){
        try{
            File file = new File(GRAMMAR_PATH);
            Scanner sc = new Scanner(file);
            boolean isFirst = true;
            while (sc.hasNextLine()) {
                String data = sc.nextLine();
                String[] hashMapSeparator = data.split("::=");//Since our grammar is in BNF nomenclature, we can split each line through the '::=' string and get the non-terminal on the first position and the productions on the second production
                List<List<AbstractSymbol>> termINoTermList = new LinkedList<>();
                NonTerminalSymbol nt = getNoTerminal(hashMapSeparator[0], isFirst);
                isFirst = false;
                this.grammar.put(nt, termINoTermList);
                getDreta(nt, hashMapSeparator[1]);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public NonTerminalSymbol getAxioma(){
        for(NonTerminalSymbol nt: grammar.keySet()) {
            if(nt.isAxioma()){
                return nt;
            }
        }
        return null;
    }

}

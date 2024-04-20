package FrontEnd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Grammar {
    private final String GRAMMAR_PATH = "gramatica2.txt";
    private HashMap<NoTerminal, LinkedList<LinkedList<TermiNoTerm>>> grammar = new HashMap<>();
    private LinkedList<NoTerminal> noTerminalList = new LinkedList<>();

    public HashMap<NoTerminal, LinkedList<LinkedList<TermiNoTerm>>> getGrammar(){
        return this.grammar;
    }

    public Grammar() {
        this.readGrammar();
    }

    private NoTerminal getNoTerminal(String text, boolean isFirst){
        text = text.trim();
        text = text.replaceAll("<", "");
        text = text.replaceAll(">", "");
        return findNoTerminal(text, isFirst);
    }

    private NoTerminal findNoTerminal(String noTerm, boolean isFirst){
        for(NoTerminal nt: noTerminalList){
            if(nt.getName().equals(noTerm)){
                return nt;
            }
        }
        NoTerminal nt = new NoTerminal(noTerm, isFirst);
        noTerminalList.add(nt);
        return nt;
    }

    private void getDreta(NoTerminal n, String text){
        text = text.trim();
        int punter = 0;
        char[] charArray = text.toCharArray();
        boolean esNoTerm = false;
        boolean esTerm = false;
        StringBuilder sbNOterm = new StringBuilder();
        StringBuilder sbTerm = new StringBuilder();
        grammar.get(n).add(new LinkedList<>());
        for (char c:charArray) {
            if(esNoTerm){
                if(c != '>'){
                    sbNOterm.append(c);
                }else{
                    String noTerm = sbNOterm.toString();
                    NoTerminal nt = findNoTerminal(noTerm, false);
                    grammar.get(n).get(punter).add(nt);
                    sbNOterm = new StringBuilder();
                    esNoTerm = false;
                }
            }else{
                if(c == '<'){
                    esNoTerm = true;
                    if(esTerm){
                        String str = sbTerm.toString();
                        Terminal t = new Terminal(str);
                        grammar.get(n).get(punter).add(t);
                        sbTerm = new StringBuilder();
                        esTerm = false;
                    }
                }else if(c == '>'){
                    esNoTerm = false;
                }else if(c == '|'){
                    grammar.get(n).add(new LinkedList<>());
                    punter++;
                }else if(c == 'ε'){
                    Terminal epsilon = new Terminal(String.valueOf(c));
                    grammar.get(n).get(punter).add(epsilon);
                }else if(c == ' '){
                    if(esTerm){
                        String str = sbTerm.toString();
                        Terminal t = new Terminal(str);
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
        if(esTerm){
            String str = sbTerm.toString();
            Terminal t = new Terminal(str);
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
                String[] hashMapSeparator = data.split("::=");
                LinkedList<LinkedList<TermiNoTerm>> termINoTermList = new LinkedList<>();
                NoTerminal nt = getNoTerminal(hashMapSeparator[0], isFirst);
                isFirst = false;
                this.grammar.put(nt, termINoTermList);
                getDreta(nt, hashMapSeparator[1]);

                //System.out.println("Esquera: " + f[0] + " Dreta: " + f[1]);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}

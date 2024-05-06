package frontEnd.sintaxis;

import errorHandlers.SyntacticErrorHandler;
import frontEnd.exceptions.InvalidFileException;
import frontEnd.exceptions.InvalidTokenException;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.Grammar;
import frontEnd.sintaxis.grammar.derivationRules.*;
import frontEnd.sintaxis.Tree;

import java.util.*;

public class RecursiveDescentLLParser implements SyntacticAnalyzerInterface {
    private final LexicalAnalyzerInterface lexicalAnalyzer;
    private final SyntacticErrorHandler errorHandler;

    private Token lookahead;

    private Tree tree;
    private Stack<AbstractSymbol> startTokensStck = new Stack<>();//Another stack to store the symbols of the tree that we weill need to retrieve later for the tree
    private String[] startTokens = new String[]{"program", "func_decl", "return_stmt", "declaration", "condition","loop_for"}; //Tokens that we will use to set the start of the tree


    public RecursiveDescentLLParser(LexicalAnalyzerInterface lexicalAnalyzer, SyntacticErrorHandler parserErrorHandler) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.errorHandler = parserErrorHandler;
    }

    /**
     * This method starts the lexical, syntactic and semantic analysis of the code.
     */
    @Override
    public void parseProgram() {
        Grammar grammar = new Grammar();
        Map<NonTerminalSymbol, List<List<AbstractSymbol>>> grammarMap;
        grammarMap = grammar.getGrammar();//Read and load the grammar

        ParsingTable parsingTable = new ParsingTable(grammarMap);//Create and fill the parsing table with our grammar

        Stack<AbstractSymbol> stack = new Stack<>();
        NonTerminalSymbol axioma = grammar.getAxioma();
        tree = new Tree<AbstractSymbol>(axioma);//Create the tree with the axioma as the root
        if (Objects.isNull(axioma)) {
            //TODO throw an exception
        } else {
            stack.push(new TerminalSymbol("EOF")); //Push the $ and the axioma to the stack
            stack.push(axioma);
            startTokensStck.push(axioma);
        }
        try {
            lexicalAnalyzer.startLexicalAnalysis();
            lookahead = lexicalAnalyzer.getNextToken();
            System.out.println("Stack: " + stack);
            while (!stack.empty()) {
                AbstractSymbol symbol = stack.pop();
                if (symbol.isTerminal()) { //If the symbol is a terminal we have to match it with the lookahead
                    match((TerminalSymbol) symbol);
                    if (symbol.getName().equals("EOF") && lookahead.getLexeme().equals("EOF")) { //if both are EOF we have finished :D
                        System.out.println("ACCEPT");
                    }
                } else {
                    List<AbstractSymbol> output = parsingTable.getProduction((NonTerminalSymbol) symbol, lookahead); //Retrieve the predicted production
                    if (Objects.isNull(output)) {
                        System.out.println("Error gramatical"); //TODO throw exception
                        break;
                    }
                    for (int i = output.size() - 1; i >= 0; i--) { //Push the production to the stack unless it is epsilon
                        if (!output.get(i).getName().equals(TerminalSymbol.EPSILON)) {
                            stack.push(output.get(i));
                            if(Arrays.asList(startTokens).contains(((AbstractSymbol)output.get(i)).getName())){
                                startTokensStck.push(output.get(i));
                            }
                        }
                    }


                    //If any of the children of the actual node of the tree is different from the symbol that we are
                    // analyzing we have to go up in the tree
                    if(!((NonTerminalSymbol) tree.getNode()).getName().equals(symbol.getName())){
                        LinkedList<Tree<AbstractSymbol>> children = tree.getChildren();
                        boolean found = false;
                        do{
                            for(Tree child: children){//Find if any of the children of the actual node is the symbol that we are analyzing
                                if(((AbstractSymbol)child.getNode()).getName().equals(symbol.getName())){
                                    tree = child;
                                    found = true;
                                    break;
                                }
                            }
                            if(!found){//If none of the children is the symbol that we are analyzing we go up in the tree
                                if(!Objects.isNull(tree.getParent())){
                                    tree = (Tree) tree.getParent();
                                    children = tree.getChildren();
                                }
                            }
                        }while (!found);//We sholud always find the symbol that we are analyzing. Gramatical error if we don't
                    }
                    //Once we found the symbol that we are analyzing we add the children to the tree
                    for(AbstractSymbol as: output){
                        tree.addChild(as);
                        if(as.getName().equals(TerminalSymbol.EPSILON)){//If the children is epsilon we have to go up in the tree
                            if(!Objects.isNull(tree.getParent())){
                                tree = (Tree) tree.getParent();
                            }

                        }
                    }
                }
                System.out.println("Stack: " + stack);
            }

        } catch (InvalidFileException | InvalidTokenException invalidFile) {
            invalidFile.printStackTrace();
        }

        //Display the firsts and follows of the grammar for debugging purposes
        //First.displayAllFirsts(grammarMap);
        //Follow.displayAllFollows(grammarMap);
    }


    /**
     * This method checks if the lookahead is the same as the terminal symbol
     * @param terminal the terminal symbol to compare
     */
    private void match(TerminalSymbol terminal) {
        if(terminal.getName().equals(String.valueOf(lookahead.getType()))){
            System.out.println("MATCH: " + terminal.getName());
            if(terminal.getName().equals("PUNT_COMMA") || terminal.getName().equals("CT")){//If we ended a sentence or a block of code
                System.out.println("\n\n-----------------TREE-----------------");
                Tree parent = (Tree) tree.getParent();
                String nodeName = ((AbstractSymbol)parent.getNode()).getName();
                AbstractSymbol symbolToSend = startTokensStck.pop();
                while (!symbolToSend.getName().equals(nodeName) //Find the root of the tree to send it
                ){
                    parent = (Tree) parent.getParent();
                    nodeName = ((AbstractSymbol)parent.getNode()).getName();
                }
                parent.removeParent();//Crec que s'ha de treure el parent perquè el rebeu com si fos l'arrel de l'arbre
                //printTree(parent);//TODO send this tree to the lexical analyzer
            }
            try {
                lookahead = lexicalAnalyzer.getNextToken();
            } catch (InvalidTokenException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("ERROR NO MATCH between " + terminal.getName() + " and " + lookahead.getType() + " :(");
        }
    }
}
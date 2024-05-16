package frontEnd.sintaxis;

import debug.PrettyPrintTree;
import errorHandlers.SyntacticErrorHandler;
import frontEnd.exceptions.InvalidFileException;
import frontEnd.exceptions.InvalidTokenException;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.lexic.dictionary.Token;
import frontEnd.lexic.dictionary.TokenType;
import frontEnd.lexic.dictionary.tokenEnums.ReservedSymbol;
import frontEnd.semantics.SemanticAnalyzer;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.Grammar;
import frontEnd.sintaxis.grammar.derivationRules.*;

import java.util.*;

public class RecursiveDescentLLParser implements SyntacticAnalyzerInterface {
    private final LexicalAnalyzerInterface lexicalAnalyzer;
    private final SyntacticErrorHandler errorHandler;

    private Token lookahead;

    private Tree<AbstractSymbol> tree;
    private Stack<AbstractSymbol> startTokensStack = new Stack<>();//Another stack to store the symbols of the tree that we weill need to retrieve later for the tree
    private String[] startTokens = new String[]{"func_type", "return_stmt", "declaration", "condition", "ELSE", "loop_for", "loop_while"}; //Tokens that we will use to set the start of the tree


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
        tree = new Tree<>(axioma);//Create the tree with the axioma as the root
        if (Objects.isNull(axioma)) {
            //TODO throw an exception (mai passarà)
        } else {
            stack.push(new TerminalSymbol("EOF")); //Push the $ and the axioma to the stack
            stack.push(axioma);
            startTokensStack.push(axioma);
        }
        try {
            lexicalAnalyzer.startLexicalAnalysis();
            lookahead = lexicalAnalyzer.getNextToken();
            //System.out.println("Stack: " + stack);
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

                        Tree treeCopy = new Tree(tree);
                        @SuppressWarnings("unchecked")
                        Stack<AbstractSymbol> stackCopy = (Stack<AbstractSymbol>) stack.clone();
                        stackCopy.push(symbol);
                        @SuppressWarnings("unchecked")
                        Stack<AbstractSymbol> startTokensStackCopy = (Stack<AbstractSymbol>) startTokensStack.clone();

                        //do{
                            System.out.println("Error gramatical"); //TODO error recovery
                            //Comparar lookahead amb follow de arbre. Si no esta pujar per el arbre
                            NonTerminalSymbol nt =  Grammar.getNoTerminal(grammarMap, (NonTerminalSymbol) tree.getNode());
                            List<TerminalSymbol> follows = Follow.getFollows(grammarMap, nt);

                            while (! Follow.containsToken(follows, lookahead.getType().toString())) {
                                tree = tree.getParent();
                                nt =  Grammar.getNoTerminal(grammarMap, (NonTerminalSymbol) tree.getNode());
                                follows = Follow.getFollows(grammarMap, nt);
                            }
                            while (!symbol.isTerminal()) {
                                symbol = stack.pop();
                            }
                            symbol = stack.pop();
                            startTokensStack.pop();


                            output = parsingTable.getProduction((NonTerminalSymbol) symbol, lookahead); //Retrieve the predicted production
                        //}while (output == null);




                        if(output == null){
                            tree = new Tree<>(treeCopy);
                            startTokensStack = (Stack<AbstractSymbol>) startTokensStackCopy.clone();
                            stack = (Stack<AbstractSymbol>) stackCopy.clone();
                            symbol = stack.pop();
                            do{
                                lookahead = lexicalAnalyzer.getNextToken();
                                output = parsingTable.getProduction((NonTerminalSymbol) symbol, lookahead); //Retrieve the predicted production
                            }while (output == null);
                        }


                        System.out.println("Lookahead: " + lookahead);
                    }
                    //Get the unique symbols of the production (without same reference)
                    List<AbstractSymbol> newOutput = getUniqueReferenceSymbols(output);

                    for (int i = newOutput.size() - 1; i >= 0; i--) { //Push the production to the stack unless it is epsilon
                        if (!newOutput.get(i).getName().equals(TerminalSymbol.EPSILON)) {
                            stack.push(newOutput.get(i));
                            if(Arrays.asList(startTokens).contains(newOutput.get(i).getName())){
                                startTokensStack.push(newOutput.get(i));
                            }
                        }
                    }


                    //If any of the children of the actual node of the tree is different from the symbol that we are
                    // analyzing we have to go up in the tree
                    if(!tree.getNode().getName().equals(symbol.getName()) || !tree.getChildren().isEmpty() ){
                        List<Tree<AbstractSymbol>> children = tree.getChildren();
                        boolean found = false;
                        do{
                            for(Tree<AbstractSymbol> child: children){//Find if any of the children of the actual node is the symbol that we are analyzing
                                if( child.getChildren().isEmpty() && child.getNode().getName().equals(symbol.getName())){
                                    tree = child;
                                    found = true;
                                    break;
                                }
                            }
                            if(!found){//If none of the children is the symbol that we are analyzing we go up in the tree
                                if(!Objects.isNull(tree.getParent())){
                                    tree = tree.getParent();
                                    children = tree.getChildren();
                                }
                            }
                        }while (!found);//We sholud always find the symbol that we are analyzing. Gramatical error if we don't
                    }
                    //Once we found the symbol that we are analyzing we add the children to the tree
                    for(AbstractSymbol as: newOutput){
                        tree.addChild(as);
                        if(as.getName().equals(TerminalSymbol.EPSILON)){//If the children is epsilon we have to go up in the tree
                            if(!Objects.isNull(tree.getParent())){
                                tree = tree.getParent();
                            }
                        }
                    }
                }
                System.out.println("Stack: " + stack);
            }
            //Go to the root of the tree
            while(!Objects.isNull(tree.getParent())){
                tree = tree.getParent();
            }
            //TODO: send full tree to T@C
            printTree(tree);




        } catch (InvalidFileException | InvalidTokenException invalidFile) {
            invalidFile.printStackTrace();
        }

        //Display the firsts and follows of the grammar for debugging purposes
        //First.displayAllFirsts(grammarMap);
        //Follow.displayAllFollows(grammarMap);
    }

    /**
     * Obtain a list of unique reference symbols from the output of the parsing table
     * @param output The output of the parsing table
     * @return new list of unique reference symbols
     */
    private List<AbstractSymbol> getUniqueReferenceSymbols(List<AbstractSymbol> output) {
        List<AbstractSymbol> newOutput = new LinkedList<>();
        for(AbstractSymbol as: output){
            if(as.isTerminal()) {
                newOutput.add(new TerminalSymbol(as.getName()));
            } else {
                newOutput.add(new NonTerminalSymbol(as.getName()));
            }
        }
        return newOutput;
    }


    /**
     * This method checks if the lookahead is the same as the terminal symbol
     * @param terminal the terminal symbol to compare
     */
    private void match(TerminalSymbol terminal) {
        if(terminal.getName().equals(String.valueOf(lookahead.getType()))){
            System.out.println("MATCH: " + terminal.getName());
            terminal.setToken(lookahead);
            if(terminal.getName().equals("PUNT_COMMA") || terminal.getName().equals("CO")|| terminal.getName().equals("CT")){//If we ended a sentence or a block of code
                System.out.println("\n\n-----------------TREE-----------------");
                Tree<AbstractSymbol> parent = tree.getParent();
                String nodeName = (parent.getNode()).getName();
                AbstractSymbol symbolToSend = startTokensStack.peek();
                if(symbolToSend.getName().equals("ELSE")){
                    if(terminal.getName().equals("CT")){
                        parent = new Tree<>(terminal);
                    }else{
                        Token elseToken = new Token( ReservedSymbol.ELSE);
                        TerminalSymbol elseSymbol = new TerminalSymbol("ELSE");
                        elseSymbol.setToken(elseToken);
                        parent = new Tree<>(elseSymbol);
                    }
                }else{
                    while (!symbolToSend.getName().equals(nodeName) //Find the root of the tree to send it
                    ){
                        parent = parent.getParent();
                        nodeName = (parent.getNode()).getName();
                    }
                    if(terminal.getName().equals("CT")){
                        parent = new Tree<>(terminal);
                        startTokensStack.pop();
                    }
                    if(terminal.getName().equals("PUNT_COMMA")){
                        startTokensStack.pop();
                    }
                    //printTree(parent);//TODO send this tree to the semantical analyzer
                }
                    SemanticAnalyzer.sendTree(parent);
            }
            try {
                lookahead = lexicalAnalyzer.getNextToken();
            } catch (InvalidTokenException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("ERROR NO MATCH between " + terminal.getName() + " and " + lookahead.getType() + " :(");
            // TODO: Error recovery (get token until follow). If there is no match, check it's EOF.
            //Crec que el error recovery no va aqui
        }
    }

    private void printTree(Tree<AbstractSymbol> tree) {
        PrettyPrintTree<Tree<AbstractSymbol>> printTree = new PrettyPrintTree<>(
                Tree::getChildren,
                Tree::getNode
        );

        printTree.display(tree);
    }

    public Tree<AbstractSymbol> getTree() {
        return tree;
    }


}
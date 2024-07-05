package frontEnd.sintaxis;

import debug.PrettyPrintTree;
import errorHandlers.SyntacticErrorHandler;
import errorHandlers.errorTypes.SyntacticErrorType;
import frontEnd.exceptions.SemanticException;
import frontEnd.exceptions.lexic.InvalidFileException;
import frontEnd.exceptions.lexic.InvalidTokenException;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.lexic.dictionary.Token;
import frontEnd.semantics.SemanticAnalyzerInterface;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.Grammar;
import frontEnd.sintaxis.grammar.derivationRules.Follow;
import frontEnd.sintaxis.grammar.derivationRules.NonTerminalSymbol;
import frontEnd.sintaxis.grammar.derivationRules.ParsingTable;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.*;

public class RecursiveDescentLLParser implements SyntacticAnalyzerInterface {
    private final LexicalAnalyzerInterface lexicalAnalyzer;
    private final SyntacticErrorHandler errorHandler;

    private final SemanticAnalyzerInterface semanticAnalyzer;
    private final String[] startTokens = new String[]{"func_type", "return_stmt", "declaration", "condition", "ELSE", "loop_for", "loop_while"}; //Tokens that we will use to set the start of the tree
    private Stack<AbstractSymbol> startTokensStack = new Stack<>();//Another stack to store the symbols of the tree that we weill need to retrieve later for the tree
    private Stack<AbstractSymbol> stack = new Stack<>();
    private Token lookahead;

    private Tree<AbstractSymbol> tree;

    public RecursiveDescentLLParser(LexicalAnalyzerInterface lexicalAnalyzer, SyntacticErrorHandler parserErrorHandler, SemanticAnalyzerInterface semanticAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.errorHandler = parserErrorHandler;
        this.semanticAnalyzer = semanticAnalyzer;
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


        NonTerminalSymbol axioma = grammar.getAxioma();
        tree = new Tree<>(axioma);//Create the tree with the axioma as the root
        if (Objects.isNull(axioma)) {
            errorHandler.reportError(SyntacticErrorType.NO_AXIOMA_ERROR, 0, 0, "");
        } else {
            stack.push(new TerminalSymbol("EOF")); //Push the $ and the axioma to the stack
            stack.push(axioma);
            startTokensStack.push(axioma);
        }
        try {
            lexicalAnalyzer.startLexicalAnalysis();
            lookahead = lexicalAnalyzer.getNextToken();
            while (!stack.empty()) {
                AbstractSymbol symbol = stack.pop();
                if (symbol.isTerminal()) { //If the symbol is a terminal we have to match it with the lookahead
                    boolean ok = match((TerminalSymbol) symbol);
                    if (!ok) break;
                } else {
                    List<AbstractSymbol> output = parsingTable.getProductionList((NonTerminalSymbol) symbol, lookahead); //Retrieve the predicted production
                    if (Objects.isNull(output)) {
                        Map<NonTerminalSymbol, List<AbstractSymbol>> outputMap = errorRecovery(symbol, grammarMap, parsingTable, null, 0);
                        if (outputMap == null) {
                            errorHandler.reportError(SyntacticErrorType.UNRECOVERABLE_ERROR, lookahead.getLine(), lookahead.getColumn(), lookahead.getLexeme());
                            return;
                        }

                        List<List<AbstractSymbol>> abstractSymbols = outputMap.values().stream().toList();
                        output = abstractSymbols.get(0);
                        symbol = (AbstractSymbol) outputMap.keySet().toArray()[0];
                    }

                    //Get the unique symbols of the production (without same reference)
                    List<AbstractSymbol> newOutput = getUniqueReferenceSymbols(output);

                    for (int i = newOutput.size() - 1; i >= 0; i--) { //Push the production to the stack unless it is epsilon
                        if (!newOutput.get(i).getName().equals(TerminalSymbol.EPSILON)) {
                            stack.push(newOutput.get(i));
                            if (Arrays.asList(startTokens).contains(newOutput.get(i).getName())) {
                                startTokensStack.push(newOutput.get(i));
                            }
                        }
                    }

                    //If any of the children of the actual node of the tree is different from the symbol that we are
                    // analyzing we have to go up in the tree
                    if (!tree.getNode().getName().equals(symbol.getName()) || !tree.getChildren().isEmpty()) {
                        List<Tree<AbstractSymbol>> children = tree.getChildren();
                        boolean found = false;
                        do {
                            for (Tree<AbstractSymbol> child : children) {//Find if any of the children of the actual node is the symbol that we are analyzing
                                if (child.getChildren().isEmpty() && child.getNode().getName().equals(symbol.getName())) {
                                    tree = child;
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {//If none of the children is the symbol that we are analyzing we go up in the tree
                                if (Objects.isNull(tree.getParent())) break;
                                tree = tree.getParent();
                                children = tree.getChildren();
                            }
                        } while (!found);//We should always find the symbol that we are analyzing. Gramatical error if we don't
                    }
                    //Once we found the symbol that we are analyzing we add the children to the tree
                    for (AbstractSymbol as : newOutput) {
                        tree.addChild(as);
                        if (as.getName().equals(TerminalSymbol.EPSILON)) {//If the children is epsilon we have to go up in the tree
                            if (!Objects.isNull(tree.getParent())) {
                                tree = tree.getParent();
                            }

                        }
                    }
                }
            }
            //Go to the root of the tree
            while (!Objects.isNull(tree.getParent())) {
                tree = tree.getParent();
            }
        } catch (InvalidTokenException e) {

        } catch (InvalidFileException e) {

        }

        //Display the firsts and follows of the grammar for debugging purposes
        //First.displayAllFirsts(grammarMap);
        //Follow.displayAllFollows(grammarMap);
    }

    /**
     * Obtain a list of unique reference symbols from the output of the parsing table
     *
     * @param output The output of the parsing table
     * @return new list of unique reference symbols
     */
    private List<AbstractSymbol> getUniqueReferenceSymbols(List<AbstractSymbol> output) {
        List<AbstractSymbol> newOutput = new LinkedList<>();
        for (AbstractSymbol as : output) {
            if (as.isTerminal()) {
                newOutput.add(new TerminalSymbol(as.getName()));
            } else {
                newOutput.add(new NonTerminalSymbol(as.getName()));
            }
        }
        return newOutput;
    }


    /**
     * This method checks if the lookahead is the same as the terminal symbol
     *
     * @param terminal the terminal symbol to compare
     */
    private boolean match(TerminalSymbol terminal) {
        if (terminal.getName().equals(String.valueOf(lookahead.getType()))) {
            terminal.setToken(lookahead);
            if (terminal.getName().equals("PUNT_COMMA") || terminal.getName().equals("CO") || terminal.getName().equals("CT")) {//If we ended a sentence or a block of code
                Tree<AbstractSymbol> parent = tree.getParent();
                if (Objects.isNull(parent)) return false;
                String nodeName = (parent.getNode()).getName();
                AbstractSymbol symbolToSend = startTokensStack.peek();
                if (symbolToSend.getName().equals("ELSE")) {
                    if (terminal.getName().equals("CT")) {
                        parent = new Tree<>(terminal);
                    } else {
                        parent = new Tree<>(symbolToSend);
                    }
                } else {
                    while (!symbolToSend.getName().equals(nodeName) //Find the root of the tree to send it
                    ) {
                        parent = parent.getParent();
                        if (Objects.isNull(parent)) return false;
                        nodeName = (parent.getNode()).getName();
                    }
                    if (terminal.getName().equals("CT")) {
                        parent = new Tree<>(terminal);
                        startTokensStack.pop();
                    }
                    if (terminal.getName().equals("PUNT_COMMA")) {
                        startTokensStack.pop();
                    }
                }
                try {
                    semanticAnalyzer.checkSyntacticTree(parent);
                } catch (SemanticException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                lookahead = lexicalAnalyzer.getNextToken();
            } catch (InvalidTokenException e) {
                errorHandler.reportError(SyntacticErrorType.UNEXPECTED_TOKEN_ERROR, lookahead.getLine(), lookahead.getColumn(), lookahead.getLexeme());
                return false;
            }
        } else {
            errorHandler.reportError(SyntacticErrorType.UNEXPECTED_TOKEN_ERROR, lookahead.getLine(), lookahead.getColumn(), lookahead.getLexeme());
            return false;
        }
        return true;
    }

    public void printTree(Tree<AbstractSymbol> tree) {
        PrettyPrintTree<Tree<AbstractSymbol>> printTree = new PrettyPrintTree<>(
                Tree::getChildren,
                Tree::getNode
        );

        printTree.display(tree);
    }

    public Tree<AbstractSymbol> getTree() {
        return tree;
    }

    private Map<NonTerminalSymbol, List<AbstractSymbol>> errorRecovery(AbstractSymbol symbol, Map<NonTerminalSymbol,
            List<List<AbstractSymbol>>> grammarMap, ParsingTable parsingTable, Map<NonTerminalSymbol, List<AbstractSymbol>> outputMap, int numRecursions) {
        Tree<AbstractSymbol> treeCopy = new Tree<>(tree);

        Stack<AbstractSymbol> stackCopy = (Stack<AbstractSymbol>) stack.clone();
        stackCopy.push(symbol);

        Stack<AbstractSymbol> startTokensStackCopy = (Stack<AbstractSymbol>) startTokensStack.clone();

        //Metode 1 (Buscar follows)
        //Comparar lookahead amb follow de arbre. Si no esta pujar per el arbre
        NonTerminalSymbol nt = Grammar.getNoTerminal(grammarMap, (NonTerminalSymbol) tree.getNode());
        List<TerminalSymbol> follows = Follow.getFollows(grammarMap, nt);

        try {
            while (!Follow.containsToken(follows, lookahead.getType().toString()) && !Objects.isNull(tree.getParent())) {
                tree = tree.getParent();
                nt = Grammar.getNoTerminal(grammarMap, (NonTerminalSymbol) tree.getNode());
                follows = Follow.getFollows(grammarMap, nt);
            }
            while (!symbol.isTerminal()) {
                symbol = stack.pop();
            }
            symbol = stack.pop();
            startTokensStack.pop();

        } catch (NullPointerException | EmptyStackException ignored) {
        }
        if (!symbol.isTerminal()) {
            outputMap = parsingTable.getProduction((NonTerminalSymbol) symbol, lookahead); //Retrieve the predicted production
        }

        //Mètode 2 (Pillar el next lookahead)
        if (outputMap == null) {
            tree = new Tree<>(treeCopy);
            startTokensStack = (Stack<AbstractSymbol>) startTokensStackCopy.clone();
            stack = (Stack<AbstractSymbol>) stackCopy.clone();
            symbol = stack.pop();

            do {
                boolean lookaheadrror;
                do {
                    try {
                        lookahead = lexicalAnalyzer.getNextToken();
                        lookaheadrror = false;
                    } catch (InvalidTokenException e) {
                        errorHandler.reportError(SyntacticErrorType.UNEXPECTED_TOKEN_ERROR, null, null, "");
                        lookaheadrror = true;
                    }
                } while (lookaheadrror);
                outputMap = parsingTable.getProduction((NonTerminalSymbol) symbol, lookahead); //Retrieve the predicted production

                if (Objects.isNull(outputMap)) {
                    if (numRecursions > 1) {
                        errorHandler.reportError(SyntacticErrorType.UNEXPECTED_TOKEN_ERROR, lookahead.getLine(), lookahead.getColumn(), " before " + lookahead.getLexeme());
                        return null;
                    }
                    outputMap = errorRecovery(symbol, grammarMap, parsingTable, null, ++numRecursions);
                } else {
                    errorHandler.reportError(SyntacticErrorType.UNEXPECTED_TOKEN_ERROR, lookahead.getLine(), lookahead.getColumn(), "before " + lookahead.getLexeme());
                }
                stack = (Stack<AbstractSymbol>) stackCopy.clone();
                stack.pop();
            } while (outputMap == null);
        } else {
            Stack<AbstractSymbol> stackCopy2 = (Stack<AbstractSymbol>) stackCopy.clone();
            AbstractSymbol symbolCopy = stackCopy2.pop();
            while (!symbolCopy.isTerminal()) {
                symbolCopy = stackCopy2.pop();
            }
            errorHandler.reportError(SyntacticErrorType.UNEXPECTED_TOKEN_ERROR, lookahead.getLine(), lookahead.getColumn(), " before " + lookahead.getLexeme());
        }
        return outputMap;
    }
}
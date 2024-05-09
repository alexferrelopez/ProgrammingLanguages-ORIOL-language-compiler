package frontEnd.intermediateCode;

import frontEnd.lexic.dictionary.Token;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import frontEnd.sintaxis.grammar.derivationRules.TerminalSymbol;

import java.util.ArrayList;

import static frontEnd.lexic.dictionary.tokenEnums.DataType.*;
import static frontEnd.lexic.dictionary.tokenEnums.DataType.VOID;
import static frontEnd.lexic.dictionary.tokenEnums.ReservedSymbol.*;
import static frontEnd.lexic.dictionary.tokenEnums.ValueSymbol.*;

public class TACGenerator {
    private TACModule tacModule; // The TACModule to store the generated code

    /**
     * Constructor of the TACGenerator class
     *
     * @param tacModule the TACModule to store the generated code
     */
    public TACGenerator(TACModule tacModule) {
        this.tacModule = tacModule;
    }

    public void generateCode(Tree<Token> tree) {
        // We get an arraylist of tokens from the semantic analyzer
        // This arraylist of tokens represents one line of code
        // The possible first tokens are: data types, reserved words (like: if, while, for, do, return), and variables
        // We need to see if the line of code is an assignment, an if statement, a while statement, etc.


        // Handle unknown token type
        if (token.getType().equals(INTEGER) || token.getType().equals(FLOAT) || token.getType().equals(BOOLEAN) || token.getType().equals(CHAR) || token.getType().equals(STRING) || token.getType().equals(VOID)) {
            // Handle data type declaration

        } else if (tree.getNode().getType().equals(VARIABLE)) {
            // Handle assignment
            handleAssignment(token, tokens);

        } else if (token.getType().equals(IF)) {
            // Handle if statement
        } else if (token.getType().equals(WHILE)) {
            // Handle while loop
            // Add more cases as needed
        } else if (token.getType().equals(ELSE)) {
            // Handle else statement
        } else if (token.getType().equals(FOR)) {
            // Handle for loop
        } else if (token.getType().equals(DO)) {
            // Handle do-while loop
        } else if (token.getType().equals(RETURN)) {
            // Handle return statement
        } else {
            // Handle unknown token type
            // ***** Must not reach this point *****
        }

    }

    private void handleAssignment(Token token, ArrayList<Token> tokens) {
        String variableName = token.getLexeme();
        // We call again the generateCode method to handle the right side of the assignment (he have to skip the variable name and the equal sign)
        generateCode(new ArrayList<>(tokens.subList(2, tokens.size())));

    }
}

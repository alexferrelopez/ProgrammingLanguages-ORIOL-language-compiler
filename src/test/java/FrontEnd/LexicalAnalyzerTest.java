package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.Dictionary.TokenEnums.*;
import FrontEnd.Exceptions.InvalidTokenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class LexicalAnalyzerTest {

    private static Stream<Arguments> provideTokensForTesting() {
        return Stream.of(
				// Simple declaration code statement
                Arguments.of("miau a is 10\n", new Token[] {
                        new Token(DataType.INTEGER),
                        new Token(ValueSymbol.VARIABLE, "a"),
                        new Token(SpecialSymbol.IS),
                        new Token(ValueSymbol.VALUE_INT, "10")
                }),

				// Conditional statement
				Arguments.of(
						"""
							check ( num module 2 eq 0 ) {
								 poop 0 ;
							}
						""", new Token[] {
								new Token(ReservedSymbol.IF),
								new Token(SpecialSymbol.PO),
								new Token(ValueSymbol.VARIABLE, "num"),
								new Token(MathOperator.MOD, "module"),
								new Token(ValueSymbol.VALUE_INT, "2"),
								new Token(BinaryOperator.EQ, "eq"),
								new Token(ValueSymbol.VALUE_INT, "0"),
								new Token(SpecialSymbol.PT),
								new Token(SpecialSymbol.CO),
								new Token(ReservedSymbol.RETURN),
								new Token(ValueSymbol.VALUE_INT, "0"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(SpecialSymbol.CT)
				}),

				// Longer code statement
				Arguments.of(
							"""
							oink : sumRange ( oink numA , oink numB , oink increment ) {
								oink result is 0.0 ;
								miau aAROn is 3 ;
								miau gEmMa is 0 ;
								miau ORIol is 4 ;
								check ( numA bg numB ) {
									oink tmp is numA ;
									numA is numB ;
									numB is tmp ;
								}
								moo c is 'a' ;
								quack text is "hola" ;
								quack text2 is 'adeu' ;
								feed ( oink i is numA to numB , i is i sum increment ) {
									result is result sum i ;
								}
								poop result ;
							}
							miau : ranch ( ) {
								oink a is 3.23 ;
								oink b is 4.5 ;
								oink c is 0.2 ;
								oink total is sumRange ( a , b , c ) ;
								poop 0 ;
							}
						""", new Token[] {
								new Token(DataType.FLOAT),
								new Token(SpecialSymbol.DOS_PUNTS),
								new Token(ValueSymbol.VARIABLE, "sumRange"),
								new Token(SpecialSymbol.PO),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "numA"),
								new Token(SpecialSymbol.COMMA),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "numB"),
								new Token(SpecialSymbol.COMMA),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "increment"),
								new Token(SpecialSymbol.PT),
								new Token(SpecialSymbol.CO),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "result"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_FLOAT, "0.0"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.INTEGER),
								new Token(ReservedSymbol.AARON, "aAROn"),	// Must be defined or will get the lexeme as "(?i)aaron"
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_INT, "3"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.INTEGER),
								new Token(ReservedSymbol.GEMMA, "gEmMa"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_INT, "0"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.INTEGER),
								new Token(ReservedSymbol.ORIOL, "ORIol"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_INT, "4"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(ReservedSymbol.IF),
								new Token(SpecialSymbol.PO),
								new Token(ValueSymbol.VARIABLE, "numA"),
								new Token(BinaryOperator.GT, "bg"),
								new Token(ValueSymbol.VARIABLE, "numB"),
								new Token(SpecialSymbol.PT),
								new Token(SpecialSymbol.CO),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "tmp"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VARIABLE, "numA"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(ValueSymbol.VARIABLE, "numA"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VARIABLE, "numB"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(ValueSymbol.VARIABLE, "numB"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VARIABLE, "tmp"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(SpecialSymbol.CT),
								new Token(DataType.CHAR),
								new Token(ValueSymbol.VARIABLE, "c"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_CHAR, "'a'"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.STRING),
								new Token(ValueSymbol.VARIABLE, "text"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_STRING, "\"hola\""),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.STRING),
								new Token(ValueSymbol.VARIABLE, "text2"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_STRING, "'adeu'"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(ReservedSymbol.FOR),
								new Token(SpecialSymbol.PO),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "i"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VARIABLE, "numA"),
								new Token(ReservedSymbol.TO),
								new Token(ValueSymbol.VARIABLE, "numB"),
								new Token(SpecialSymbol.COMMA),
								new Token(ValueSymbol.VARIABLE, "i"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VARIABLE, "i"),
								new Token(MathOperator.SUM),
								new Token(ValueSymbol.VARIABLE, "increment"),
								new Token(SpecialSymbol.PT),
								new Token(SpecialSymbol.CO),
								new Token(ValueSymbol.VARIABLE, "result"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VARIABLE, "result"),
								new Token(MathOperator.SUM),
								new Token(ValueSymbol.VARIABLE, "i"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(SpecialSymbol.CT),
								new Token(ReservedSymbol.RETURN),
								new Token(ValueSymbol.VARIABLE, "result"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(SpecialSymbol.CT),
								new Token(DataType.INTEGER),
								new Token(SpecialSymbol.DOS_PUNTS),
								new Token(ReservedSymbol.MAIN),
								new Token(SpecialSymbol.PO),
								new Token(SpecialSymbol.PT),
								new Token(SpecialSymbol.CO),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "a"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_FLOAT, "3.23"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "b"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_FLOAT, "4.5"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "c"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VALUE_FLOAT, "0.2"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(DataType.FLOAT),
								new Token(ValueSymbol.VARIABLE, "total"),
								new Token(SpecialSymbol.IS),
								new Token(ValueSymbol.VARIABLE, "sumRange"),
								new Token(SpecialSymbol.PO),
								new Token(ValueSymbol.VARIABLE, "a"),
								new Token(SpecialSymbol.COMMA),
								new Token(ValueSymbol.VARIABLE, "b"),
								new Token(SpecialSymbol.COMMA),
								new Token(ValueSymbol.VARIABLE, "c"),
								new Token(SpecialSymbol.PT),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(ReservedSymbol.RETURN),
								new Token(ValueSymbol.VALUE_INT, "0"),
								new Token(SpecialSymbol.PUNT_COMMA),
								new Token(SpecialSymbol.CT)
						}
				)

                // Add more test cases here
        );
    }

    @ParameterizedTest
    @MethodSource("provideTokensForTesting")
    public void test_tokenizer(String sourceCode, Token[] expectedTokens) {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(sourceCode, true);

        for (Token expectedToken : expectedTokens) {
			try {
				// Check if the read token is the same as the next one.
				Token currentToken = lexicalAnalyzer.getNextToken();
				//Assertions.assertEquals(currentToken.toString(), expectedToken.toString());
				Assertions.assertEquals(expectedToken.getType(), currentToken.getType());
				Assertions.assertEquals(expectedToken.getLexeme(), currentToken.getLexeme());
			} catch (InvalidTokenException e) {
				throw new RuntimeException(e);
			}
        }
    }
}
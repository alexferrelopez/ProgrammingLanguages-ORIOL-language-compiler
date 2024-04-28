package FrontEnd;

import FrontEnd.Dictionary.Token;
import FrontEnd.Dictionary.TokenEnums.*;
import FrontEnd.Exceptions.InvalidFileException;
import FrontEnd.Exceptions.InvalidTokenException;
import jdk.jfr.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

class LexicalAnalyzerTest {

	private final static String TEST_FILE_NAME = "test_sourceCode_tokenizer";
	private final static String TEST_FILE_EXT = ".farm";
	private final static String TEST_FILE_DIR = "java.io.tmpdir";

	private File tempFile;

    private static Stream<Arguments> provideTokensForTesting() {
        return Stream.of(
				// Simple declaration code statement
                Arguments.of("miau a   is 10;\na", new Token[] {
                        new Token(DataType.INTEGER),
                        new Token(ValueSymbol.VARIABLE, "a"),
                        new Token(SpecialSymbol.IS),
                        new Token(ValueSymbol.VALUE_INT, "10"),
						new Token(SpecialSymbol.PUNT_COMMA),
						new Token(ValueSymbol.VARIABLE, "a"),
                }),

				// Simple declaration code statement
				Arguments.of("{(})}; ; {(})}; \n ", new Token[] {
						new Token(SpecialSymbol.CO),
						new Token(SpecialSymbol.PO),
						new Token(SpecialSymbol.CT),
						new Token(SpecialSymbol.PT),
						new Token(SpecialSymbol.CT),
						new Token(SpecialSymbol.PUNT_COMMA),
						new Token(SpecialSymbol.PUNT_COMMA),
						new Token(SpecialSymbol.CO),
						new Token(SpecialSymbol.PO),
						new Token(SpecialSymbol.CT),
						new Token(SpecialSymbol.PT),
						new Token(SpecialSymbol.CT),
						new Token(SpecialSymbol.PUNT_COMMA),
				}),

				// Conditional statement
				Arguments.of(
						"""
							check (num module 2 eq 0 ) {
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

	@BeforeEach
	// Create a file a private constant.
	void setUp() throws IOException {
		// Create a temporary file before each test (prefix, suffix, and directory for the temp file)
		tempFile = File.createTempFile(TEST_FILE_NAME, TEST_FILE_EXT, new File(System.getProperty(TEST_FILE_DIR)));

		// Ensure the file is initially empty and ready for use
		if (tempFile.exists()) {
			tempFile.delete();
		}
		tempFile.createNewFile();
	}

	@AfterEach
	// Clear the file and remove it.
	void tearDown() {
		// Delete the temporary file after each test
		if (tempFile.exists()) {
			tempFile.delete();
		}
	}

    @ParameterizedTest
    @MethodSource("provideTokensForTesting")
	@DisplayName("Tokenizer: Convert words into tokens.")
	@Description("Test that all the words from the source code file are properly converted into a Token.")
    public void test_tokenizeWords(String sourceCode, Token[] expectedTokens) {
		try {
			// Write the source code to the file
			try (FileWriter writer = new FileWriter(tempFile)) {
				writer.write(sourceCode);
			}

			// Now pass the file to the LexicalAnalyzer
			LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(tempFile.getAbsolutePath());
			lexicalAnalyzer.openCodeFile();

			for (Token expectedToken : expectedTokens) {
				// Check if the read token is the same as the next one.
				Token currentToken = lexicalAnalyzer.getNextToken();
				Assertions.assertEquals(expectedToken.getType(), currentToken.getType(), "The token type must be the same.");
				Assertions.assertEquals(expectedToken.getLexeme(), currentToken.getLexeme(), "The lexeme must be the same.");
			}
		} catch (IOException | InvalidTokenException | InvalidFileException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Check file non-existence handling.")
	@Description("Test that checks if a file that does not exist is handled properly.")
	public void test_checkFileNotExists() {
		LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer("fileDoesNotExist.tmp");

		// Asserts that the operation throws the specified exception
		Assertions.assertThrows(InvalidFileException.class, lexicalAnalyzer::openCodeFile, "LexicalAnalyzer should throw InvalidFileException for non-existent files.");
	}

	@Test
	@DisplayName("Check file existence handling.")
	@Description("Test that checks if a file that exists is handled properly.")
	public void test_checkFileExists() {
		LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(tempFile.getAbsolutePath());	// tempFile is created always before the test (@BeforeEach)

		// Asserts that the operation does not throw any exception
		Assertions.assertDoesNotThrow(lexicalAnalyzer::openCodeFile, "LexicalAnalyzer should not throw any exception for existent files.");
	}
}
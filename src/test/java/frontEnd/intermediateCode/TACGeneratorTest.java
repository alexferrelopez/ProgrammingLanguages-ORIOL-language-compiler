package frontEnd.intermediateCode;

import errorHandlers.LexicalErrorHandler;
import errorHandlers.SemanticErrorHandler;
import errorHandlers.SyntacticErrorHandler;
import frontEnd.lexic.LexicalAnalyzer;
import frontEnd.lexic.LexicalAnalyzerInterface;
import frontEnd.semantics.SemanticAnalyzer;
import frontEnd.semantics.SemanticAnalyzerInterface;
import frontEnd.semantics.symbolTable.SymbolTableInterface;
import frontEnd.semantics.symbolTable.SymbolTableTree;
import frontEnd.sintaxis.RecursiveDescentLLParser;
import frontEnd.sintaxis.Tree;
import frontEnd.sintaxis.grammar.AbstractSymbol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TACGeneratorTest {

    private final static String TEST_FILE_FOLDER = "src/test/resources/";
    private final static String TEST_FILE_RESULT_FOLDER = TEST_FILE_FOLDER + "tac_results/";

    //---------------------Exemples for testing-------------------------//
    //ONLY MODIFY THIS SECTION. THE EXAMPLE AND THE RESULT MUST HAVE THE SAME BASE NAME.
    private final String EXEMPLE_1 = "ExempleProgram";

    //---------------------Paths for testing----------------------------//
    private final String CODE_PATH_1 = TEST_FILE_FOLDER + EXEMPLE_1 + ".farm";

    //---------------------Paths for results----------------------------//
    private final String RESULT_PATH_1 = TEST_FILE_RESULT_FOLDER + EXEMPLE_1 + "TAC.csv";


    private RecursiveDescentLLParser parser;
    private LexicalAnalyzerInterface lexicalAnalyzer;
    private SyntacticErrorHandler errorHandler;
    private SemanticAnalyzerInterface semanticAnalyzer;
    private SemanticErrorHandler semanticErrorHandler;
    private SymbolTableInterface symbolTable;
    private TACGenerator tacGenerator;
    private LinkedList<TACInstruction> tacResultList;

    private void setupCompiler(String filePath) {
        errorHandler = new SyntacticErrorHandler();
        lexicalAnalyzer = new LexicalAnalyzer(filePath, new LexicalErrorHandler());
        semanticErrorHandler = new SemanticErrorHandler();
        symbolTable = new SymbolTableTree();
        semanticAnalyzer = new SemanticAnalyzer(semanticErrorHandler, symbolTable);
        parser = new RecursiveDescentLLParser(lexicalAnalyzer, errorHandler, semanticAnalyzer);
        tacGenerator = new TACGenerator(new TACModule(),new SymbolTableTree());
    }

    private void readTACResult(String path) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(path));
        while(sc.hasNext()) {
            String s = sc.nextLine();
            List<String> segmentedList = Arrays.asList(s.replace("=", "").trim().split("\\s+"));

            if(segmentedList.size() > 2) {
                tacResultList.add(new TACInstruction(segmentedList.get(0),segmentedList.get(1),segmentedList.get(2),segmentedList.get(3)));
            }else if(segmentedList.size() > 1) {
                tacResultList.add(new TACInstruction(segmentedList.get(0),segmentedList.get(1)));
            }else{
                tacResultList.add(new TACInstruction("",segmentedList.get(0)));
            }
        }
    }

    private void readTACResultCSV(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = br.readLine();
        tacResultList = new LinkedList<>();

        // Read header line to determine column positions
        String[] headers = line.split(";");
        Map<String, Integer> columnMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            columnMap.put(headers[i], i);
        }


        // Read and process each line
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(";");
            Map<String, String> params = new HashMap<>();

            for (String key : columnMap.keySet()) {
                int index = columnMap.get(key);
                if (index < parts.length && !parts[index].isEmpty()) {
                    params.put(key, parts[index]);
                } else {
                    params.put(key, null);  // Handle missing columns
                }
            }
            if(params.get("operator").equals("Return")){
                params.put("result","");
            }
            TACInstruction instruction = new TACInstruction(params.get("result"),params.get("operator"),params.get("operand1"),params.get("operand2"));
            tacResultList.add(instruction);
        }
        br.close(); //closes the scanner
    }


    public boolean compareInstructions(TACInstruction ins1,TACInstruction ins2) {
        return ins2.toString().equals(ins1.toString());
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void test_generateTAC() {

        try {
            readTACResultCSV(RESULT_PATH_1);
            setupCompiler(CODE_PATH_1);
            //Tree<AbstractSymbol> tree=new Tree<AbstractSymbol>();
            parser.parseProgram();


            // *** Intermediate Code *** //
            Tree<AbstractSymbol> tree = parser.getTree();
            parser.printTree(tree);
            List<TACInstruction> myList =  tacGenerator.generateTAC(tree);
            int i = 0;
            while(i < myList.size()) {
                TACInstruction inst1 = myList.get(i);
                TACInstruction inst2 = tacResultList.get(i);
                if(!compareInstructions(inst1,inst2)) {
                    break;
                }
                i++;
            }

            Assertions.assertEquals(i,myList.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void test_printTAC() {
    }
}
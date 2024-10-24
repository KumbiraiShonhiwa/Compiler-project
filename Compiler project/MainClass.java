


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MainClass {
  public static void main(String[] args) {
    // Define the path to the XML file in the project directory
    String inputFile = "Compiler project//input8.txt"; // Adjust this path as per your project structure
    String xmlOutputFile = "tokens_output.xml";
    String xmlOutputFileSyntaxTree = "syntax_tree.xml";
    try {
        // Step 1: Read input file
        String input = new String(Files.readAllBytes(Paths.get(inputFile)));
        System.out.println("Input file contents:\n" + input);

        // Step 2: Lexing process - generate tokens
        List<Token> tokens = RecSPLLexer.lex(input);
        // for (int i = 0; i < tokens.size(); i++) {
        //     System.out.println(tokens.get(i));
        // }
        System.out.println("Lexing completed successfully. No errors found.");
        // Step 3: Parsing process        
        Node rootNode = new Node(0, "ROOT", null);
        RecSPLParser parser = new RecSPLParser(tokens, rootNode); // Pass tokens to the parser

        parser.parseProgram(); // Start parsing the 
        System.out.println("Parsing completed successfully. No syntax errors found.");
        // parser.functionTable.forEach((key, value) -> {
        //     System.out.println(key + " : " + value);
        //     System.out.println(value.getParamTypes());
        // });
        // Step 4: Semantic Analysis of functions
        Node functionNode = new Node(0, "ROOT", null);
        RecSPLParser parserFunction = new RecSPLParser(parser, functionNode);
        parserFunction.darkart = "notdarkart";
        parserFunction.parseProgram();
        // parserFunction.functionTable.forEach((key, value) -> {
        //     System.out.println(key + " : " + value);
        //     System.out.println(value.getParamTypes());
        // });
        System.out.println("Semantic Analysis completed successfully. No syntax errors found.");
        // Output the syntax tree
        RecSPLLexer.writeTokensToXML(tokens, xmlOutputFile);
        parser.syntaxTree.toXML(xmlOutputFileSyntaxTree);
        // System.out.println(syntaxTreeXML);
        System.out.println("Syntax tree output to " + xmlOutputFileSyntaxTree);
       // Step 5: Typechecking
        System.out.println("call typechecker ");
        // RecSPLTypeCheckerA typeChecker = new RecSPLTypeCheckerA(parser.symbolTable);

        // if (typeChecker.typecheck(parser.syntaxTree))
        // {
        //     System.out.println("Type checking successful");
        // }
        // else
        // {
        //     System.out.println("Type checking unsuccessful");
        // }

        // Step 6: Code Generation
        CodeGenerator codeGenerator = new CodeGenerator(tokens, parserFunction.symbolTable, parserFunction.functionTable);
        String code = codeGenerator.translate();
        codeGenerator.writeFormattedCodeToFile(code);
        ConvertToBasic converter = new ConvertToBasic();
        converter.convertToBasic("output.txt", "basic_output.bas");

    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
}
}

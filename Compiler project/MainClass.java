import java.nio.file.*;
import java.util.List;

public class MainClass {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar CompilerProject.jar <inputFile> <outputFile>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            // Step 1: Read input file
            String input = new String(Files.readAllBytes(Paths.get(inputFile)));
            System.out.println("Input file contents:\n" + input);
            

            // Step 2: Lexing process - generate tokens
            List<Token> tokens = RecSPLLexer.lex(input);
            RecSPLLexer.writeTokensToXML(tokens, "tokens_output.xml");
            System.out.println("Lexing completed successfully. No errors found.");

            // Step 3: Parsing process
            Node rootNode = new Node(0, "ROOT", null);
            RecSPLParser parser = new RecSPLParser(tokens, rootNode);
            parser.parseProgram();
            parser.syntaxTree.toXML("syntax_tree.xml");
            System.out.println("Parsing completed successfully. No syntax errors found.");

            // Step 4: Semantic Analysis
            Node functionNode = new Node(0, "ROOT", null);
            RecSPLParser parserFunction = new RecSPLParser(parser, functionNode);
            parserFunction.parseProgram();
            System.out.println("Semantic Analysis completed successfully.");

            // Step 5: Typechecking
            RecSPLTypeChecker typeChecker = new RecSPLTypeChecker(parser.symbolTable);
            if (typeChecker.check(tokens)) {
                System.out.println("Type checking successful.");
                CodeGenerator codeGenerator = new CodeGenerator(tokens, parserFunction.symbolTable, parserFunction.functionTable);
                String code = codeGenerator.translate();
                codeGenerator.writeFormattedCodeToFile(outputFile);
                System.out.println("Code written to " + outputFile);
    
                // Optional: Convert to BASIC format (if needed)
                ConvertToBasic converter = new ConvertToBasic();
                converter.convertToBasic(outputFile, "basic_output.bas");
                System.out.println("BASIC code output to basic_output.bas");
            } else {
                System.out.println("Type checking unsuccessful.");
            }

            // Step 6: Code Generation
            CodeGenerator codeGenerator = new CodeGenerator(tokens, parserFunction.symbolTable, parserFunction.functionTable);
            String code = codeGenerator.translate();
            codeGenerator.writeFormattedCodeToFile(outputFile);
            System.out.println("Code written to " + outputFile);

            // Optional: Convert to BASIC format (if needed)
            ConvertToBasic converter = new ConvertToBasic();
            converter.convertToBasic(outputFile, "basic_output.bas");
            // System.out.println("BASIC code output to basic_output.bas");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

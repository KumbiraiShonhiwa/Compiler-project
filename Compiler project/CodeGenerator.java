
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

public class CodeGenerator {

    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private Map<String, FunctionSignature> functionTable;
    private int currentTokenIndex;

    public CodeGenerator(List<Token> tokens, SymbolTable symbolTable, Map<String, FunctionSignature> functionTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
        this.currentTokenIndex = 0; // Start at the beginning of the token list
        this.functionTable = functionTable;
    }

    public String translate() {
        return translationPROG();
    }

    private Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        } else {
            return null; // End of input
        }

    }

    private String translationPROG() {
        // Assume the first token is 'main'
        match(TokenType.MAIN); // Consume 'main'
        translationGLOBVARS();
        String algoCode = translationALGO();
        String functionsCode = translateFUNCTIONS();
        return algoCode + " STOP " + functionsCode; // Append STOP after ALGO
    }

    private String translationGLOBVARS() {
        // Initialize the token iterator to read through GLOBVARS
        Token currentToken = getCurrentToken();

        // Process the tokens that make up the global variable declarations
        while (currentToken != null && isGlobalVarToken(currentToken)) {
            // Skip the global variable declaration or register it for scope analysis
            // This could involve adding it to a symbol table for later reference if needed.
            processGlobalVariable(currentToken);

            // Move to the next token
            currentToken = getCurrentToken();
        }

        // After processing, return an empty string since GLOBVARS are ignored in the output
        return "";
    }

    // Helper method to check if a token is part of a global variable declaration
    private boolean isGlobalVarToken(Token token) {
        // Implement the logic to identify global variable tokens, such as type, name, etc.
        // This could involve checking if the token is of a certain type (e.g., int, string)
        if (token.isType(TokenType.NUM) == true) {
            match(token.type);
            Token currentToken = getCurrentToken();
            if (currentToken.isType(TokenType.VNAME) == true) {
                match(currentToken.type);
                currentToken = getCurrentToken();
                if (currentToken.isType(TokenType.COMMA) == true) {
                    match(currentToken.type);
                    translationGLOBVARS();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    // Helper method to process global variable declarations for scope analysis
    private void processGlobalVariable(Token token) {
        // Register the global variable in the symbol table or perform any necessary analysis
        symbolTable.add(token.getName(), token.getType().toString());
    }

    private String translationALGO() {
        match(TokenType.BEGIN); // Consume 'begin'
        String instrucCode = translationINSTRUC();
        match(TokenType.END); // Consume 'end'
        return instrucCode; // Return the instruction code
    }

    private String translationINSTRUC() {
        // This method should handle INSTRUC, which can be empty
        String code = "";
        if (getCurrentToken() == null) {
            return "REM END ";
        } else {
            code = translationCOMMAND();
            Token currentToken = getCurrentToken();
            if (currentToken != null && currentToken.type == TokenType.SEMICOLON) {
                match(TokenType.SEMICOLON); // Consume ';'
                code += "\n" + translationINSTRUC(); // Append the next instruction
            }
            return code;
        }
    }

    private String translationFUNCTIONS() {
        // Assuming this processes the function declarations
        StringBuilder functionsCode = new StringBuilder();
        while (currentTokenIndex < tokens.size()) {
            Token token = tokens.get(currentTokenIndex);
            // Process function tokens here (to be implemented)
            // functionsCode.append(translationFUNCTION(token));
            // For demonstration, we'll just break the loop
            break; // Placeholder to stop the loop for demonstration
        }
        return functionsCode.toString(); // Return the accumulated function code
    }

    private String translationCOMMAND() {
        Token currentToken = tokens.get(currentTokenIndex);
        switch (currentToken.type) {
            case SKIP:
                currentTokenIndex++;
                return " REM DO NOTHING ";
            case HALT:
                currentTokenIndex++;
                return " STOP ";
            case PRINT:
                currentTokenIndex++;
                return "PRINT " + translateATOMIC(); // ATOMIC after PRINT
            case RETURN:
                currentTokenIndex++;
                return "RETURN " + translateATOMIC(); // ATOMIC after RETURN
            case VNAME:
                return translationASSIGN();
            case FNAME:
                return translationCALL();
            case IF:
                return translationBRANCH();
            case END:
                return "";
            default:
                throw new RuntimeException("Unexpected token: " + currentToken);
        }
    }

    private String translationASSIGN() {
        // VNAME = TERM
        String vnameCode = translateATOMIC();
        match(TokenType.EQUALS); // Consume '='
        String termCode = translationTERM();
        return vnameCode + " := " + termCode; // Translate assignment
    }

    private String translationTERM() {
        // Handle TERM definitions, such as ATOMIC, CALL, OP
        Token currentToken = tokens.get(currentTokenIndex);
        if (currentToken.type == TokenType.VNAME || currentToken.type == TokenType.CONST || currentToken.type == TokenType.CONST2) {
            return translateATOMIC(); // Handle variable name
        } else if (currentToken.type == TokenType.FNAME) {
            return translationCALL(); // Handle function call
        } else if (isOperator(currentToken.type)) {
            return translateOP(); // Handle operator translation
        }
        throw new RuntimeException("Unexpected term: " + currentToken);
    }

    private String translationCALL() {
        // Handle function calls
        Token funcToken = getCurrentToken();
        FunctionSignature signature = functionTable.get(funcToken.data);// Retrieve new name
        match(TokenType.FNAME); // Consume function name
        match(TokenType.LPAREN); // Consume '('
        String p1 = translateATOMIC();
        match(TokenType.COMMA);
        String p2 = translateATOMIC();
        match(TokenType.COMMA);
        String p3 = translateATOMIC();
        match(TokenType.RPAREN); // Consume ')'
        return "CALL_" + signature.getFunctionName() + "(" + p1 + "," + p2 + "," + p3 + ")";
    }

    private String translateATOMIC() {
        // Handle atomic elements, such as VNAME and CONST
        Token currentToken = tokens.get(currentTokenIndex++);
        if (currentToken.type == TokenType.VNAME) {
            // Retrieve renamed variable from symbol table
            return symbolTable.getVariableName(currentToken.data);
        } else if (currentToken.type == TokenType.CONST2 || currentToken.type == TokenType.CONST) {
            return currentToken.data; // Return constant as is
        }
        throw new RuntimeException("Unexpected atomic: " + currentToken);
    }

    private String translateOP() {
        // Translate operator based on the type
        Token currentToken = getCurrentToken();
        switch (currentToken.type) {
            case BINOP:
                return translateBinop();
            case UNOP:
                return translateUnop();
            default:
                throw new RuntimeException("Unexpected operator: " + currentToken);
        }
    }

    private String translateBinop() {
        Token currentToken = getCurrentToken();
        String atomic1Code = "";
        String atomic2Code = "";
        String op = currentToken.data;
        switch (op) {
            case "eq":
            case "grt":
            case "add":
            case "sub":
            case "mul":
            case "div":
                match(TokenType.BINOP); // Consume binary operator
                match(TokenType.LPAREN); // Consume '('

                // Recursively translate the first argument
                currentToken = getCurrentToken();
                if (currentToken.type == TokenType.VNAME || currentToken.type == TokenType.CONST || currentToken.type == TokenType.CONST2) {
                    atomic1Code = translateATOMIC();
                } else if (currentToken.type == TokenType.BINOP || currentToken.type == TokenType.UNOP) {
                    atomic1Code = translateOP(); // Recursive call
                }

                match(TokenType.COMMA); // Consume ','

                // Recursively translate the second argument
                currentToken = getCurrentToken();
                if (currentToken.type == TokenType.VNAME || currentToken.type == TokenType.CONST || currentToken.type == TokenType.CONST2) {
                    atomic2Code = translateATOMIC();
                } else if (currentToken.type == TokenType.BINOP || currentToken.type == TokenType.UNOP) {
                    atomic2Code = translateOP(); // Recursive call
                }

                match(TokenType.RPAREN); // Consume ')'
                return atomic1Code + convertOPtoSymbol(op) + atomic2Code;

            default:
                throw new RuntimeException("Unknown binary operator: " + op);
        }
    }

    public String convertOPtoSymbol(String op) {
        switch (op) {
            case "eq":
                return "=";
            case "grt":
                return ">";
            case "add":
                return "+";
            case "sub":
                return "-";
            case "mul":
                return "*";
            case "div":
                return "/";
            default:
                throw new RuntimeException("Unknown binary operator: " + op);
        }
    }

    private String translateUnop() {
        Token currentToken = getCurrentToken();

        match(TokenType.UNOP); // Consume unary operator
        match(TokenType.LPAREN); // Consume '('

        String argumentCode = "";

        // Check if the argument is an ATOMIC or an OP
        currentToken = getCurrentToken();
        if (currentToken.type == TokenType.VNAME || currentToken.type == TokenType.CONST || currentToken.type == TokenType.CONST2) {
            argumentCode = translateATOMIC();
        } else if (currentToken.type == TokenType.BINOP || currentToken.type == TokenType.UNOP) {
            argumentCode = translateOP(); // Recursive call
        }

        match(TokenType.RPAREN); // Consume ')'

        switch (currentToken.data) {
            case "not":
                // No specific syntax for "not", so swap branches when it is used in a condition
                return ""; // Assuming not is handled in another part of code as per instructions
            case "sqrt":
                return "SQR(" + argumentCode + ")";
            default:
                throw new RuntimeException("Unknown unary operator: " + currentToken.data);
        }
    }

    private String translationBRANCH() {
        // Handle branch statement translations
        match(TokenType.IF); // Consume 'if'
        String conditionCode = translationCOND();
        match(TokenType.THEN); // Consume 'then'
        String thenCode = translationALGO(); // Translate ALGO1
        match(TokenType.ELSE); // Consume 'else'
        String elseCode = translationALGO(); // Translate ALGO2
        return "IF " + conditionCode + " THEN " + thenCode + " ELSE " + elseCode;
    }

    private String translationCOND() {
        // Handle conditions (SIMPLE or COMPOSIT)
        // Placeholder: Assuming conditions are always SIMPLE for now
        Token currentToken = getCurrentToken();
        if (currentToken.type == TokenType.BINOP) {
            match(TokenType.BINOP);
            match(TokenType.LPAREN);
            Token atomic1 = getCurrentToken();
            if (atomic1.type == TokenType.VNAME || atomic1.type == TokenType.CONST || atomic1.type == TokenType.CONST2) {
                return translationSIMPLE(currentToken.data);
            } else {
                return translationCOMPOSIT(currentToken.data);
            }

        } else if (currentToken.type == TokenType.UNOP) {
            match(TokenType.UNOP);
            match(TokenType.LPAREN);
            Token atomic1 = getCurrentToken();
            return translationSIMPLE(currentToken.data);
        } else {
            throw new RuntimeException("Unexpected condition: " + currentToken);
        }

    }

    private String translationCOMPOSIT(String op) {
        // Handle composite conditions
        // For example: translate(BINOP(COND1, COND2))
        Token currentToken = getCurrentToken();
        if (currentToken.type == TokenType.BINOP) {
            String cond1Code = translationSIMPLE(op);
            match(TokenType.BINOP); // Consume binary operator
            String cond2Code = translationSIMPLE(op);
            match(TokenType.RPAREN); // Consume ')'
            return cond1Code + " " + op + " " + cond2Code;
        } else {
            throw new RuntimeException("Unexpected composite condition: " + currentToken);
        }
    }

    private String translationSIMPLE(String op) {
        // Handle simple conditions
        // For example: translate(BINOP(ATOMIC1, ATOMIC2))
        String atomic1Code = translateATOMIC();
        match(TokenType.COMMA); // Consume ','
        String atomic2Code = translateATOMIC();
        match(TokenType.RPAREN); // Consume ')'
        return atomic1Code + " " + op + " " + atomic2Code;
    }

    public String translateFUNCTIONS() {
        Token expected = getCurrentToken();
        if (expected == null) {
            return " REM END ";
        } else {
            if (expected.type == TokenType.VOID || expected.type == TokenType.NUM) {
                return translateFUNCTIONS1();
            } else {
                return "";
            }
        }
    }

    public String translateFUNCTIONS1() {
        String dCode = translateDECL();
        String fCode = translateFUNCTIONS2();
        return dCode + " STOP " + fCode;
    }

    public String translateDECL() {
        Token expected = getCurrentToken();
        if (expected.type == TokenType.VOID || expected.type == TokenType.NUM) {
            translateHEADER();

        } else {
            return "";
        }
        return translateBODY();
    }

    public void translateHEADER() {
        // HEADER is ignored in the code generation step.
        Token expected = getCurrentToken();
        match(expected.type);
        expected = getCurrentToken();
        match(TokenType.FNAME);
        match(TokenType.LPAREN);
        match(TokenType.VNAME);
        match(TokenType.COMMA);
        match(TokenType.VNAME);
        match(TokenType.COMMA);
        match(TokenType.VNAME);
        match(TokenType.RPAREN);
    }

    public void translateFTYP() {
        // FTYP is ignored in the code generation step.
    }

    public void translateLOCVARS() {
        // LOCVARS are ignored in the code generation step.
        Token expected = getCurrentToken();
        System.out.println(expected.type.toString());
        match(expected.type);
        match(TokenType.VNAME);
    }

    public String translateBODY() {

        String pCode = translatePROLOG();
        translateLOCVARS();
        String aCode = translationALGO();
        String eCode = translateEPILOG();
        String sCode = translateSUBFUNCS();
        return pCode + aCode + eCode + sCode;
    }

    public String translatePROLOG() {
        Token expected = getCurrentToken();
        if (expected.type == TokenType.PROLOG) {
            match(expected.type);
            return " REM BEGIN ";
        }
        return "";

    }

    public String translateEPILOG() {
        Token expected = getCurrentToken();
        if (expected.type == TokenType.EPILOG) {
            match(expected.type);
            return " REM END ";
        }
        return "";
    }

    public String translateSUBFUNCS() {
        return translateFUNCTIONS();
    }

    public String translateFUNCTIONS2() {
        return translateFUNCTIONS();  // Assuming it's a recursive structure
    }

    private void match(TokenType expected) {
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).type == expected) {
            currentTokenIndex++; // Move to the next token
        } else {
            throw new RuntimeException("Expected token: " + expected + ", found: " + tokens.get(currentTokenIndex));
        }
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.BINOP || type == TokenType.UNOP;
    }

    public void writeFormattedCodeToFile(String translatedCode) {
        // Split the translated code by lines
        String[] lines = translatedCode.split("\n");

        StringBuilder formattedCode = new StringBuilder();
        int indentLevel = 0;
        String indentString = "    "; // Use 4 spaces for each indent level

        for (String line : lines) {
            line = line.trim(); // Remove any leading/trailing whitespace

            // Decrease indent level after closing blocks (e.g., ELSE, END, STOP)
            if (line.startsWith("ELSE") || line.startsWith("END") || line.startsWith("STOP")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            // Append the formatted line with the current indent level
            formattedCode.append(indentString.repeat(indentLevel)).append(line).append("\n");

            // Increase indent level after opening blocks (e.g., IF, THEN, BEGIN)
            if (line.startsWith("IF") || line.startsWith("THEN") || line.startsWith("CALL") || line.startsWith("BEGIN")) {
                indentLevel++;
            }
        }

        // Write the formatted code to a file called output.txt
        try (FileWriter fileWriter = new FileWriter("output.txt")) {
            fileWriter.write(formattedCode.toString());
            System.out.println("Code written to output.txt");
        } catch (IOException e) {
            System.err.println("Error writing to output.txt: " + e.getMessage());
        }
    }
}

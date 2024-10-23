
import java.util.List;

public class CodeGenerator {

    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private int currentTokenIndex;

    public CodeGenerator(List<Token> tokens, SymbolTable symbolTable) {
        this.tokens = tokens;
        this.symbolTable = symbolTable;
        this.currentTokenIndex = 0; // Start at the beginning of the token list
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
        String functionsCode = translationFUNCTIONS();
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
        String functionName = symbolTable.getType(tokens.get(currentTokenIndex++).data); // Retrieve new name
        match(TokenType.LPAREN); // Consume '('
        String p1 = translateATOMIC();
        match(TokenType.COMMA);
        String p2 = translateATOMIC();
        match(TokenType.COMMA);
        String p3 = translateATOMIC();
        match(TokenType.RPAREN); // Consume ')'
        return "CALL_" + functionName + "(" + p1 + "," + p2 + "," + p3 + ")";
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
        return translationSIMPLE();
    }

    private String translationSIMPLE() {
        // Handle simple conditions
        // For example: translate(BINOP(ATOMIC1, ATOMIC2))
        String atomic1Code = translateATOMIC();
        match(TokenType.BINOP); // Consume binary operator
        String atomic2Code = translateATOMIC();
        return atomic1Code + " " + translateBinop() + " " + atomic2Code;
    }

    public String translateFUNCTIONS() {
        return " REM END ";
    }

    public String translateFUNCTIONS1() {
        String dCode = translateDECL();
        String fCode = translateFUNCTIONS2();
        return dCode + " STOP " + fCode;
    }

    public String translateDECL() {
        return translateBODY();
    }

    public void translateHEADER() {
        // HEADER is ignored in the code generation step.
    }

    public void translateFTYP() {
        // FTYP is ignored in the code generation step.
    }

    public void translateLOCVARS() {
        // LOCVARS are ignored in the code generation step.
    }

    public String translateBODY() {
        String pCode = translatePROLOG();
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
}

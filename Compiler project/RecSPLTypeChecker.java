
import java.util.*;

public class RecSPLTypeChecker {

    private final List<Token> tokens;
    private int currentTokenIndex = 0;
    private final Map<String, String> symbolTable = new HashMap<>();  // Keeps track of variable types
    private final Map<String, FunctionSignature> functionTable = new HashMap<>();  // Function signatures

    public RecSPLTypeChecker(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Helper method to get the current token
    private Token currentToken() {
        return tokens.get(currentTokenIndex);
    }

    // Helper method to advance to the next token
    private void advance() {
        if (currentTokenIndex < tokens.size() - 1) {
            currentTokenIndex++;
        }
    }

    // Helper method to check if the token matches a specific type
    private boolean match(TokenType expectedType) {
        if (currentToken().type == expectedType) {
            advance();
            return true;
        }
        return false;
    }

    // Main method to start the type checking
    public boolean typeCheck() {
        return checkProg();
    }

    // PROG ::= main GLOBVARS ALGO FUNCTIONS
    private boolean checkProg() {
        if (match(TokenType.MAIN)) {
            if (checkGlobVars() && checkAlgo() && checkFunctions()) {
                return true;
            }
        }
        return false;
    }

    // GLOBVARS ::= VTYP VNAME , GLOBVARS2 | ε
    private boolean checkGlobVars() {
        while (match(TokenType.NUM) || match(TokenType.TEXT)) {
            Token vname = currentToken();
            if (vname.type == TokenType.VNAME) {
                symbolTable.put(vname.data, currentToken().type.name()); // Add to symbol table
                advance();
                if (!match(TokenType.COMMA)) {
                    break;
                }
            } else {
                return false;  // Invalid variable name
            }
        }
        return true;  // No global variables is valid too
    }

    // ALGO ::= begin INSTRUC end
    private boolean checkAlgo() {
        if (match(TokenType.BEGIN)) {
            if (checkInstruc()) {
                return match(TokenType.END);
            }
        }
        return false;
    }

    // INSTRUC ::= COMMAND ; INSTRUC2 | ε
    private boolean checkInstruc() {
        while (checkCommand()) {
            if (!match(TokenType.SEMICOLON)) {
                return false;  // Every command must end with a semicolon
            }
        }
        return true;
    }

    // COMMAND ::= skip | halt | print ATOMIC | return ATOMIC | ASSIGN | CALL | BRANCH
    private boolean checkCommand() {
        if (match(TokenType.SKIP) || match(TokenType.HALT)) {
            return true;  // Skip and halt are valid commands
        } else if (match(TokenType.PRINT)) {
            return checkAtomic();
        } else if (match(TokenType.RETURN)) {
            return checkAtomic();
        } else if (checkAssign()) {
            return true;
        } else if (checkCall()) {
            return true;
        } else if (checkBranch()) {
            return true;
        }
        return false;
    }

    // ASSIGN ::= VNAME < input | VNAME = TERM
    private boolean checkAssign() {
        Token vname = currentToken();
        if (vname.type == TokenType.VNAME) {
            advance();
            if (match(TokenType.LESS)) {
                return match(TokenType.INPUT);  // Only num types can take input
            } else if (match(TokenType.EQUALS)) {
                return checkTerm();  // Ensure the term type matches variable type
            }
        }
        return false;
    }

    // TERM ::= ATOMIC | CALL | OP
    private boolean checkTerm() {
        return checkAtomic() || checkCall() || checkOp();
    }

    // ATOMIC ::= VNAME | CONST
    private boolean checkAtomic() {
        Token token = currentToken();
        if (token.type == TokenType.VNAME) {
            advance();
            return true;  // Check symbol table later
        } else if (token.type == TokenType.CONST) {
            advance();
            return true;  // A constant is valid
        }
        return false;
    }

    // CALL ::= FNAME( ATOMIC , ATOMIC , ATOMIC )
    private boolean checkCall() {
        Token fname = currentToken();
        if (fname.type == TokenType.FNAME) {
            advance();
            if (match(TokenType.LPAREN)) {
                if (checkAtomic() && match(TokenType.COMMA) && checkAtomic() && match(TokenType.COMMA) && checkAtomic()) {
                    return match(TokenType.RPAREN);
                }
            }
        }
        return false;
    }

    // OP ::= UNOP( ARG ) | BINOP( ARG1 , ARG2 )
    private boolean checkOp() {
        if (checkUnop()) {
            return true;
        } else if (checkBinop()) {
            return true;
        }
        return false;
    }

    // UNOP ::= not | sqrt
    private boolean checkUnop() {
        if (match(TokenType.UNOP)) {
            return match(TokenType.LPAREN) && checkAtomic() && match(TokenType.RPAREN);
        }
        return false;
    }

    // BINOP ::= eq | grt | add | sub | mul | div | or | and
    private boolean checkBinop() {
        if (match(TokenType.BINOP)) {
            return match(TokenType.LPAREN) && checkAtomic() && match(TokenType.COMMA) && checkAtomic() && match(TokenType.RPAREN);
        }
        return false;
    }

    // BRANCH ::= if COND then ALGO1 else ALGO2
    private boolean checkBranch() {
        if (match(TokenType.IF)) {
            if (checkCond()) {
                return match(TokenType.THEN) && checkAlgo() && match(TokenType.ELSE) && checkAlgo();
            }
        }
        return false;
    }

    // COND ::= SIMPLE | COMPOSIT
    private boolean checkCond() {
        return checkSimple() || checkComposit();
    }

    // SIMPLE ::= BINOP( ATOMIC , ATOMIC )
    private boolean checkSimple() {
        return checkBinop();
    }

    // COMPOSIT ::= BINOP( SIMPLE , SIMPLE ) | UNOP( SIMPLE )
    private boolean checkComposit() {
        if (checkUnop()) {
            return checkSimple();
        } else if (checkBinop()) {
            return checkSimple() && match(TokenType.COMMA) && checkSimple();
        }
        return false;
    }

    // FUNCTIONS ::= DECL | ε
    private boolean checkFunctions() {
        while (checkDecl()) {
            // Keep checking function declarations
        }
        return true;  // No functions is valid too
    }

    // DECL ::= HEADER BODY
    private boolean checkDecl() {
        return checkHeader() && checkBody();
    }

    // HEADER ::= FTYP FNAME( VNAME , VNAME , VNAME )
    private boolean checkHeader() {
        if (match(TokenType.NUM) || match(TokenType.VOID)) {
            Token fname = currentToken();
            if (fname.type == TokenType.FNAME) {
                advance();
                if (match(TokenType.LPAREN)) {
                    if (checkParamList()) {
                        return match(TokenType.RPAREN);
                    }
                }
            }
        }
        return false;
    }

    // Check parameter list (three VNAMEs)
    private boolean checkParamList() {
        return match(TokenType.VNAME) && match(TokenType.COMMA)
                && match(TokenType.VNAME) && match(TokenType.COMMA)
                && match(TokenType.VNAME);
    }

    // BODY ::= PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
    private boolean checkBody() {
        return match(TokenType.PROLOG) && checkLocVars() && checkAlgo() && match(TokenType.EPILOG) && checkSubFuncs() && match(TokenType.END);
    }

    // LOCVARS ::= VTYP1 VNAME1 , VTYP2 VNAME2 , VTYP3 VNAME3
    private boolean checkLocVars() {
        for (int i = 0; i < 3; i++) {
            if (!checkVarDecl()) {
                return false;
            }
        }
        return true;
    }

    // Check variable declaration
    private boolean checkVarDecl() {
        if (match(TokenType.NUM) || match(TokenType.TEXT)) {
            if (match(TokenType.VNAME)) {
                return match(TokenType.COMMA);
            }
        }
        return false;
    }

    // SUBFUNCS ::= FUNCTIONS
    private boolean checkSubFuncs() {
        return checkFunctions();
    }

    public boolean typecheck(Node node) {
        String nodeType = node.getType();  // Assume Node has a getType() method

        // Check the node type and apply type-checking rules
        switch (nodeType) {
            case "CONST":
                // A constant has a type based on its value (number, text, etc.)
                Token token = node.getToken(); // Get the token associated with the node
                if (token.type == TokenType.CONST) {
                    return true; // Numeric constant is valid
                } else if (token.type == TokenType.TEXT) {
                    return true; // Text constant is valid
                } else {
                    return false; // Invalid constant type
                }

            case "VNAME":
                // Variable name, check its type in the symbol table
                String varName = node.getToken().getData(); // Get the variable name
                if (symbolTable.containsKey(varName)) {
                    return true; // Variable is declared and its type is known
                } else {
                    throw new RuntimeException("Variable " + varName + " not declared");
                }

            case "ASSIGN":
                // Assignment, ensure the left-hand side (variable) and right-hand side (expression) types match
                Node lhs = node.getLeftChild(); // Variable being assigned to
                Node rhs = node.getRightChild(); // Expression being assigned

                String lhsType = typeof(lhs);
                String rhsType = typeof(rhs);

                if (!lhsType.equals(rhsType)) {
                    throw new RuntimeException("Type mismatch in assignment: " + lhsType + " = " + rhsType);
                }

                // Recursively check the right-hand side expression
                return typecheck(rhs);

            case "BINOP":
                // Binary operator, ensure both operands have the correct types
                Node leftOperand = node.getLeftChild();
                Node rightOperand = node.getRightChild();
                String opType = node.getToken().getData(); // Operator (e.g., "+", "-", "*", "/")

                String leftType = typeof(leftOperand);
                String rightType = typeof(rightOperand);

                if (!leftType.equals(rightType)) {
                    throw new RuntimeException("Type mismatch in binary operation: " + leftType + " " + opType + " " + rightType);
                }

                if (!isValidOperation(leftOperand, rightOperand, opType)) {
                    throw new RuntimeException("Invalid operation: " + opType + " for types " + leftType + " and " + rightType);
                }

                // Recursively type check both operands
                return typecheck(leftOperand) && typecheck(rightOperand);

            case "CALL":
                // Function call, check the types of arguments and the return type
                // Example: CALL ::= FNAME( ARG1, ARG2, ARG3 )
                List<Node> arguments = node.getChildren(); // Assume this method retrieves function arguments

                // Fetch the function's signature (argument types and return type)
                Token funcToken = currentToken();
                String functionName = node.getToken().getData(); // Function name
                FunctionSignature signature = functionTable.get(funcToken.data); // Retrieve function signature

                if (arguments.size() != signature.getParamTypes().size()) {
                    throw new RuntimeException("Argument count mismatch in function call: " + functionName);
                }

                for (int i = 0; i < arguments.size(); i++) {
                    Node arg = arguments.get(i);
                    String argType = typeof(arg);
                    String expectedType = signature.getParamTypes().get(i);

                    if (!argType.equals(expectedType)) {
                        throw new RuntimeException("Argument type mismatch in function call: expected " + expectedType + " but got " + argType);
                    }

                    // Recursively type check the argument
                    if (!typecheck(arg)) {
                        return false;
                    }
                }

                return true;

            default:
                // For any other node types, check if they're valid or throw an error
                throw new RuntimeException("Unknown node type for typechecking: " + nodeType);
        }
    }

    public String typeof(Node node) {
        String nodeType = node.getType();  // Get the node type (e.g., CONST, VNAME, BINOP, etc.)

        switch (nodeType) {
            case "CONST":
                // For constants, return the type based on the value (assuming token class is used to identify type)
                Token token = node.getToken(); // Get the associated token

                if (token.type == TokenType.CONST) {
                    return "num";  // Numeric constant (integer)
                } else if (token.type == TokenType.TEXT) {
                    return "text";  // Text constant (string)
                } else {
                    throw new RuntimeException("Unknown constant type for token: " + token.getData());
                }

            case "VNAME":
                // For variables, return the type from the symbol table
                String varName = node.getToken().getData(); // Get the variable name
                if (symbolTable.containsKey(varName)) {
                    return symbolTable.get(varName);  // Return the type of the variable
                } else {
                    throw new RuntimeException("Variable " + varName + " is not declared");
                }

            case "ASSIGN":
                // For assignments, return the type of the right-hand side (RHS)
                Node rhs = node.getRightChild();
                return typeof(rhs);  // The type of the RHS expression

            case "BINOP":
                // For binary operations, ensure both operands have the same type and return that type
                Node leftOperand = node.getLeftChild();
                Node rightOperand = node.getRightChild();
                String leftType = typeof(leftOperand);
                String rightType = typeof(rightOperand);

                if (!leftType.equals(rightType)) {
                    throw new RuntimeException("Type mismatch in binary operation: " + leftType + " and " + rightType);
                }

                // Assuming the operation is valid (checked elsewhere), return the type of the operands
                return leftType;

            case "CALL":
                // For function calls, return the return type of the function
                Token funcToken = currentToken();
                String functionName = node.getToken().getData(); // Function name
                FunctionSignature signature = functionTable.get(funcToken.data); // Retrieve function signature

                // Return the return type of the function
                return signature.getReturnType();

            case "IF":
            case "WHILE":
                // Conditional statements, the condition must be a boolean
                Node condition = node.getLeftChild();
                String conditionType = typeof(condition);

                if (!conditionType.equals("bool")) {
                    throw new RuntimeException("Condition in " + nodeType + " statement must be of type 'bool'");
                }

                // No specific return type for control flow, so returning null
                return null;

            default:
                // For any other node types, throw an error as they should be handled separately
                throw new RuntimeException("Unknown node type for typeof: " + nodeType);
        }
    }

    public boolean isValidOperation(Node leftOperand, Node rightOperand, String operationType) {
        String leftType = typeof(leftOperand);
        String rightType = typeof(rightOperand);

        // Example checks for different operation types
        return switch (operationType) {
            case "add", "sub", "mul", "div" ->
                leftType.equals("num") && rightType.equals("num");
            case "eq", "grt", "or", "and" ->
                (leftType.equals("num") || leftType.equals("text"))
                && (rightType.equals("num") || rightType.equals("text"));
            default ->
                false;
        }; // Numeric operations require both operands to be numeric
        // Comparison operations can be applied to numeric or boolean types
        // Add other cases for different operation types as needed
        // Unknown operation type
    }

}

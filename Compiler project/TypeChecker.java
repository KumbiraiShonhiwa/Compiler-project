import java.util.ArrayList;
import java.util.List;

public class TypeChecker {
    private SymbolTable symbolTable;
    private final List<String> errors;

    public TypeChecker(SymbolTable symbolTableA) {
        this.symbolTable = symbolTableA;
        this.errors = new ArrayList<>();
    }

    public boolean check(List<Token> tokens) {
        for (Token token : tokens) {
            switch (token.type) {
                case MAIN:
                    checkMainFunction(token);
                    break;
                case BEGIN:
                    symbolTable.enterScope();
                    break;
                case END:
                    symbolTable.exitScope();
                    break;
                case VNAME:
                    checkVariableReference(token);
                    break;
                case CONST:
                    // Handle constants if necessary (this is handled later in expressions)
                    break;
                case EQUALS:
                    handleAssignment(tokens);
                    break;
                case BINOP:
                    checkBinaryOperation(tokens, token);
                    break;
                case UNOP:
                    checkUnaryOperation(tokens, token);
                    break;
                case IF:
                case THEN:
                case ELSE:
                    checkCondition(tokens);
                    break;
                
                case PRINT:
                    checkPrintStatement(tokens);
                    break;
                case SKIP:
                case HALT:
                    // These commands don't require specific checks
                    break;
                // Add other cases as necessary for your grammar
                default:
                    break;
            }
        }

        return reportErrors();
    }

    private void checkMainFunction(Token token) {
        // Check for the presence of a main function
        // Assuming you have logic for this already
    }

    private void checkVariableReference(Token token) {
        if (!symbolTable.containsSymbol(token.data)) {
            errors.add("Error: Variable '" + token.data + "' is not declared.");
        }
    }

    private void handleAssignment(List<Token> tokens) {
        int index = tokens.indexOf(new Token(TokenType.EQUALS, "=", -1));
        if (index > 0) {
            Token variable = tokens.get(index - 1);
            if (!symbolTable.containsSymbol(variable.data)) {
                errors.add("Error: Variable '" + variable.data + "' is not declared before assignment.");
            }
            // Further checks for the expression on the right-hand side
            // You may want to check the type of the assigned value
            checkAssignmentValue(tokens, index);
        }
    }

    private void checkAssignmentValue(List<Token> tokens, int index) {
        // Assuming the term is to the right of the assignment
        if (index + 1 < tokens.size()) {
            Token assignedValue = tokens.get(index + 1);
            // Logic to check type of the assigned value goes here
            // For example:
            // if (assignedValue.type != TokenType.CONST && !symbolTable.containsSymbol(assignedValue.data)) {
            //     errors.add("Error: Assigned value must be a declared variable or constant.");
            // }
        }
    }

    private void checkBinaryOperation(List<Token> tokens, Token token) {
        // Implement type checking for binary operations
        int index = tokens.indexOf(token);
        if (index > 0 && index < tokens.size() - 1) {
            Token leftOperand = tokens.get(index - 1);
            Token rightOperand = tokens.get(index + 1);

            if (!isValidOperand(leftOperand) || !isValidOperand(rightOperand)) {
                errors.add("Error: Invalid types for binary operation '" + token.data + "'.");
            }
        }
    }

    private void checkUnaryOperation(List<Token> tokens, Token token) {
        // Implement type checking for unary operations
        int index = tokens.indexOf(token);
        if (index + 1 < tokens.size()) {
            Token operand = tokens.get(index + 1);
            if (!isValidOperand(operand)) {
                errors.add("Error: Invalid type for unary operation '" + token.data + "'.");
            }
        }
    }

    private boolean isValidOperand(Token operand) {
        // Determine if the operand is a valid type (e.g., number, variable, constant)
        return operand.type == TokenType.CONST || 
               (operand.type == TokenType.VNAME && symbolTable.containsSymbol(operand.data));
    }

    private void checkCondition(List<Token> tokens) {
        // Check conditions in IF statements
        // Ensure the condition evaluates to a boolean type
        // You may want to look for the last binary operation before IF
        // Check that the result type of the condition is boolean
    }

    private void checkFunctionCall(List<Token> tokens) {
        // Check if the function called exists in the symbol table
        // Check the number of arguments matches the function signature
        // Ensure types of arguments match expected types
    }

    private void checkPrintStatement(List<Token> tokens) {
        // Check that the argument of print is a valid type (either variable or constant)
        if (tokens.size() < 2 || !isValidOperand(tokens.get(1))) {
            errors.add("Error: Invalid argument in print statement.");
        }
    }

    private boolean reportErrors() {
        if (!errors.isEmpty()) {
            System.err.println("Type Checking Errors:");
            for (String error : errors) {
                System.err.println(error);
            }
            return false; 
        } else {
            System.out.println("Type checking completed successfully. No errors found.");
            return true; 
        }
    }
}

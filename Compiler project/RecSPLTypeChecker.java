import java.util.ArrayList;
import java.util.List;
public class RecSPLTypeChecker {
    private SymbolTable symbolTable;
    private final List<String> errors;

    public RecSPLTypeChecker(SymbolTable symbolTableA) {
        this.symbolTable = symbolTableA;
        this.errors = new ArrayList<>();
    }

    public boolean check(List<Token> tokens) {
        for (Token token : tokens) {
            switch (token.type) {
                case MAIN:
                    checkMainFunction(tokens);
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
                    break;
                default:
                    break;
            }
        }

        return reportErrors();
    }

    private void checkMainFunction(List<Token> tokens) {
        boolean foundMain = false;
    
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
    
           
            if (token.type == TokenType.MAIN) {
                foundMain = true;
            }
        }
    
        // If main function was not found
        if (!foundMain) {
            errors.add("Error: 'main' function is not declared.");
        }
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
        
            checkAssignmentValue(tokens, index);
        }
    }

    private void checkAssignmentValue(List<Token> tokens, int index) {
        if (index + 1 < tokens.size()) {
            Token assignedValue = tokens.get(index + 1);
    
            if (assignedValue.type == TokenType.CONST) {
                return; // Valid constant assignment
            }
    
            if (assignedValue.type == TokenType.VNAME) {
                if (!symbolTable.containsSymbol(assignedValue.data)) {
                    errors.add("Error: Assigned value must be a declared variable or constant.");
                } else {
                   
                }
            } else {
                errors.add("Error: Assigned value must be a constant or a declared variable.");
            }
        } else {
            errors.add("Error: Missing value for assignment.");
        }
    }
    

    private void checkBinaryOperation(List<Token> tokens, Token token) {
        // Implement type checking for binary operations
        int index = tokens.indexOf(token);
        if (index > 0 && index < tokens.size() - 1) {

            Token leftOperand = tokens.get(index - 2);
            Token rightOperand = tokens.get(index + 2);

            

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
        int ifIndex = tokens.indexOf(new Token(TokenType.IF, "if", 0));
        if (ifIndex != -1) {
            int conditionEndIndex = ifIndex + 1;
    
            while (conditionEndIndex < tokens.size() && tokens.get(conditionEndIndex).type != TokenType.THEN) {
                conditionEndIndex++;
            }
    
            if (conditionEndIndex < tokens.size()) {
                Token conditionToken = tokens.get(conditionEndIndex - 1);
                if (conditionToken.type == TokenType.BINOP) {
                    // Logic to check the left and right operands of the binary operation
                    Token leftOperand = tokens.get(conditionEndIndex - 2); // Left operand
                    Token rightOperand = tokens.get(conditionEndIndex); // Right operand
    
                    if (!isBooleanExpression(leftOperand) || !isBooleanExpression(rightOperand)) {
                        errors.add("Error: Condition in IF statement must evaluate to a boolean type.");
                    }
                } else {
                    errors.add("Error: Condition in IF statement must be a binary operation.");
                }
            } else {
                errors.add("Error: Missing THEN after IF condition.");
            }
        }
    }
    
    private boolean isBooleanExpression(Token token) {
       
        return token.type == TokenType.BINOP && (token.data.equals("eq") || token.data.equals("grt"));
    }
    

    private void checkFunctionCall(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type == TokenType.FNAME) {
                String functionName = tokens.get(i).data;
    
                if (!symbolTable.containsSymbol(functionName)) {
                    errors.add("Error: Function '" + functionName + "' is not declared.");
                    continue;
                }
    
                int paramStartIndex = i + 1; // Start of parameters
                int paramEndIndex = paramStartIndex;
    
                while (paramEndIndex < tokens.size() && tokens.get(paramEndIndex).type != TokenType.RPAREN) {
                    paramEndIndex++;
                }
    
                if (paramEndIndex < tokens.size()) {
                    List<Token> params = tokens.subList(paramStartIndex, paramEndIndex);
                  
                } else {
                    errors.add("Error: Missing closing parenthesis for function call '" + functionName + "'.");
                }
            }
        }
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

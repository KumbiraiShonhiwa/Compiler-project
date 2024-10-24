import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.Stack;

public class RecSPLTypeChecker {

    private final SymbolTable symbolTable;
    private final Map<String, FunctionSignature> functionTable;

    // Constructor
    public RecSPLTypeChecker(SymbolTable symbolTable, Map<String, FunctionSignature> functionTable) {
        this.symbolTable = symbolTable;
        this.functionTable = functionTable;
    }

    // Main method to check the entire program
    public boolean checkProgram(Node root) {
        symbolTable.enterScope(); // Global scope

        boolean isCorrect = checkNode(root);

        symbolTable.exitScope(); // Exit global scope
        return isCorrect;
    }

    
   

   
    // Check variable assignments
    private boolean checkAssignment(SyntaxTreeNode node) {
        String varName = node.getChild(0).getValue();
        SyntaxTreeNode valueNode = node.getChild(1);

        String expectedType = symbolTable.getType(varName);
        if (expectedType == null) {
            System.out.println("Undefined variable: " + varName);
            return false;
        }

        String actualType = evaluateType(valueNode);
        if (!expectedType.equals(actualType)) {
            System.out.println("Type mismatch in assignment to " + varName);
            return false;
        }
        return true;
    }

    // Check function calls
    private boolean checkFunctionCall(SyntaxTreeNode node) {
        String functionName = node.getChild(0).getValue();
        List<String> paramTypes = getParameterTypes(node);

        FunctionSignature signature = functionTable.get(functionName);
        if (signature == null) {
            System.out.println("Undefined function: " + functionName);
            return false;
        }

        List<String> expectedParamTypes = signature.getParameterTypes();
        if (!expectedParamTypes.equals(paramTypes)) {
            System.out.println("Parameter type mismatch in call to " + functionName);
            return false;
        }

        return true;
    }

    // Check branches (if-else statements)
    private boolean checkBranch(SyntaxTreeNode node) {
        SyntaxTreeNode conditionNode = node.getChild(0);
        String conditionType = evaluateType(conditionNode);

        if (!"bool".equals(conditionType)) {
            System.out.println("Condition must evaluate to boolean");
            return false;
        }

        boolean thenCorrect = checkNode(node.getChild(1));
        boolean elseCorrect = checkNode(node.getChild(2));
        return thenCorrect && elseCorrect;
    }

   

    // Check the type of an operation
    private boolean checkOperation(SyntaxTreeNode node) {
        String resultType = evaluateOperationType(node);
        return !"unknown".equals(resultType);
    }

    // Evaluate the type of an operation (e.g., add, sub, etc.)
    private String evaluateOperationType(Node node) {
        String operator = node.getOperator();
        String leftType = evaluateType(node.getChild(0));
        String rightType = evaluateType(node.getChild(1));

        if ("add".equals(operator) || "sub".equals(operator) || "mul".equals(operator) || "div".equals(operator)) {
            if ("num".equals(leftType) && "num".equals(rightType)) {
                return "num";
            }
        } else if ("eq".equals(operator) || "grt".equals(operator) || "and".equals(operator) || "or".equals(operator)) {
            if ("num".equals(leftType) && "num".equals(rightType)) {
                return "bool";
            }
        }
        return "unknown";
    }

    // Helper method to get the parameter types from a function call node
    private List<String> getParameterTypes(Node node) {
        // Assuming the function call has parameters as children
        List<String> paramTypes = new ArrayList<>();
        for (int i = 1; i < node.children.size(); i++) {
            paramTypes.add(evaluateType(node.getChild(i)));
        }
        return paramTypes;
    }
}

public class TypeChecker {

    private SymbolTable symbolTable;

    public TypeChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    // Main typecheck procedure
    public boolean typecheck(Node rootNode) {
        if (rootNode == null) {
            System.err.println("The AST root node is null.");
            return false;
        }
        System.out.println("Starting type-checking...");
        boolean result = typecheckNode(rootNode);
        System.out.println(result ? "Type-checking completed successfully." : "Type-checking failed.");
        return result;
    }

    private boolean typecheckNode(Node node) {
        if (node == null) {
            System.err.println("Encountered a null node during type checking.");
            return false;
        }

        System.out.println("Type-checking node: " + node.getSymbol());
        Node next = node.getChildren().get(0);
        System.out.println("Next node: " + next.getSymbol());
        switch (next.getSymbol()) {
            case "PROG" -> {
                return typecheckPROG(node);
            }
            case "ALGO" -> {
                return typecheckALGO(node);
            }
            case "INSTRUC" -> {
                return typecheckINSTRUC(node);
            }
            case "COMMAND" -> {
                return typecheckCOMMAND(node);
            }
            case "ASSIGN" -> {
                return typecheckASSIGN(node);
            }
            case "print" -> {
                return typecheckPrint(node);
            }
            case "begin", "end" -> {
                return true; // No specific type check needed for 'begin' and 'end'
            }
            default -> {
                System.err.println("No type-checking rules for symbol: " + node.getSymbol());
                return false;
            }
        }
    }

    private boolean typecheckPROG(Node node) {
        System.out.println("Type-checking PROG node...");
        return typecheckNode(node.getChildren().get(0)); // Assume the first child is ALGO
    }

    private boolean typecheckALGO(Node node) {
        System.out.println("Type-checking ALGO node...");
        for (Node child : node.getChildren()) {
            if (!typecheckNode(child)) {
                return false;
            }
        }
        return true;
    }

    private boolean typecheckINSTRUC(Node node) {
        System.out.println("Type-checking INSTRUC node...");
        for (Node child : node.getChildren()) {
            if (!typecheckNode(child)) {
                return false;
            }
        }
        return true;
    }

    private boolean typecheckCOMMAND(Node node) {
        System.out.println("Type-checking COMMAND node...");
        for (Node child : node.getChildren()) {
            if (!typecheckNode(child)) {
                return false;
            }
        }
        return true;
    }

    private boolean typecheckASSIGN(Node node) {
        System.out.println("Type-checking ASSIGN node...");
        Node varNode = node.getChildren().get(0); // The variable being assigned to
        Node termNode = node.getChildren().get(1); // The value being assigned

        // Ensure the variable exists in the symbol table and determine its type
        String varType = typeof(varNode);
        String termType = typeof(termNode);

        System.out.println("Variable Type: " + varType + ", Term Type: " + termType);

        if ("u".equals(varType) || "u".equals(termType)) {
            System.err.println("Undefined variable or term in assignment.");
            return false;
        }

        // Ensure that the types match before assigning
        if (!varType.equals(termType)) {
            System.err.println("Type mismatch in assignment: " + varType + " vs " + termType);
            return false;
        }

        // Update the symbol table with the new variable and its type (if applicable)
        //symbolTable.insert(varNode.getSymbol(), varType);
        return true;
    }

    private boolean typecheckPrint(Node node) {
        System.out.println("Type-checking print node...");
        String type = typeof(node.getChildren().get(0)); // Assuming the first child is the term to print
        System.out.println("Print Type: " + type);
        return "n".equals(type) || "t".equals(type);
    }

    // Auxiliary typeof function
    private String typeof(Node node) {
        if (node == null) {
            System.err.println("Encountered a null node while determining type.");
            return "u"; // undefined type
        }

        String symbol = node.getSymbol();

        // Check if the symbol is a variable in the symbol table
        if (symbolTable.containsSymbol(symbol)) {
            return symbolTable.getType(symbol);
        }

        // Determine type based on the node's symbol
        switch (symbol) {
            case "num":
                return "n"; // number type
            case "text":
                return "t"; // text type
            case "void":
                return "v"; // void type
            case "VNAME":
                // Get the type of the variable name from the symbol table
                String varName = node.getChildren().get(0).getSymbol();
                return symbolTable.getType(varName);
            case "CONST":
                return symbol.matches("\\d+") ? "n" : "t"; // Numeric or text constant
            case "TERM":
                return typeof(node.getChildren().get(0)); // Recursively check the term's type
            default:
                System.err.println("Undefined type for symbol: " + symbol);
                return "u"; // undefined type
        }
    }
}

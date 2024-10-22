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

        switch (node.getSymbol()) {
            case "PROG":
                return typecheckPROG(node);
            case "ALGO":
                return typecheckALGO(node);
            case "INSTRUC":
                return typecheckINSTRUC(node);
            case "COMMAND":
                return typecheckCOMMAND(node);
            case "ASSIGN":
                return typecheckASSIGN(node);
            case "print":
                return typecheckPrint(node);
            case "begin":
            case "end":
                return true; // No specific type check needed for 'begin' and 'end'
            default:
                System.err.println("No type-checking rules for symbol: " + node.getSymbol());
                return false;
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
        Node varNode = node.getChildren().get(0);
        Node termNode = node.getChildren().get(1);
        
        char varType = typeof(varNode);
        char termType = typeof(termNode);
        
        System.out.println("Variable Type: " + varType + ", Term Type: " + termType);
        
        if (varType == 'u' || termType == 'u') {
            System.err.println("Undefined variable or term in assignment.");
            return false;
        }
        
        if (varType != termType) {
            System.err.println("Type mismatch in assignment: " + varType + " vs " + termType);
            return false;
        }

        return true;
    }

    private boolean typecheckPrint(Node node) {
        System.out.println("Type-checking print node...");
        char type = typeof(node.getChildren().get(0)); // Assuming the first child is the term to print
        System.out.println("Print Type: " + type);
        return type == 'n' || type == 't';
    }

    // Auxiliary typeof function
    private char typeof(Node node) {
        if (node == null) {
            System.err.println("Encountered a null node while determining type.");
            return 'u'; // undefined type
        }

        String symbol = node.getSymbol();
        if (symbolTable.contains(symbol)) {
            return symbolTable.getType(symbol);
        }

        switch (symbol) {
            case "num":
                return 'n';
            case "text":
                return 't';
            case "void":
                return 'v';
            case "VNAME":
                return symbolTable.getType(node.getChildren().get(0).getSymbol());
            case "CONST":
                return symbol.matches("\\d+") ? 'n' : 't';
            case "TERM":
                return typeof(node.getChildren().get(0)); // A
            default:
                System.err.println("Undefined type for symbol: " + symbol);
                return 'u'; // undefined type
        }
    }
}

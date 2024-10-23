
import java.util.List;

public class RecSPLTypeChecker {

    private final SymbolTable symbolTable;

    public RecSPLTypeChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public boolean typecheck(Node root) {
        return typecheckNode(root);
    }

    private boolean typecheckNode(Node node) {
        System.out.println("Type-checking node: " + node.getSymbol());
        if ("ROOT".equals(node.getSymbol())) {
            Node nextNode = node.getChildren().get(0);
            switch (nextNode.getSymbol()) {
                case "main":
                    return typecheckPROG(node);
                case "GLOBVARS":
                    return typecheckGLOBVARS(node);
                case "ALGO":
                    return typecheckALGO(node);
                case "FUNCTIONS":
                    return typecheckFUNCTIONS(node);
                case "COMMAND":
                    return typecheckCOMMAND(node);
                // Add cases for other symbols as necessary
                default:
                    return false; // Unrecognized symbol
            }
        } else {
        }
        return false;
    }

    private String typeof(Node node) {
        switch (node.getSymbol()) {
            case "VNAME":
                return symbolTable.getType(node.getSymbol()); // Consult symbol table for variable type
            case "CONST":
                return getConstType(node);
            case "VTYP":
                return typeofVtyp(node);
            case "ATOMIC":
                return typeofAtomic(node);
            case "TERM":
                return typeofTerm(node);
            case "CALL":
                return typeofCall(node);
            case "OP":
                return typeofOp(node);
            // Add more cases as needed
            default:
                return null; // Undefined type
        }
    }

    private String getConstType(Node constNode) {
        if (isNumericConstant(constNode)) {
            return "n"; // Numeric type
        } else if (isTextConstant(constNode)) {
            return "t"; // Text type
        }
        return null; // Undefined constant type
    }

    private boolean isNumericConstant(Node constNode) {
        return constNode.getSymbol().matches("\\d+"); // Simple regex for digits
    }

    private boolean isTextConstant(Node constNode) {
        return constNode.getSymbol().startsWith("\"") && constNode.getSymbol().endsWith("\"");
    }

    private String typeofVtyp(Node vtypNode) {
        switch (vtypNode.getSymbol()) {
            case "num":
                return "n"; // Numeric type
            case "text":
                return "t"; // Text type
            default:
                return null; // Undefined
        }
    }

    private String typeofAtomic(Node atomicNode) {
        if (atomicNode.getChildren().isEmpty()) {
            return null; // No children means it's an invalid atomic
        }

        Node firstChild = atomicNode.getChildren().get(0);
        switch (firstChild.getSymbol()) {
            case "VNAME":
                return typeof(firstChild); // Get type from symbol table
            case "CONST":
                return typeof(firstChild); // Get constant type
            default:
                return null; // Undefined atomic
        }
    }

    private String typeofTerm(Node termNode) {
        if (termNode.getChildren().isEmpty()) {
            return null; // No children means it's an invalid term
        }

        Node firstChild = termNode.getChildren().get(0);
        switch (firstChild.getSymbol()) {
            case "ATOMIC":
                return typeofAtomic(firstChild); // Type from atomic
            case "CALL":
                return typeofCall(firstChild); // Type from call
            case "OP":
                return typeofOp(firstChild); // Type from operation
            default:
                return null; // Undefined term
        }
    }

    private boolean typecheckPROG(Node progNode) {
        // Assume GLOBVARS is the first child and FUNCTIONS is the second child
        Node globvarsNode = progNode.getChildren().get(0);
        Node functionsNode = progNode.getChildren().get(1);

        return typecheckGLOBVARS(globvarsNode) && typecheckFUNCTIONS(functionsNode);
    }

    private boolean typecheckGLOBVARS(Node globvarsNode) {
        for (Node globvar : globvarsNode.getChildren()) {
            String vtType = typeof(globvar.getChildren().get(0)); // VTYPE
            String vName = globvar.getChildren().get(1).getSymbol(); // VNAME
            symbolTable.link(vtType, vName);
        }
        return true;
    }

    private boolean typecheckALGO(Node algoNode) {
        return typecheckINSTRUC(algoNode); // Assuming algoNode is directly the INSTRUC
    }

    private boolean typecheckINSTRUC(Node instrucNode) {
        for (Node instruc : instrucNode.getChildren()) {
            if (!typecheckCOMMAND(instruc)) {
                return false;
            }
        }
        return true;
    }

    private boolean typecheckBranch(Node commandNode) {
        Node condNode = commandNode.getChildren().get(0); // COND
        String condType = typeof(condNode);
        if (condType.equals("b")) { // Assuming "b" is for boolean
            return typecheckALGO(commandNode.getChildren().get(1)) && typecheckALGO(commandNode.getChildren().get(2)); // ALGO1 and ALGO2
        }
        return false; // The condition is not of boolean type
    }

    private boolean typecheckCOMMAND(Node commandNode) {
        switch (commandNode.getSymbol()) {
            case "SkipCommand":
            case "HaltCommand":
                return true; // Base-case for simple commands
            case "PrintCommand":
                return typecheckPrintCommand(commandNode);
            case "ReturnCommand":
                return typecheckReturn(commandNode);
            case "AssignCommand":
                return typecheckAssign(commandNode);
            case "CallCommand":
                return typecheckCall(commandNode);
            case "BranchCommand":
                return typecheckBranch(commandNode);
            default:
                return false; // Unrecognized command
        }
    }

    private boolean typecheckPrintCommand(Node commandNode) {
        Node atomicNode = commandNode.getChildren().get(0); // Assume ATOMIC is the first child
        String atomicType = typeof(atomicNode);
        return atomicType.equals("n") || atomicType.equals("t");
    }

    private boolean typecheckReturn(Node commandNode) {
        Node atomicNode = commandNode.getChildren().get(0); // Assume ATOMIC is the first child
        String atomicType = typeof(atomicNode);
        String returnType = typeof(commandNode);
        return atomicType.equals(returnType);
    }

    private boolean typecheckAssign(Node commandNode) {
        Node vNameNode = commandNode.getChildren().get(0); // VNAME
        Node termNode = commandNode.getChildren().get(1); // TERM
        String vNameType = typeof(vNameNode);
        String termType = typeof(termNode);
        return vNameType.equals(termType);
    }

    private boolean typecheckCall(Node commandNode) {
        // Implement the logic for CALL commands
        return true; // Placeholder
    }

    public boolean typecheckLOCVARS(Node locVarsNode) {
        // Assuming locVarsNode has children that contain VTYP and VNAME pairs
        List<Node> children = locVarsNode.getChildren();

        for (int i = 0; i < children.size(); i += 2) { // Step by 2 (VTYP, VNAME)
            String vType = children.get(i).getSymbol(); // VTYP
            String vName = children.get(i + 1).getSymbol(); // VNAME

            // Get the type from the symbol table
            String typeFromSymbolTable = symbolTable.getType(vName);

            // Link the type in the symbol table
            symbolTable.addSymbol(vName, vType, null); // Assume value is null for declaration

            // Ensure the types match
            if (!vType.equals(typeFromSymbolTable)) {
                throw new RuntimeException("Type mismatch for variable " + vName);
            }
        }

        return true; // Return true if all variable declarations are correct
    }

    private String typeofCall(Node callNode) {
        // Assuming FNAME is the first child and we need to validate its type
        Node fNameNode = callNode.getChildren().get(0); // FNAME
        // Implement logic to check the return type of the function here
        // return symbolTable.getReturnType(fNameNode.getSymbol()); // Consult symbol table for return type
        return "null";
    }

    public boolean typecheckSUBFUNCS(Node subFuncsNode) {
        // Assuming subFuncsNode is a single node containing all functions
        return typecheckFUNCTIONS(subFuncsNode); // Call the existing typecheck for FUNCTIONS
    }

    private boolean typecheckFUNCTIONS(Node functionsNode) {
        for (Node function : functionsNode.getChildren()) {
            if (!typecheckDECL(function)) {
                return false;
            }
        }
        return true;
    }

    public boolean typecheckBODY(Node bodyNode) {
        // Assuming bodyNode has children in the order: PROLOG, LOCVARS, ALGO, EPILOG, SUBFUNCS
        List<Node> children = bodyNode.getChildren();

        // Ensure correct number of children
        if (children.size() != 5) {
            throw new RuntimeException("Invalid BODY structure. Expected 5 components.");
        }

        // Decompose the body into its components
        Node prologNode = children.get(0);  // PROLOG
        Node locVarsNode = children.get(1); // LOCVARS
        Node algoNode = children.get(2);     // ALGO
        Node epilogNode = children.get(3);   // EPILOG
        Node subFuncsNode = children.get(4); // SUBFUNCS

        // Type check each component and combine results
        return typecheckPROLOG(prologNode)
                && typecheckLOCVARS(locVarsNode)
                && typecheckALGO(algoNode)
                && typecheckEPILOG(epilogNode)
                && typecheckSUBFUNCS(subFuncsNode);
    }

    public boolean typecheckPROLOG(Node prologNode) {
        // Base case for type-checking PROLOG
        return true; // Always returns true
    }

    public boolean typecheckEPILOG(Node epilogNode) {
        // Base case for type-checking EPILOG
        return true; // Always returns true
    }

    private boolean typecheckDECL(Node declNode) {
        Node headerNode = declNode.getChildren().get(0); // HEADER
        Node bodyNode = declNode.getChildren().get(1); // BODY
        return typecheckHEADER(headerNode) && typecheckBODY(bodyNode);
    }

    public boolean typecheckHEADER(Node headerNode) {
        // Assuming headerNode has children in the order: FTYP, FNAME, VNAME1, VNAME2, VNAME3
        List<Node> children = headerNode.getChildren();

        // Ensure correct number of children
        if (children.size() != 5) {
            throw new RuntimeException("Invalid HEADER structure. Expected 5 components.");
        }

        // Decompose the header into its components
        Node ftypNode = children.get(0);    // FTYP
        Node fnameNode = children.get(1);    // FNAME
        Node vname1Node = children.get(2);    // VNAME1
        Node vname2Node = children.get(3);    // VNAME2
        Node vname3Node = children.get(4);    // VNAME3

        // Get the return type from FTYP
        String returnType = typeof(ftypNode);
        String functionName = fnameNode.getSymbol(); // Assuming FNAME has the function's name

        // Link the function's return type to the symbol table
        if (!symbolTable.containsSymbol(functionName)) {
            throw new RuntimeException("Function " + functionName + " is not declared.");
        }

        // Retrieve the function identifier from the symbol table
        String functionId = functionName; // Assuming this is how you get the function's ID
        symbolTable.link(returnType, functionId); // Link the return type to the function

        // Ensure that the function name type matches the return type
        if (!returnType.equals(symbolTable.getType(functionId))) {
            return false; // Return type mismatch
        }

        // Check types of VNAME1, VNAME2, and VNAME3
        String typeVName1 = typeof(vname1Node);
        String typeVName2 = typeof(vname2Node);
        String typeVName3 = typeof(vname3Node);

        // Verify all parameters are of type 'n' (numeric)
        if (typeVName1.equals("n") && typeVName2.equals("n") && typeVName3.equals("n")) {
            return true; // All parameter types are numeric
        } else {
            return false; // Parameter type mismatch
        }
    }

    // Method to typecheck COND
    public String typecheckCOND(Node condNode) {
        // if (condNode.isSIMPLE()) {
        //     return typecheckSIMPLE(condNode);
        // } else if (condNode.isCOMPOSIT()) {
        //     return typecheckCOMPOSIT(condNode);
        // }
        return "u"; // Undefined for other cases
    }

    // Method to typecheck SIMPLE
    public String typecheckSIMPLE(Node simpleNode) {
        Node binopNode = simpleNode.getChild(0); // Assuming first child is BINOP
        Node atomic1Node = simpleNode.getChild(1); // Assuming second child is ATOMIC1
        Node atomic2Node = simpleNode.getChild(2); // Assuming third child is ATOMIC2

        String binopType = typeof(binopNode);
        String atomic1Type = typeof(atomic1Node);
        String atomic2Type = typeof(atomic2Node);

        // Check conditions for SIMPLE
        if (binopType.equals(atomic1Type) && atomic1Type.equals(atomic2Type) && atomic1Type.equals("b")) {
            return "b"; // Both operands are boolean
        } else if (binopType.equals("c") && atomic1Type.equals("n") && atomic2Type.equals("n")) {
            return "b"; // Comparison type with numeric operands
        } else {
            return "u"; // Undefined
        }
    }

    // Method to typecheck COMPOSIT
    public String typecheckCOMPOSIT(Node compositNode) {
        if (compositNode.symbol.equals("BINOP")) {
            Node simple1Node = compositNode.getChild(0); // First SIMPLE
            Node simple2Node = compositNode.getChild(1); // Second SIMPLE

            String binopType = typeof(compositNode);
            String simple1Type = typecheckSIMPLE(simple1Node);
            String simple2Type = typecheckSIMPLE(simple2Node);

            // Check conditions for COMPOSIT with binary operation
            if (binopType.equals(simple1Type) && simple1Type.equals(simple2Type) && simple1Type.equals("b")) {
                return "b"; // Both operands are boolean
            } else {
                return "u"; // Undefined
            }
        } else if (compositNode.symbol.equals("UNOP")) {
            Node simpleNode = compositNode.getChild(0); // Assuming UNOP has one SIMPLE

            String unopType = typeof(compositNode);
            String simpleType = typecheckSIMPLE(simpleNode);

            // Check conditions for COMPOSIT with unary operation
            if (unopType.equals(simpleType) && simpleType.equals("b")) {
                return "b"; // Result is boolean
            } else {
                return "u"; // Undefined
            }
        }

        return "u"; // Undefined for other cases
    }

    private String getMatchingFunctionType(Node returnCommand) {
        // Implement logic to determine the type of the function being returned from
        return "n"; // Placeholder
    }

    private boolean typecheckOp(Node opNode) {
        Node binopNode = opNode.getChildren().get(0); // BINOP
        Node arg1Node = opNode.getChildren().get(1); // ARG1
        Node arg2Node = opNode.getChildren().get(2); // ARG2

        String binopType = typeof(binopNode);
        String arg1Type = typeof(arg1Node);
        String arg2Type = typeof(arg2Node);

        if (binopType.equals(arg1Type) && arg1Type.equals(arg2Type)) {
            if (arg1Type.equals("b")) {
                return true; // Boolean operation
            } else if (arg1Type.equals("n")) {
                return true; // Numeric operation
            } else if (binopType.equals("c") && arg1Type.equals("n") && arg2Type.equals("n")) {
                return true; // Comparison operation
            }
        }

        return false; // Undefined operation
    }

    private String typeofUnop(Node unopNode) {
        String operator = unopNode.getSymbol();
        switch (operator) {
            case "not":
                return "b"; // Boolean type
            case "sqrt":
                return "n"; // Numeric type
            // Add more unary operations as needed
            default:
                return null; // Undefined
        }
    }

    private String typeofOp(Node opNode) {
        Node binopNode = opNode.getChildren().get(0); // Assuming BINOP is the first child
        Node arg1Node = opNode.getChildren().get(1); // Assuming ARG1 is the second child
        Node arg2Node = opNode.getChildren().get(2); // Assuming ARG2 is the third child

        String binopType = typeof(binopNode);
        String arg1Type = typeof(arg1Node);
        String arg2Type = typeof(arg2Node);

        if (binopType.equals(arg1Type) && arg1Type.equals(arg2Type)) {
            if (arg1Type.equals("b")) {
                return "b"; // Boolean type
            } else if (arg1Type.equals("n")) {
                return "n"; // Numeric type
            } else if (binopType.equals("c") && arg1Type.equals("n") && arg2Type.equals("n")) {
                return "b"; // Comparison operation
            }
        }

        return "u"; // Undefined
    }

    private String typeofBinop(Node binopNode) {
        String operator = binopNode.getSymbol();
        switch (operator) {
            case "or":
            case "and":
                return "b"; // Boolean type
            case "eq":
            case "grt":
                return "c"; // Comparison type
            case "add":
            case "sub":
            case "mul":
            case "div":
                return "n"; // Numeric type
            default:
                return null; // Undefined
        }
    }

}

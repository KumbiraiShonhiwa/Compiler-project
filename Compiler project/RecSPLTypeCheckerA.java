import java.util.Map;

public class RecSPLTypeCheckerA {
    private SymbolTable symbolTable;
    private String currentFunctionReturnType; 
    public RecSPLTypeCheckerA(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.currentFunctionReturnType = null;
        
    }

    // Main typecheck method
    public boolean typecheck(SyntaxTree syntaxTree) {
        return typecheckNode(syntaxTree.root.getFirstChild());
    }

    // Type checking method for a general node
    private boolean typecheckNode(Node node) {
        System.out.println("Typecheck start  :"+node.symbol);
        switch (node.symbol) {

            case "PROG":
                return typecheckPROG(node);
            case "GLOBVARS":
                return typecheckGLOBVARS(node);
            case "ALGO":
                return typecheckALGO(node);
            case "COMMAND":
                return typecheckCOMMAND(node);
                case "INSTRUC":
                    return typecheckINSTRUC(node);
                    case "FUNCTIONS":
                        return typecheckFUNCTIONS(node);
                        case "DECL":
                            return typecheckDECL(node);
                            case "BODY":
                                return typecheckBODY(node);
                                case "LOCVARS":
                                    return typecheckLOCVARS(node);
                                    case "EPILOG":
                                        return typecheckEPILOG(node);
                                        case "Header":
                                        return typecheckHEADER(node);
            default:
                return false;
        }
    }

    public boolean typecheckPROG(Node prog) {
        System.out.println("Typecheck prog :"+prog.symbol);
        symbolTable.enterScope(); // Start a new global scope
        boolean result = typecheckGLOBVARS(prog.getChild("GLOBVARS")) &&
                         typecheckALGO(prog.getChild("ALGO")) &&
                         typecheckFUNCTIONS(prog.getChild("FUNCTIONS"));
        symbolTable.exitScope(); // Exit the global scope
        return result;
    }

    // Type check for global variables (GLOBVARS)
    private boolean typecheckGLOBVARS(Node globvars) {
        System.out.println("Typecheck globvars :"+globvars);
        if (globvars == null) return true;  // Base case
        String type = typeof(globvars.getChild("VTYPE"));
        //System.out.println("done");

        String id = 
        globvars.getChild("VNAME").symbol;
       

        symbolTable.addSymbol(id, type, null); // Add to symbol table with the identified type
       // System.out.println(type+"  done  "+id);
        return typecheckGLOBVARS(globvars.getChild("GLOBVARS"));
    }

    // Type check for the algorithm block (ALGO)
    private boolean typecheckALGO(Node algo) {
        System.out.println("Typecheck algo  :"+algo.symbol);
        return typecheckINSTRUC(algo.getChild("INSTRUC"));
    }

    // Type check for instructions (INSTRUC)
    private boolean typecheckINSTRUC(Node instruc) {
        System.out.println("Typecheck instruc  :"+instruc.symbol);
        if (instruc == null) return true; // Base case
        return typecheckCOMMAND(instruc.getChild("COMMAND")) &&
               typecheckINSTRUC(instruc.getChild("INSTRUC2"));
    }

    // Type check for individual commands (COMMAND)
    private boolean typecheckCOMMAND(Node command) {
        System.out.println("Typecheck command  :"+command.symbol);
        switch (command.symbol) {
            case "COMMAND":
                return typecheckCOMMAND(command.getFirstChild());
            case "skip":
            case "halt":
                return true; // Valid commands
            case "print":
                return typecheckPRINT(command);
            case "return":
                return typecheckRETURN(command);
            case "ASSIGN":
            //System.out.println("on assign");
                return typecheckASSIGN(command);
            case "CALL":
                return typecheckCALL(command.getChild("CALL"));
            case "BRANCH":
                return typecheckBRANCH(command.getChild("BRANCH"));
            default:
            //System.out.println("unrecognised");
                return false; // Unrecognized command
        }
    }

    // Type check for the 'print' command
    private boolean typecheckPRINT(Node printCommand) {
        System.out.println("Typecheck printcommand :"+printCommand.symbol);
        String atomicType = typeof(printCommand.getChild("ATOMIC"));
        return atomicType.equals("n") || atomicType.equals("t");
    }

    // Type check for the 'return' command inside a function
    private boolean typecheckRETURN(Node returnCommand) {
        System.out.println("Typecheck returncommand  :"+returnCommand.symbol);
         // Function scope management needed
        String atomicType = typeof(returnCommand.getChild("ATOMIC"));
        return atomicType.equals(currentFunctionReturnType);
    }

    // Type check for assignment (ASSIGN)

    private boolean typecheckASSIGN(Node assign) {
    
    String varName = assign.getChild("VNAME").symbol;
    String varType = symbolTable.getType(varName);
    if (varType == null) {
        System.out.println("Variable not declared: " + varName);
        return false; // Variable not declared
    }
   // System.out.println(varType+" on assign "+assign.symbol);
    Node rightSide = assign.getFirstChild();
    if (rightSide.symbol.equals("input")) {
        return varType.equals("n"); // Only numeric input allowed
    } else if (rightSide.symbol.equals("=")) {
        String termType = typeof(assign.getChild("TERM"));
        return varType.equals(termType); // Types must match
    }
   // System.out.println(rightSide+" default "+rightSide.symbol);

    return true; 
}

    // Type check for a function call (CALL)
    private boolean typecheckCALL(Node call) {
        System.out.println("Typecheck call :"+call.symbol);
        String funcName = call.getChild("FNAME").symbol;
        String returnType = symbolTable.getType(funcName);
        return returnType != null && returnType.equals("v");
    }

    // Type check for branching (BRANCH)
    private boolean typecheckBRANCH(Node branch) {
        System.out.println("Typecheck branck  :"+branch.symbol);
        String condType = typeof(branch.getChild("COND"));
        if (!condType.equals("b")) return false; // Condition must be boolean
        return typecheckALGO(branch.getChild("ALGO1")) &&
               typecheckALGO(branch.getChild("ALGO2"));
    }

    // Type check for functions (FUNCTIONS)
    private boolean typecheckFUNCTIONS(Node functions) {

        System.out.println("Typecheck funcs :"+functions.symbol);
        if (functions == null) return true; // Base case
        return typecheckDECL(functions.getChild("DECL")) &&
               typecheckFUNCTIONS(functions.getChild("FUNCTIONS2"));
    }

    // Type check for function declaration (DECL)
    private boolean typecheckDECL(Node decl) {
        System.out.println("Typecheck declr  :"+decl.symbol);
        symbolTable.enterScope(); // Enter a new function scope
        String returnType = typeof(decl.getChild("HEADER").getChild("FTYP"));
        String functionName = decl.getChild("HEADER").getChild("FNAME").symbol;
        symbolTable.addSymbol(functionName, returnType, null); // Add function to symbol table
        currentFunctionReturnType = returnType; // Set the current function's return type
        
        boolean result = typecheckBODY(decl.getChild("BODY"));
        symbolTable.exitScope(); // Exit the function scope
        currentFunctionReturnType = null; // Reset after exiting the scope
        return result;
    }

    // Type check for function header (HEADER)
    private boolean typecheckHEADER(Node header) {
        System.out.println("Typecheck header :" +header.symbol);
        String returnType = typeof(header.getChild("FTYP"));
        String functionName = header.getChild("FNAME").symbol;
        symbolTable.addSymbol(functionName, returnType, null); // Add function to symbol table
        String paramType1 = typeof(header.getChild("VNAME"));
        String paramType2 = typeof(header.getChild("VNAME"));
        String paramType3 = typeof(header.getChild("VNAME"));
        return paramType1.equals("n") && paramType2.equals("n") && paramType3.equals("n");
    }

    // Type check for function body (BODY)
    private boolean typecheckBODY(Node body) {
        System.out.println("Typecheck body :"+body.symbol);
        return typecheckPROLOG(body.getChild("PROLOG")) &&
               typecheckLOCVARS(body.getChild("LOCVARS")) &&
               typecheckALGO(body.getChild("ALGO")) &&
               typecheckEPILOG(body.getChild("EPILOG")) &&
               typecheckSUBFUNCS(body.getChild("SUBFUNCS"));
    }

    // Base case for prolog and epilog
    private boolean typecheckPROLOG(Node prolog) {
        System.out.println("Typecheck prolog  :"+prolog.symbol);
        return true;
    }

    private boolean typecheckEPILOG(Node epilog) {
        System.out.println("Typecheck epilog  :"+epilog.symbol);
        return true;
    }

    // Type check for local variables (LOCVARS)
    private boolean typecheckLOCVARS(Node locvars) {
        System.out.println("Typecheck locvar  :"+locvars.symbol);
        if (locvars == null) return true;
        for (Node var : locvars.children) {
            String type = typeof(var.getChild("VTYPE"));
            String id = var.getChild("VNAME").symbol;
            symbolTable.addSymbol(id, type, null);
        }
        return true;
    }

    // Type check for sub-functions inside a function (SUBFUNCS)
    private boolean typecheckSUBFUNCS(Node subfuncs) {
        System.out.println("Typecheck subfunc  :"+subfuncs.symbol);
        return typecheckFUNCTIONS(subfuncs.getChild("FUNCTIONS"));
    }

    private String getCurrentFunctionReturnType() {
        System.out.println("getcurrreturntype  :");
        
        return currentFunctionReturnType;
    }

    // Determine the type of a node
    private String typeof(Node node) {
        System.out.println("Typecheck typeof :"+node.symbol);
       
        switch (node.symbol) {
            case "VTYPE":
               return typeof(node.getFirstChild());
            case "NUM":
                return "n";
            case "TEXT":
                return "t";
            case "VNAME":
                return symbolTable.getType(node.symbol);
            case "CONST":
                return node.symbol.equals("N") ? "n" : "t";
            case "CALL":
                return symbolTable.getType(node.getChild("FNAME").symbol);
            case "UNOP":
            case "BINOP":
                return typeofOP(node);
            default:
                return "u";
        }
    }

    // Handle operators (UNOP and BINOP)
    private String typeofOP(Node op) {
        System.out.println("Typecheck typeop  :");
        String argType1 = typeof(op.getChild("ARG1"));
        String argType2 = op.hasChild("ARG2") ? typeof(op.getChild("ARG2")) : argType1;
        switch (op.symbol) {
            case "add":
            case "sub":
            case "mul":
            case "div":
                return (argType1.equals("n") && argType2.equals("n")) ? "n" : "u";
            case "and":
            case "or":
                return (argType1.equals("b") && argType2.equals("b")) ? "b" : "u";
            case "eq":
            case "grt":
                return (argType1.equals("n") && argType2.equals("n")) ? "b" : "u";
            case "not":
                return argType1.equals("b") ? "b" : "u";
            case "sqrt":
                return argType1.equals("n") ? "n" : "u";
            default:
                return "u";
        }
    }
}

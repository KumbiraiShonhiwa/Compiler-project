
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class RecSPLParser {

    // enum TokenType {
    //     MAIN("main"), BEGIN("begin"), END("end"), NUM("num"), TEXT("text"),
    //     SKIP("skip"), HALT("halt"), PRINT("print"), INPUT("input"),
    //     IF("if"), THEN("then"), ELSE("else"),
    //     OR("or"), AND("and"), EQ("eq"), GRT("grt"),
    //     ADD("add"), SUB("sub"), MUL("mul"), DIV("div"),
    //     NOT("not"), SQRT("sqrt"), VOID("void"), RETURN("return"),
    //     VNAME("V_[a-z]([a-z]|[0-9])*"), FNAME("F_[a-z]([a-z]|[0-9])*"),
    //     CONST("\"[A-Z][a-z]{0,7}\""), // Corrected: Removed duplicate NUM definition
    //     SEMICOLON(";"), EQUALS("="), COMMA(","), LESS("<"),
    //     LPAREN("\\("), RPAREN("\\)"), LBRACE("\\{"), RBRACE("\\}"),
    //     WHITESPACE("[ \t\f\r\n]+"),
    //     BINOP("eq|grt|add|sub|mul|div");
    //     public final String pattern;
    //     private TokenType(String pattern) {
    //         this.pattern = pattern;
    //     }
    // }
    // static class Token {
    //     public TokenType type;
    //     public String data;
    //     public int id;
    //     public Token(TokenType type, String data, int id) {
    //         this.type = type;
    //         this.data = data;
    //         this.id = id;
    //     }
    //     @Override
    //     public String toString() {
    //         return String.format("(%d: %s, \"%s\")", id, type.name(), data);
    //     }
    // }
    private List<Token> tokens;
    private int currentTokenIndex;
    private int nodeIdCounter = 0;
    private SyntaxTree syntaxTree;
    private Stack<Map<String, String>> symbolTableStack;
    private Map<String, FunctionSignature> functionTable;
    private String darkart = "darkart";

    private int getNextNodeId() {
        return nodeIdCounter++;
    }

    public RecSPLParser(List<Token> tokens, Node node) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.syntaxTree = new SyntaxTree(node);
        this.symbolTableStack = new Stack<>();
        this.functionTable = new HashMap<>();

        // Push the global scope onto the stack
        this.symbolTableStack.push(new HashMap<>());
    }

    public RecSPLParser(RecSPLParser parser, Node node) {
        this.tokens = parser.tokens;
        this.currentTokenIndex = 0;
        this.syntaxTree = new SyntaxTree(node);
        this.symbolTableStack = new Stack<>();
        this.functionTable = parser.functionTable;
        System.out.println("Function table size: " + functionTable.size());
        functionTable.forEach((key, value) -> {
            System.out.println(key + " : " + value);
            System.out.println(value.getParamTypes());
        });
        // Push the global scope onto the stack
        this.symbolTableStack.push(new HashMap<>());

    }

    private Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        } else {
            return null; // End of input
        }

    }

    public Map<String, FunctionSignature> getFunctionTable() {
        return functionTable;
    }

    private void consume() {
        currentTokenIndex++;
    }

    private void expect(TokenType expectedType, Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token == null || token.type != expectedType) {
            throw new Exception("Expected " + expectedType + " but found " + (token != null ? token.type : "null"));
        }
        // Node childNode = new Node(nodeIdCounter++, token.type.toString());
        // parentNode.addChild(childNode);
        // consume();
        // Create a new leaf node for this token
        LeafNode leafNode = new LeafNode(parentNode.getUnid(), getNextNodeId(), token.type.toString());

        // Add the leaf node to the tree
        syntaxTree.addLeafNode(leafNode);

        // Add this leaf node's UNID as a child of the parent node
        parentNode.addChild(leafNode);

        consume(); // Move to the next token
    }

    // Recursive Descent Functions
    // <PROG> ::= main GLOBVARS ALGO FUNCTIONS
    public void parseProgram() throws Exception {
        Node programNode = new Node(nodeIdCounter++, "PROG");
        this.syntaxTree.root.addChild(programNode);

        expect(TokenType.MAIN, programNode); // match 'main'
        parseGlobVars(programNode);          // match global variables
        parseAlgo(programNode);              // match algorithm block
        parseFunctions(programNode);         // match functions
    }

    // <GLOBVARS> ::= VTYP VNAME , GLOBVARS | // nullable
    public void parseGlobVars(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token == null) {
            return;
        }
        if (token != null && token.type == TokenType.NUM || token.type == TokenType.TEXT) {

            Node globVarsNode = new Node(nodeIdCounter++, "GLOBVARS");
            syntaxTree.addInnerNode(globVarsNode);
            parentNode.addChild(globVarsNode);

            String varType = parseVType(globVarsNode);
            Token varToken = getCurrentToken();
            expect(TokenType.VNAME, globVarsNode);

            // Semantic check: Add variable to global symbol table
            Map<String, String> globalScope = symbolTableStack.peek();
            if (globalScope.containsKey(varToken.data)) {
                throw new Exception("Variable " + varToken.data + " already declared globally.");
            } else {
                globalScope.put(varToken.data, varType);
            }
            Token commaToken = getCurrentToken();
            if (commaToken == null) {
                return;
            }
            if (commaToken.type == TokenType.COMMA) {
                consume(); // consume comma
                parseGlobVars(globVarsNode); // recursively parse more global vars
            }
        } else {
            System.out.println("No global variables found");
        }
    }

    // <ALGO> ::= begin INSTRUC end
    public void parseAlgo(Node parentNode) throws Exception {
        Node algoNode = new Node(nodeIdCounter++, "ALGO");
        parentNode.addChild(algoNode);
        Token token = getCurrentToken();
        if (token == null) {
            return;
        }
        if (token.type != TokenType.VOID && token.type != TokenType.NUM) {

            expect(TokenType.BEGIN, algoNode);

            // Push a new scope (local to the algorithm block)
            symbolTableStack.push(new HashMap<>());

            parseInstruc(algoNode);

            // Pop the local scope
            symbolTableStack.pop();

            expect(TokenType.END, algoNode);
        }
    }
    // <INSTRUC> ::= COMMAND ; INSTRUC | // nullable

    public void parseInstruc(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token != null && token.type != TokenType.END) {
            Node instrucNode = new Node(nodeIdCounter++, "INSTRUC");
            parentNode.addChild(instrucNode);

            parseCommand(instrucNode);
            expect(TokenType.SEMICOLON, instrucNode);
            parseInstruc(instrucNode); // recursively parse more instructions
        }
    }

    // <COMMAND> ::= skip | halt | print ATOMIC | ASSIGN | CALL | BRANCH
    public void parseCommand(Node parentNode) throws Exception {
        Node commandNode = new Node(getNextNodeId(), "COMMAND");
        syntaxTree.addInnerNode(commandNode);
        parentNode.addChild(commandNode);

        Token token = getCurrentToken();
        switch (token.type) {
            case SKIP:
                consume(); // match 'skip'
                commandNode.setSymbol("skip");
                break;
            case HALT:
                consume(); // match 'halt'
                commandNode.setSymbol("halt");
                break;
            case PRINT:
                consume(); // match 'print'
                parseAtomic(commandNode);
                break;
            case VNAME:
                parseAssign(commandNode);
                break;
            case FNAME:
                parseCall(commandNode);
                break;
            case IF:
                parseBranch(commandNode);
                break;
            case RETURN:
                consume(); // match 'return'
                commandNode.setSymbol("return");
                parseAtomic(commandNode); // parse the atomic expression after return
                break;
            default:
                throw new Exception("Unexpected command: " + token);
        }
    }

    // Implement other rules similarly...
    // Helper methods
    private String parseAtomic(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.VNAME) {
            expect(TokenType.VNAME, parentNode); // match variable name
            return getVariableType(token.data); // return the type of the variable
        } else if (token.type == TokenType.CONST) {
            expect(TokenType.CONST, parentNode); // match constant
            return "text"; // Constants are treated as "text"
        } else {
            throw new Exception("Expected atomic value but found " + token.type);
        }
    }

    private void parseAssign(Node parentNode) throws Exception {
        Node assignNode = new Node(nodeIdCounter++, "ASSIGN");
        syntaxTree.addInnerNode(assignNode);
        parentNode.addChild(assignNode);

        Token varToken = getCurrentToken();
        expect(TokenType.VNAME, assignNode);

        // Check if variable is declared in current scope or global scope
        if (!isVariableDeclared(varToken.data)) {
            throw new Exception("Variable " + varToken.data + " not declared.");
        }

        Token token = getCurrentToken();
        if (token.type == TokenType.EQUALS) {
            consume(); // match '='
            parseTerm(assignNode);

            // Check if assigned value matches the declared type
            String varType = getVariableType(varToken.data);
            String assignedType = inferExpressionType();
            if (!varType.equals(assignedType)) {
                throw new Exception("Type mismatch: cannot assign " + assignedType + " to " + varType + ".");
            }
        } else if (token.type == TokenType.INPUT) {
            consume(); // match '< input'
        } else {
            throw new Exception("Invalid assignment syntax");
        }
    }

    private void parseCall(Node parentNode) throws Exception {
        Node callNode = new Node(nodeIdCounter++, "CALL");
        syntaxTree.addInnerNode(callNode);
        parentNode.addChild(callNode);

        Token funcToken = getCurrentToken();
        expect(TokenType.FNAME, callNode);

        // Check if the function is declared
        if (darkart.equals("notdarkart")) {
            System.out.println("Not Dark art");

            if (!functionTable.containsKey(funcToken.data)) {
                throw new Exception("Function " + funcToken.data + " not declared.");
            }
            if (functionTable == null) {
                System.out.println("Function table is null");
            }
            FunctionSignature signature = functionTable.get(funcToken.data);
            expect(TokenType.LPAREN, callNode);
            List<String> argumentTypes = parseArguments(callNode, signature.getParamTypes()); // Match arguments
            expect(TokenType.RPAREN, callNode);

            System.out.println(argumentTypes.size());
            System.out.println(signature.getParamTypes().size());

            // Check if the number and types of arguments match the function signature
            if (signature.matchesArgumentTypes(argumentTypes)) {
                throw new Exception("Function " + funcToken.data + " called with incorrect number of arguments.");
            }

            for (int i = 0; i < functionTable.size(); i++) {
                if (signature == functionTable.get(funcToken.data)) {
                    System.out.println("Function signature matches");
                    functionTable.remove(funcToken.data);
                } else {
                    System.out.println("Function signature does not match");
                }
            }

        } else {

            expect(TokenType.LPAREN, callNode);
            parseAtomic(callNode);
            expect(TokenType.COMMA, callNode);
            parseAtomic(callNode);
            expect(TokenType.COMMA, callNode);
            parseAtomic(callNode);
            expect(TokenType.RPAREN, callNode);
        }
    }

    private List<String> parseArguments(Node parentNode, List<String> expectedTypes) throws Exception {
        List<String> argumentTypes = new ArrayList<>();
        Token token = getCurrentToken();
        if (token.type == TokenType.VNAME || token.type == TokenType.CONST) {
            Node argumentsNode = new Node(nodeIdCounter++, "ARGUMENTS");
            syntaxTree.addInnerNode(argumentsNode);
            parentNode.addChild(argumentsNode);

            argumentTypes.add(parseAtomic(argumentsNode));

            if (getCurrentToken().type == TokenType.COMMA) {
                consume(); // Consume ',' and parse next argument
                argumentTypes.addAll(parseArguments(argumentsNode, expectedTypes));
            }
        }
        return argumentTypes;
    }

    // Helper function to check if a variable is declared in any active scope
    private boolean isVariableDeclared(String varName) {
        for (Map<String, String> scope : symbolTableStack) {
            if (scope.containsKey(varName)) {
                return true;
            }
        }
        return false;
    }

    private String getVariableType(String varName) {
        for (Map<String, String> scope : symbolTableStack) {
            if (scope.containsKey(varName)) {
                return scope.get(varName);
            }
        }
        return null; // Variable not found
    }

    // Helper function to infer the type of an expression
    private String inferExpressionType() {
        // Example implementation, returning "num" or "text" based on the expression
        return "num"; // placeholder
    }

    private void parseBranch(Node parentNode) throws Exception {
        Node branchNode = new Node(nodeIdCounter++, "BRANCH");
        syntaxTree.addInnerNode(branchNode);
        parentNode.addChild(branchNode);

        expect(TokenType.IF, branchNode);
        parseCond(branchNode);
        expect(TokenType.THEN, branchNode);
        parseAlgo(branchNode);
        expect(TokenType.ELSE, branchNode);
        parseAlgo(branchNode);
    }

    private void parseCond(Node parentNode) throws Exception {
        Node condNode = new Node(nodeIdCounter++, "COND");
        syntaxTree.addInnerNode(condNode);
        parentNode.addChild(condNode);

        parseSimple(condNode);
    }

    private void parseSimple(Node parentNode) throws Exception {
        Node simpleNode = new Node(nodeIdCounter++, "SIMPLE");
        syntaxTree.addInnerNode(simpleNode);
        parentNode.addChild(simpleNode);
        expect(TokenType.BINOP, simpleNode); // BINOP can be eq, grt, etc.
        expect(TokenType.LPAREN, simpleNode);
        parseAtomic(simpleNode);
        expect(TokenType.COMMA, simpleNode);
        parseAtomic(simpleNode);
        expect(TokenType.RPAREN, simpleNode);

        // parseAtomic(simpleNode);
        // expect(TokenType.BINOP, simpleNode); // BINOP can be eq, grt, etc.
        // parseAtomic(simpleNode);
    }

    private void parseTerm(Node parentNode) throws Exception {
        Node termNode = new Node(nodeIdCounter++, "TERM");
        syntaxTree.addInnerNode(termNode);
        parentNode.addChild(termNode);

        parseAtomic(termNode);
    }

    // <FUNCTIONS> ::= // nullable | DECL FUNCTIONS
    public void parseFunctions(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token != null  && token.type == TokenType.END) {
            return;
        }
        if (token != null && (token.type == TokenType.NUM || token.type == TokenType.VOID)) {
            Node functionsNode = new Node(nodeIdCounter++, "FUNCTIONS");
            syntaxTree.addInnerNode(functionsNode);
            parentNode.addChild(functionsNode);

            parseDecl(functionsNode); // Parse a single function declaration
            parseFunctions(functionsNode); // Recursively parse more functions (if any)
        } else if (token == null) {
            System.out.println("End of input");
        } else if (token.type != TokenType.NUM && token.type != TokenType.VOID) {
            throw new Exception("Expected function type but found " + token);
        }
    }

    // <DECL> ::= HEADER BODY
    private void parseDecl(Node parentNode) throws Exception {
        Node declNode = new Node(nodeIdCounter++, "DECL");
        syntaxTree.addInnerNode(declNode);
        parentNode.addChild(declNode);

        parseHeader(declNode);
        parseBody(declNode);
    }

// <HEADER> ::= FTYP FNAME( VNAME , VNAME , VNAME )
    private void parseHeader(Node parentNode) throws Exception {
        Node headerNode = new Node(nodeIdCounter++, "HEADER");
        syntaxTree.addInnerNode(headerNode);
        parentNode.addChild(headerNode);

        String returnType = parseFType(headerNode); // Parse function return type
        Token funcToken = getCurrentToken();
        expect(TokenType.FNAME, headerNode); // Parse function name
        String functionName = funcToken.data;

        expect(TokenType.LPAREN, headerNode); // Parse '('
        List<String> parameterTypes = parseParams(headerNode); // Parse parameters
        expect(TokenType.RPAREN, headerNode); // Parse ')'

        // Add function signature to the function table
        if (functionTable.containsKey(functionName)) {
            throw new Exception("Function " + functionName + " is already declared.");
        } else {
            functionTable.put(functionName, new FunctionSignature(returnType, parameterTypes));
        }
    }

    private List<String> parseParams(Node parentNode) throws Exception {
        List<String> parameterTypes = new ArrayList<>();
        Token token = getCurrentToken();
        if (token.type == TokenType.VNAME) {
            Node paramsNode = new Node(nodeIdCounter++, "PARAMS");
            syntaxTree.addInnerNode(paramsNode);
            parentNode.addChild(paramsNode);

            // String paramType = parseVType(paramsNode); // Parse parameter type
            // parameterTypes.add(paramType);
            expect(TokenType.VNAME, paramsNode); // Parse parameter name

            if (getCurrentToken().type == TokenType.COMMA) {
                consume(); // consume ','
                parameterTypes.addAll(parseParams(paramsNode)); // Recursively parse more parameters
            }
        }
        return parameterTypes;
    }

// <BODY> ::= PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
    private void parseBody(Node parentNode) throws Exception {
        Node bodyNode = new Node(nodeIdCounter++, "BODY");
        syntaxTree.addInnerNode(bodyNode);
        parentNode.addChild(bodyNode);

        expect(TokenType.PROLOG, bodyNode); // Parse 'PROLOG'
        parseLocVars(bodyNode); // Parse local variables
        parseAlgo(bodyNode); // Parse algorithm block
        expect(TokenType.EPILOG, bodyNode); // Parse 'EPILOG'
        parseFunctions(bodyNode); // Parse functions
        expect(TokenType.END, bodyNode); // Parse 'end'
    }

// <LOCVARS> ::= VTYP VNAME , VTYP VNAME , VTYP VNAME ,
    private void parseLocVars(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.TEXT) {

            Node locVarsNode = new Node(nodeIdCounter++, "LOCVARS");
            syntaxTree.addInnerNode(locVarsNode);
            parentNode.addChild(locVarsNode);

            String varType = parseVType(locVarsNode);
            Token varToken = getCurrentToken();
            expect(TokenType.VNAME, locVarsNode);  // match variable name

            Map<String, String> LocalScope = symbolTableStack.peek();
            if (LocalScope.containsKey(varToken.data)) {
                throw new Exception("Variable " + varToken.data + " already declared globally.");
            } else {
                LocalScope.put(varToken.data, varType);
            }

            if (getCurrentToken().type == TokenType.COMMA) {
                consume();                 // consume the comma
                parseLocVars(locVarsNode); // recursively parse more local variables
            }
        }
    }

    // <VTYP> ::= num | text
    private String parseFType(Node parentNode) throws Exception {
        Node fTypeNode = new Node(nodeIdCounter++, "FTYP");
        syntaxTree.addInnerNode(fTypeNode);
        parentNode.addChild(fTypeNode);

        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.VOID) {
            expect(token.type, fTypeNode); // match num or void
        } else {
            throw new Exception("Expected function type (num or void) but found " + token);
        }
        return token.data;
    }

    // private String parseFType(Node parentNode) throws Exception {
    //     Node fTypeNode = new Node(nodeIdCounter++, "VTYPE");
    //     syntaxTree.addInnerNode(fTypeNode);
    //     parentNode.addChild(fTypeNode);
    //     Token currentToken = getCurrentToken();
    //     if (null == currentToken.type) {
    //         // If it's not a valid type, throw an error
    //         throw new Exception("Expected a type, but found: " + currentToken.data);
    //     } else // Check the token for a valid type
    //     {
    //         switch (currentToken.type) {
    //             case NUM:
    //                 // Add the type token  to the syntax tree
    //                 expect(TokenType.NUM, fTypeNode);
    //                 return currentToken.data;  // Return the type as a string
    //             case TEXT:
    //                 // Add the type token to the syntax tree
    //                 expect(TokenType.VOID, fTypeNode);
    //                 return currentToken.data;  // Return the type as a string
    //             case VOID:
    //             default:
    //                 // If it's not a valid type, throw an error
    //                 throw new Exception("Expected a type, but found: " + currentToken.data);
    //         }
    //     }
    // }
    private String parseVType(Node parentNode) throws Exception {
        Node vTypeNode = new Node(nodeIdCounter++, "VTYPE");
        syntaxTree.addInnerNode(vTypeNode);
        parentNode.addChild(vTypeNode);

        Token currentToken = getCurrentToken();

        if (null == currentToken.type) {
            // If it's not a valid type, throw an error
            throw new Exception("Expected a type, but found: " + currentToken.data);
        } else // Check the token for a valid type
        {
            switch (currentToken.type) {
                case NUM:
                    // Add the type token  to the syntax tree
                    expect(TokenType.NUM, vTypeNode);
                    return currentToken.data;  // Return the type as a string
                case TEXT:
                    // Add the type token to the syntax tree
                    expect(TokenType.TEXT, vTypeNode);
                    return currentToken.data;  // Return the type as a string
                case VOID:
                default:
                    // If it's not a valid type, throw an error
                    throw new Exception("Expected a type, but found: " + currentToken.data);
            }
        }
    }

    public static void main(String[] args) {
        // Define the path to the XML file in the project directory
        String inputFile = "Compiler project//input8.txt"; // Adjust this path as per your project structure
        String xmlOutputFile = "tokens_output.xml";
        String xmlOutputFileSyntaxTree = "syntax_tree.xml";
        try {
            // Step 1: Read input file
            String input = new String(Files.readAllBytes(Paths.get(inputFile)));
            System.out.println("Input file contents:\n" + input);

            // Step 2: Lexing process - generate tokens
            List<Token> tokens = RecSPLLexer.lex(input);
            for (int i = 0; i < tokens.size(); i++) {
                System.out.println(tokens.get(i));
            }

            // Step 3: Parsing process        
            Node rootNode = new Node(0, "ROOT");
            RecSPLParser parser = new RecSPLParser(tokens, rootNode); // Pass tokens to the parser

            parser.parseProgram(); // Start parsing the 

            parser.functionTable.forEach((key, value) -> {
                System.out.println(key + " : " + value);
                System.out.println(value.getParamTypes());
            });
            // Step 4: Semantic Analysis of functions
            Node functionNode = new Node(0, "ROOT");
            RecSPLParser parserFunction = new RecSPLParser(parser, functionNode);
            parserFunction.darkart = "notdarkart";
            parserFunction.parseProgram();

            // Output the syntax tree
            // RecSPLLexer.writeTokensToXML(tokens, xmlOutputFile);
            // parser.syntaxTree.toXML(xmlOutputFileSyntaxTree);
            // System.out.println(syntaxTreeXML);

            System.out.println("Parsing completed successfully. No syntax errors found.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}

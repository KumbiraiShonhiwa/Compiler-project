
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

    private int getNextNodeId() {
        return nodeIdCounter++;
    }

    public RecSPLParser(List<Token> tokens, Node node) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.syntaxTree = new SyntaxTree(node);
    }

    private Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        } else {
            return null; // End of input
        }
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
        // parentNode.addChild(childNode.unid);
        // consume();
        // Create a new leaf node for this token
        LeafNode leafNode = new LeafNode(parentNode.getUnid(), getNextNodeId(), token.type.toString());

        // Add the leaf node to the tree
        syntaxTree.addLeafNode(leafNode);

        // Add this leaf node's UNID as a child of the parent node
        parentNode.addChild(leafNode.getUnid());

        consume(); // Move to the next token
    }

    // Recursive Descent Functions
    // <PROG> ::= main GLOBVARS ALGO FUNCTIONS
    public void parseProgram() throws Exception {
        Node programNode = new Node(nodeIdCounter++, "PROG");
        this.syntaxTree.root.addChild(programNode.unid);

        expect(TokenType.MAIN, programNode); // match 'main'
        parseGlobVars(programNode);          // match global variables
        parseAlgo(programNode);              // match algorithm block
        parseFunctions(programNode);         // match functions
    }

    // <GLOBVARS> ::= VTYP VNAME , GLOBVARS | // nullable
    public void parseGlobVars(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.TEXT) {
            Node globVarsNode = new Node(nodeIdCounter++, "GLOBVARS");
            parentNode.addChild(globVarsNode.unid);

            parseVType(globVarsNode);
            expect(TokenType.VNAME, globVarsNode);

            if (getCurrentToken().type == TokenType.COMMA) {
                consume(); // consume comma
                parseGlobVars(globVarsNode); // recursively parse more global vars
            }
        }
    }

    // <ALGO> ::= begin INSTRUC end
    public void parseAlgo(Node parentNode) throws Exception {
        Node algoNode = new Node(nodeIdCounter++, "ALGO");
        parentNode.addChild(algoNode.unid);

        expect(TokenType.BEGIN, algoNode);
        parseInstruc(algoNode);
        expect(TokenType.END, algoNode);
    }

    // <INSTRUC> ::= COMMAND ; INSTRUC | // nullable
    public void parseInstruc(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token != null && token.type != TokenType.END) {
            Node instrucNode = new Node(nodeIdCounter++, "INSTRUC");
            parentNode.addChild(instrucNode.unid);

            parseCommand(instrucNode);
            expect(TokenType.SEMICOLON, instrucNode);
            parseInstruc(instrucNode); // recursively parse more instructions
        }
    }

    // <COMMAND> ::= skip | halt | print ATOMIC | ASSIGN | CALL | BRANCH
    public void parseCommand(Node parentNode) throws Exception {
        // Create a new inner node for the COMMAND
        Node commandNode = new Node(getNextNodeId(), "COMMAND");

        // Add the inner node to the syntax tree
        syntaxTree.addInnerNode(commandNode);

        // Add this inner node's UNID as a child of the parent node
        parentNode.addChild(commandNode.getUnid());

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
            default:
                throw new Exception("Unexpected command: " + token);
        }
    }

    // Implement other rules similarly...
    // Helper methods
    private void parseAtomic(Node parentNode) throws Exception {
        Node atomicNode = new Node(nodeIdCounter++, "ATOMIC");
        syntaxTree.addInnerNode(atomicNode);
        parentNode.addChild(atomicNode.unid);

        Token token = getCurrentToken();
        if (token.type == TokenType.VNAME || token.type == TokenType.CONST) {
            expect(token.type, atomicNode); // match atomic value
        } else {
            throw new Exception("Expected an atomic value but found " + token);
        }
    }

    private void parseAssign(Node parentNode) throws Exception {
        Node assignNode = new Node(nodeIdCounter++, "ASSIGN");
        syntaxTree.addInnerNode(assignNode);
        parentNode.addChild(assignNode.unid);

        expect(TokenType.VNAME, assignNode);
        Token token = getCurrentToken();
        if (token.type == TokenType.EQUALS) {
            consume(); // match '='
            parseTerm(assignNode);
        } else if (token.type == TokenType.INPUT) {
            consume(); // match '< input'
        } else {
            throw new Exception("Invalid assignment syntax");
        }
    }

    private void parseCall(Node parentNode) throws Exception {
        Node callNode = new Node(nodeIdCounter++, "CALL");
        syntaxTree.addInnerNode(callNode);
        parentNode.addChild(callNode.unid);

        expect(TokenType.FNAME, callNode);
        expect(TokenType.LPAREN, callNode);
        parseAtomic(callNode);
        expect(TokenType.COMMA, callNode);
        parseAtomic(callNode);
        expect(TokenType.COMMA, callNode);
        parseAtomic(callNode);
        expect(TokenType.RPAREN, callNode);
    }

    private void parseBranch(Node parentNode) throws Exception {
        Node branchNode = new Node(nodeIdCounter++, "BRANCH");
        syntaxTree.addInnerNode(branchNode);
        parentNode.addChild(branchNode.unid);

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
        parentNode.addChild(condNode.unid);

        parseSimple(condNode);
    }

    private void parseSimple(Node parentNode) throws Exception {
        Node simpleNode = new Node(nodeIdCounter++, "SIMPLE");
        syntaxTree.addInnerNode(simpleNode);
        parentNode.addChild(simpleNode.unid);

        parseAtomic(simpleNode);
        expect(TokenType.BINOP, simpleNode); // BINOP can be eq, grt, etc.
        parseAtomic(simpleNode);
    }

    private void parseTerm(Node parentNode) throws Exception {
        Node termNode = new Node(nodeIdCounter++, "TERM");
        syntaxTree.addInnerNode(termNode);
        parentNode.addChild(termNode.unid);

        parseAtomic(termNode);
    }

    // <FUNCTIONS> ::= // nullable | DECL FUNCTIONS
    public void parseFunctions(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token != null && (token.type == TokenType.NUM || token.type == TokenType.VOID)) {
            Node functionsNode = new Node(nodeIdCounter++, "FUNCTIONS");
            syntaxTree.addInnerNode(parentNode);
            parentNode.addChild(functionsNode.unid);

            parseDecl(functionsNode); // Parse a single function declaration
            parseFunctions(functionsNode); // Recursively parse more functions (if any)
        }
    }

    // <DECL> ::= HEADER BODY
    private void parseDecl(Node parentNode) throws Exception {
        Node declNode = new Node(nodeIdCounter++, "DECL");
        syntaxTree.addInnerNode(declNode);
        parentNode.addChild(declNode.unid);

        parseHeader(declNode); // Parse the function header
        parseBody(declNode);   // Parse the function body
    }

// <HEADER> ::= FTYP FNAME( VNAME , VNAME , VNAME )
    private void parseHeader(Node parentNode) throws Exception {
        Node headerNode = new Node(nodeIdCounter++, "HEADER");
        syntaxTree.addInnerNode(headerNode);
        parentNode.addChild(headerNode.unid);

        parseFType(headerNode);  // Parse function return type (num or void)
        expect(TokenType.FNAME, headerNode); // Expect a function name (FNAME)
        expect(TokenType.LPAREN, headerNode); // Expect '('
        expect(TokenType.VNAME, headerNode);  // Expect first VNAME (parameter)
        expect(TokenType.COMMA, headerNode);
        expect(TokenType.VNAME, headerNode);  // Expect second VNAME (parameter)
        expect(TokenType.COMMA, headerNode);
        expect(TokenType.VNAME, headerNode);  // Expect third VNAME (parameter)
        expect(TokenType.RPAREN, headerNode); // Expect ')'
    }

// <BODY> ::= PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
    private void parseBody(Node parentNode) throws Exception {
        Node bodyNode = new Node(nodeIdCounter++, "BODY");
        syntaxTree.addInnerNode(bodyNode);
        parentNode.addChild(bodyNode.unid);

        expect(TokenType.LBRACE, bodyNode); // Expect '{'
        parseLocVars(bodyNode);           // Parse local variables
        parseAlgo(bodyNode);              // Parse the algorithm (ALGO)
        expect(TokenType.RBRACE, bodyNode); // Expect '}'
    }

// <LOCVARS> ::= VTYP VNAME , VTYP VNAME , VTYP VNAME ,
    private void parseLocVars(Node parentNode) throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.TEXT) {
            Node locVarsNode = new Node(nodeIdCounter++, "LOCVARS");
            syntaxTree.addInnerNode(locVarsNode);
            parentNode.addChild(locVarsNode.unid);

            parseVType(locVarsNode);       // match variable type
            expect(TokenType.VNAME, locVarsNode);  // match variable name
            if (getCurrentToken().type == TokenType.COMMA) {
                consume();                 // consume the comma
                parseLocVars(locVarsNode); // recursively parse more local variables
            }
        }
    }

    // <VTYP> ::= num | text
    private void parseFType(Node parentNode) throws Exception {
        Node fTypeNode = new Node(nodeIdCounter++, "FTYP");
        syntaxTree.addInnerNode(fTypeNode);
        parentNode.addChild(fTypeNode.unid);

        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.VOID) {
            expect(token.type, fTypeNode); // match num or void
        } else {
            throw new Exception("Expected function type (num or void) but found " + token);
        }
    }

    private void parseVType(Node parentNode) throws Exception {
        Node vTypeNode = new Node(nodeIdCounter++, "VTYP");
        syntaxTree.addInnerNode(vTypeNode);
        parentNode.addChild(vTypeNode.unid);

        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.TEXT) {
            expect(token.type, vTypeNode); // match num or text
        } else {
            throw new Exception("Expected variable type (num or text) but found " + token);
        }
    }

    public static void main(String[] args) {
        // Define the path to the XML file in the project directory
        String inputFile = "Compiler project//input.txt"; // Adjust this path as per your project structure

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

            parser.parseProgram(); // Start parsing the program

            // Output the syntax tree
            String syntaxTreeXML = parser.syntaxTree.toXML();
            System.out.println(syntaxTreeXML);

            System.out.println("Parsing completed successfully. No syntax errors found.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}


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

    public RecSPLParser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
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

    private void expect(TokenType expectedType) throws Exception {
        Token token = getCurrentToken();
        if (token == null || token.type != expectedType) {
            throw new Exception("Expected " + expectedType + " but found " + (token != null ? token.type : "null"));
        }
        consume();
    }

    // Recursive Descent Functions
    // <PROG> ::= main GLOBVARS ALGO FUNCTIONS
    public void parseProgram() throws Exception {
        expect(TokenType.MAIN); // match 'main'
        parseGlobVars();        // match global variables
        parseAlgo();            // match algorithm block
        parseFunctions();       // match functions
    }

    // <GLOBVARS> ::= VTYP VNAME , GLOBVARS | // nullable
    public void parseGlobVars() throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.TEXT) {
            parseVType();
            expect(TokenType.VNAME);
            if (getCurrentToken().type == TokenType.COMMA) {
                consume(); // consume comma
                parseGlobVars(); // recursively parse more global vars
            }
        }
        // GLOBVARS is nullable, so it can return without consuming anything
    }

    // <ALGO> ::= begin INSTRUC end
    public void parseAlgo() throws Exception {
        expect(TokenType.BEGIN);
        parseInstruc();
        expect(TokenType.END);
    }

    // <INSTRUC> ::= COMMAND ; INSTRUC | // nullable
    public void parseInstruc() throws Exception {
        Token token = getCurrentToken();
        if (token != null && token.type != TokenType.END) {
            parseCommand();
            expect(TokenType.SEMICOLON);
            parseInstruc(); // recursively parse more instructions
        }
        // INSTRUC is nullable, so it can return without consuming anything
    }

    // <COMMAND> ::= skip | halt | print ATOMIC | ASSIGN | CALL | BRANCH
    public void parseCommand() throws Exception {
        Token token = getCurrentToken();
        switch (token.type) {
            case SKIP:
                consume(); // match 'skip'
                break;
            case HALT:
                consume(); // match 'halt'
                break;
            case PRINT:
                consume(); // match 'print'
                parseAtomic();
                break;
            case VNAME:
                parseAssign();
                break;
            case FNAME:
                parseCall();
                break;
            case IF:
                parseBranch();
                break;
            default:
                throw new Exception("Unexpected command: " + token);
        }
    }

    // Implement other rules similarly...
    // Helper methods
    private void parseAtomic() throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.VNAME || token.type == TokenType.CONST) {
            consume(); // match atomic value
        } else {
            throw new Exception("Expected an atomic value but found " + token);
        }
    }

    private void parseAssign() throws Exception {
        expect(TokenType.VNAME);
        Token token = getCurrentToken();
        if (token.type == TokenType.EQUALS) {
            consume(); // match '='
            parseTerm();
        } else if (token.type == TokenType.INPUT) {
            consume(); // match '< input'
        } else {
            throw new Exception("Invalid assignment syntax");
        }
    }

    private void parseCall() throws Exception {
        expect(TokenType.FNAME);
        expect(TokenType.LPAREN);
        parseAtomic();
        expect(TokenType.COMMA);
        parseAtomic();
        expect(TokenType.COMMA);
        parseAtomic();
        expect(TokenType.RPAREN);
    }

    private void parseBranch() throws Exception {
        expect(TokenType.IF);
        parseCond();
        expect(TokenType.THEN);
        parseAlgo();
        expect(TokenType.ELSE);
        parseAlgo();
    }

    private void parseCond() throws Exception {
        parseSimple(); // for simplicity, assuming SIMPLE conditions
    }

    private void parseSimple() throws Exception {
        parseAtomic();
        expect(TokenType.BINOP); // BINOP can be eq, grt, etc.
        parseAtomic();
    }

    private void parseTerm() throws Exception {
        parseAtomic(); // or recursive call for deep-nested terms
    }

    // <FUNCTIONS> ::= // nullable | DECL FUNCTIONS
    public void parseFunctions() throws Exception {
        Token token = getCurrentToken();
        if (token != null && (token.type == TokenType.NUM || token.type == TokenType.VOID)) {
            parseDecl(); // Parse a single function declaration
            parseFunctions(); // Recursively parse more functions (if any)
        }
        // FUNCTIONS is nullable, so this function can return without consuming anything
    }

// <DECL> ::= HEADER BODY
    private void parseDecl() throws Exception {
        parseHeader(); // Parse the function header
        parseBody();   // Parse the function body
    }

// <HEADER> ::= FTYP FNAME( VNAME , VNAME , VNAME )
    private void parseHeader() throws Exception {
        parseFType();  // Parse function return type (num or void)
        expect(TokenType.FNAME); // Expect a function name (FNAME)
        expect(TokenType.LPAREN); // Expect '('
        expect(TokenType.VNAME);  // Expect first VNAME (parameter)
        expect(TokenType.COMMA);
        expect(TokenType.VNAME);  // Expect second VNAME (parameter)
        expect(TokenType.COMMA);
        expect(TokenType.VNAME);  // Expect third VNAME (parameter)
        expect(TokenType.RPAREN); // Expect ')'
    }

// <BODY> ::= PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
    private void parseBody() throws Exception {
        expect(TokenType.LBRACE); // Expect '{'
        parseLocVars();           // Parse local variables
        parseAlgo();              // Parse the algorithm (ALGO)
        expect(TokenType.RBRACE); // Expect '}'
        parseSubFuncs();          // Parse any sub-functions
        expect(TokenType.END);    // Expect 'end' to close the function
    }

// <LOCVARS> ::= VTYP VNAME , VTYP VNAME , VTYP VNAME ,
    private void parseLocVars() throws Exception {
        parseVType();             // Parse first variable type
        expect(TokenType.VNAME);  // Parse first variable name
        expect(TokenType.COMMA);
        parseVType();             // Parse second variable type
        expect(TokenType.VNAME);  // Parse second variable name
        expect(TokenType.COMMA);
        parseVType();             // Parse third variable type
        expect(TokenType.VNAME);  // Parse third variable name
        expect(TokenType.COMMA);  // Expect trailing comma (according to grammar)
    }

// <SUBFUNCS> ::= FUNCTIONS
    private void parseSubFuncs() throws Exception {
        parseFunctions(); // Functions within a function (sub-functions)
    }

    // <VTYP> ::= num | text
    private void parseVType() throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.TEXT) {
            consume(); // Consume the 'num' or 'text' token
        } else {
            throw new Exception("Expected 'num' or 'text' but found " + token);
        }
    }

    // <FTYP> ::= num | void
    private void parseFType() throws Exception {
        Token token = getCurrentToken();
        if (token.type == TokenType.NUM || token.type == TokenType.VOID) {
            consume(); // Consume the 'num' or 'void' token
        } else {
            throw new Exception("Expected 'num' or 'void' but found " + token);
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
            RecSPLParser parser = new RecSPLParser(tokens); // Pass tokens to the parser
            parser.parseProgram(); // Start parsing the program

            System.out.println("Parsing completed successfully. No syntax errors found.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}

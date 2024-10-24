
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RecSPLLexer {
    // Token Types Enum
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
    //     WHITESPACE("[ \t\f\r\n]+");

    //     public final String pattern;
    //     private TokenType(String pattern) {
    //         this.pattern = pattern;
    //     }
    // }
    // Token Class
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
    // Lexer Method
    public static List<Token> lex(String input) throws Exception {
        List<Token> tokens = new ArrayList<>();
        int tokenId = 1;

        StringBuilder tokenPatternsBuffer = new StringBuilder();
        for (TokenType tokenType : TokenType.values()) {
            tokenPatternsBuffer.append(String.format("|(?<%s>%s)", tokenType.name(), tokenType.pattern));
        }

        Pattern tokenPatterns = Pattern.compile(tokenPatternsBuffer.substring(1));

        Matcher matcher = tokenPatterns.matcher(input);
        while (matcher.find()) {
            for (TokenType tokenType : TokenType.values()) {
                if (matcher.group(tokenType.name()) != null) {
                    if (tokenType != TokenType.WHITESPACE) {  // Skip whitespaces
                        tokens.add(new Token(tokenType, matcher.group(tokenType.name()), tokenId++));
                    }
                    break;
                }
            }
        }

        if (matcher.find()) {
            throw new Exception("Lexical error: Unrecognized token '" + input.substring(matcher.start(), matcher.end()) + "' at position " + matcher.start());
        }

        return tokens;
    }

    public static void writeTokensToXML(List<Token> tokens, String outputPath) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("TOKENSTREAM");
        doc.appendChild(rootElement);
        for (Token token : tokens) {
            Element tokenElement = doc.createElement("TOK");
    
            Element id = doc.createElement("ID");
            id.appendChild(doc.createTextNode(String.valueOf(token.id)));
            tokenElement.appendChild(id);
    
            Element tokenClass = doc.createElement("CLASS");
            tokenClass.appendChild(doc.createTextNode(token.type.name()));
            tokenElement.appendChild(tokenClass);
    
            Element word = doc.createElement("WORD");
            word.appendChild(doc.createTextNode(token.data));
            tokenElement.appendChild(word);
    
            rootElement.appendChild(tokenElement);
        }
    
        // Configure the transformer to add indentation
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outputPath));
        transformer.transform(source, result);
    }
    
    public static void main(String[] args) {
        System.out.println(args.length);
        if (args.length != 1) {
            System.out.println("Usage: java RecSPLLexer <input_file.txt>");
            return;
        }

        String inputFile = args[0];
        String xmlOutputFile = "tokens_output.xml";

        try {
            // Read input file
            String input = new String(Files.readAllBytes(Paths.get(inputFile)));

            // Lexing process
            List<Token> tokens = lex(input);

            // Write tokens to XML
            writeTokensToXML(tokens, xmlOutputFile);
            System.out.println("Lexing completed successfully. Tokens saved to " + xmlOutputFile);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

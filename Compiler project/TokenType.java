// TokenTypes.java

public enum TokenType {
    MAIN("main"), BEGIN("begin"), END("end"),
    SKIP("skip"), HALT("halt"), PRINT("print"), INPUT("< input"),
    IF("if"), THEN("then"), ELSE("else"),
    // OR("or"), AND("and"), EQ("eq"), GRT("grt"),
    // ADD("add"), SUB("sub"), MUL("mul"), DIV("div"),
    // NOT("not"), SQRT("sqrt"), 
    // VOID("void"), 
    RETURN("return"),
    VNAME("V_[a-z]([a-z]|[0-9])*"), FNAME("F_[a-z]([a-z]|[0-9])*"),
    CONST("\"[A-Z][a-z]{0,7}\""),
    CONST2("[0-9]+"),
    SEMICOLON(";"), EQUALS("="), COMMA(","),
    LPAREN("\\("), RPAREN("\\)"), PROLOG("\\{"), EPILOG("\\}"),
    WHITESPACE("[ \t\f\r\n]+"),
    BINOP("eq|grt|add|sub|mul|div|or|and"),
    UNOP("not|sqrt"),
    TEXT("text"),NUM("num"),VOID("void");

    public final String pattern;

    private TokenType(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}

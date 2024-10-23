public class Token {
    public final TokenType type;
    public final String data;
    public final int id;

    public Token(TokenType type, String data, int id) {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    public boolean isType(TokenType type) {
        return this.type == type;
    }
    public String getName() {
        return data;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("(%d: %s, \"%s\")", id, type.name(), data);
    }
}
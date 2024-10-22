import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Character> table = new HashMap<>();

    public void add(String name, char type) {
        table.put(name, type);
    }

    public void link(char type, String name) {
        table.put(name, type);
    }

    public char getType(String name) {
        return table.getOrDefault(name, 'u'); // 'u' for undefined
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }
}

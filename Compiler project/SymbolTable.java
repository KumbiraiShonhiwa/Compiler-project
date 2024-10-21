import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, String> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public void addSymbol(String name, String type) {
        table.put(name, type);
    }

    public String getType(String name) {
        return table.get(name);
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }
}

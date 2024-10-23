
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {

    // Stack to manage scopes (each scope is a map of variable names to their type and value)
    final Stack<Map<String, SymbolInfo>> symbolTableStack;

    // Inner class to store type and value of each symbol
    // Constructor
    public SymbolTable() {
        this.symbolTableStack = new Stack<>();
        // Initialize with a global scope (root scope)
        this.symbolTableStack.push(new HashMap<>());
    }

    // Method to enter a new scope (pushes a new map onto the stack)
    public void enterScope() {
        symbolTableStack.push(new HashMap<>());
    }

    // Method to exit the current scope (pops the map from the stack)
    public void exitScope() {
        if (!symbolTableStack.isEmpty()) {
            symbolTableStack.pop();
        } else {
            throw new RuntimeException("No scope to exit.");
        }
    }

    // Add a symbol (variable) with its type and value to the current scope
    public void addSymbol(String name, String type, Object value) {
        if (symbolTableStack.isEmpty()) {
            throw new RuntimeException("No active scope.");
        }
        symbolTableStack.peek().put(name, new SymbolInfo(type, value, name));
    }

    public void add(String name, String type) {
        if (symbolTableStack.isEmpty()) {
            throw new RuntimeException("No active scope.");
        }
        symbolTableStack.peek().put(name, new SymbolInfo(type, null, name));
    }

    // Get the type of a symbol (variable) from the closest scope
    public String getType(String name) {
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).containsKey(name)) {
                return symbolTableStack.get(i).get(name).type;
            }
        }
        return null; // Return null if symbol is not found
    }

    // Get the value of a symbol (variable) from the closest scope
    public Object getValue(String name) {
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).containsKey(name)) {
                return symbolTableStack.get(i).get(name).value;
            }
        }
        return null; // Return null if symbol is not found
    }

    public String getVariableName(String name) {
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).containsKey(name)) {
                return symbolTableStack.get(i).get(name).internalName;
            }
        }
        return null; // Return null if symbol is not found
    }

    // Update the value of a symbol in the closest scope where it is found
    public void updateValue(String name, Object value) {
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).containsKey(name)) {
                symbolTableStack.get(i).get(name).value = value;
                return;
            }
        }
        throw new RuntimeException("Variable " + name + " not found.");
    }

    // Check if a symbol exists in any active scope
    public boolean containsSymbol(String name) {
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    // Print all symbols in the current scope (for debugging)
    public void printCurrentScope() {
        System.out.println("Current Scope Symbol Table:");
        if (!symbolTableStack.isEmpty()) {
            Map<String, SymbolInfo> currentScope = symbolTableStack.peek();
            for (Map.Entry<String, SymbolInfo> entry : currentScope.entrySet()) {
                System.out.println("Name: " + entry.getKey() + ", Type: " + entry.getValue().type + ", Value: " + entry.getValue().value);
            }
        } else {
            System.out.println("No active scope.");
        }
    }
    // Add this method to your SymbolTable class

    public void link(String type, String name) {
        // Ensure we're not overwriting an existing symbol in the current scope
        if (symbolTableStack.peek().containsKey(name)) {
            throw new RuntimeException("Symbol " + name + " is already defined in the current scope.");
        }
        // Add the symbol with a type but no initial value (value is set to null)
        addSymbol(name, type, null);
    }
}
